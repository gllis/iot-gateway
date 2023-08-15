package com.gllis.gateway.server.tcp;

import com.gllis.gateway.server.conf.NettyTcpConf;
import com.gllis.gateway.server.core.codec.PacketDecoder;
import com.gllis.gateway.server.core.listener.Listener;
import com.gllis.gateway.server.core.service.BaseServiceImpl;
import com.gllis.gateway.server.enums.ServerStateEnum;
import com.gllis.gateway.server.exception.ServiceException;
import com.gllis.gateway.server.handler.ServerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * tcp服务
 *
 * @author glli
 * @date 2023/8/15
 */
@Slf4j
@Component
@Configuration
@EnableConfigurationProperties(NettyTcpConf.class)
public class NettyTcpServer extends BaseServiceImpl {

    @Autowired
    private NettyTcpConf nettyTcpConf;
    @Autowired
    private ServerChannelHandler serverChannelHandler;

    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;


    private final AtomicReference<ServerStateEnum> serverState = new AtomicReference<>(ServerStateEnum.Created);
    @Override
    public void init() {
        if (!serverState.compareAndSet(ServerStateEnum.Created, ServerStateEnum.Initialized)) {
            throw new ServiceException("Server already init");
        }
    }

    @Override
    public boolean isRunning() {
        return serverState.get() == ServerStateEnum.Started;
    }

    @Override
    public void start(Listener listener) {
        if (!serverState.compareAndSet(ServerStateEnum.Initialized, ServerStateEnum.Starting)) {
            throw new ServiceException("Server already started or have not init");
        }
        if (nettyTcpConf.isEpollEnabled()) {
            createEpollServer(listener);
        } else {
            createNioServer(listener);
        }
        super.start(listener);
    }

    @Override
    public void stop(Listener listener) {
        if (serverState.compareAndSet(ServerStateEnum.Started, ServerStateEnum.Shutdown)) {
            if (listener != null) {
                listener.onFailure(new ServiceException("server was already shutdown."));
            }
            log.info("{} was already shutdown.", this.getClass().getSimpleName());
            return;
        }
        log.info("try shutdown {}...", this.getClass().getSimpleName());
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
        log.info("{} shutdown success.", this.getClass().getSimpleName());
        super.stop(listener);
    }

    private void createNioServer(Listener listener) {
        NioEventLoopGroup epollBossGroup = new NioEventLoopGroup(nettyTcpConf.getThreadNum(), getThreadFactory());
        epollBossGroup.setIoRatio(100);
        this.bossGroup = epollBossGroup;

        NioEventLoopGroup epollWorkGroup = new NioEventLoopGroup(nettyTcpConf.getThreadNum(), getThreadFactory());
        epollWorkGroup.setIoRatio(100);
        this.workerGroup = epollWorkGroup;
        createServer(listener, NioServerSocketChannel::new);
    }


    private void createEpollServer(Listener listener) {
        EpollEventLoopGroup epollBossGroup = new EpollEventLoopGroup(nettyTcpConf.getThreadNum(), getThreadFactory());
        epollBossGroup.setIoRatio(100);
        this.bossGroup = epollBossGroup;

        EpollEventLoopGroup epollWorkGroup = new EpollEventLoopGroup(nettyTcpConf.getThreadNum(), getThreadFactory());
        epollWorkGroup.setIoRatio(100);
        this.workerGroup = epollWorkGroup;
        createServer(listener, EpollServerSocketChannel::new);
    }

    private void createServer(Listener listener, ChannelFactory<? extends ServerChannel> channelFactory) {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channelFactory(channelFactory);
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    initPipeline(channel.pipeline());
                }
            });
            initOption(bootstrap);
            InetSocketAddress address = !StringUtils.isEmpty(nettyTcpConf.getHost()) ?
                    new InetSocketAddress(nettyTcpConf.getHost(), nettyTcpConf.getPort()) : new InetSocketAddress(nettyTcpConf.getPort());
            bootstrap.bind(address).addListener(future -> {
                if (future.isSuccess()) {
                    serverState.set(ServerStateEnum.Started);
                    log.info("server start success on:{}", nettyTcpConf.getPort());
                    if (listener != null) {
                        listener.onSuccess(nettyTcpConf.getPort());
                    }
                } else {
                    log.info("server start failure on:{}", nettyTcpConf.getPort(), future.cause());
                    if (listener != null) {
                        listener.onFailure(future.cause());
                    }
                }
            });
        } catch (Exception e) {
            log.error("server start exception", e);
            if (listener != null) {
                listener.onFailure(e);
            }
            throw new ServiceException("server start exception, port=" + nettyTcpConf.getPort(), e);
        }
    }


    private ThreadFactory getThreadFactory() {
        return new DefaultThreadFactory(nettyTcpConf.getBoosThreadName());
    }

    private void initOption(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }
    private void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new IdleStateHandler(nettyTcpConf.getTimeout(), 0, 0, TimeUnit.MINUTES));
        pipeline.addLast("decoder", new PacketDecoder());
        pipeline.addLast("handler", serverChannelHandler);

    }
}
