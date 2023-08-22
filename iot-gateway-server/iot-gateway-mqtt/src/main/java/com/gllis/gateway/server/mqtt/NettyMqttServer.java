package com.gllis.gateway.server.mqtt;

import com.gllis.gateway.server.conf.NettyMqttConf;
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
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * mqtt 服务
 *
 * @author glli
 * @date 2023/8/22
 */
@Slf4j
public class NettyMqttServer extends BaseServiceImpl {
    @Autowired
    private NettyMqttConf nettyMqttConf;
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
        if (nettyMqttConf.isEpollEnabled()) {
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
        NioEventLoopGroup epollBossGroup = new NioEventLoopGroup(nettyMqttConf.getThreadNum(), getThreadFactory());
        epollBossGroup.setIoRatio(100);
        this.bossGroup = epollBossGroup;

        NioEventLoopGroup epollWorkGroup = new NioEventLoopGroup(nettyMqttConf.getThreadNum(), getThreadFactory());
        epollWorkGroup.setIoRatio(100);
        this.workerGroup = epollWorkGroup;
        createServer(listener, NioServerSocketChannel::new);
    }


    private void createEpollServer(Listener listener) {
        EpollEventLoopGroup epollBossGroup = new EpollEventLoopGroup(nettyMqttConf.getThreadNum(), getThreadFactory());
        epollBossGroup.setIoRatio(100);
        this.bossGroup = epollBossGroup;

        EpollEventLoopGroup epollWorkGroup = new EpollEventLoopGroup(nettyMqttConf.getThreadNum(), getThreadFactory());
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
            InetSocketAddress address = !StringUtils.isEmpty(nettyMqttConf.getHost()) ?
                    new InetSocketAddress(nettyMqttConf.getHost(), nettyMqttConf.getPort()) : new InetSocketAddress(nettyMqttConf.getPort());
            bootstrap.bind(address).addListener(future -> {
                if (future.isSuccess()) {
                    serverState.set(ServerStateEnum.Started);
                    log.info("server start success on:{}", nettyMqttConf.getPort());
                    if (listener != null) {
                        listener.onSuccess(nettyMqttConf.getPort());
                    }
                } else {
                    log.info("server start failure on:{}", nettyMqttConf.getPort(), future.cause());
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
            throw new ServiceException("server start exception, port=" + nettyMqttConf.getPort(), e);
        }
    }


    private ThreadFactory getThreadFactory() {
        return new DefaultThreadFactory(nettyMqttConf.getBoosThreadName());
    }

    private void initOption(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }
    private void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new IdleStateHandler(nettyMqttConf.getTimeout(), 0, 0, TimeUnit.MINUTES));
        pipeline.addLast("decoder", new MqttDecoder());
        pipeline.addLast("handler", serverChannelHandler);
    }
}
