package com.gllis.gateway.server.udp;

import com.gllis.gateway.server.conf.NettyUdpConf;
import com.gllis.gateway.server.core.listener.Listener;
import com.gllis.gateway.server.core.service.BaseServiceImpl;
import com.gllis.gateway.server.enums.ServerStateEnum;
import com.gllis.gateway.server.exception.ServiceException;
import com.gllis.gateway.server.handler.UdpChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;
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
@EnableConfigurationProperties(NettyUdpConf.class)
public class NettyUdpServer extends BaseServiceImpl {

    @Autowired
    private NettyUdpConf nettyUdpConf;
    @Autowired
    private UdpChannelHandler udpChannelHandler;

    protected EventLoopGroup eventLoopGroup;


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
        if (nettyUdpConf.isEpollEnabled()) {
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
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully().syncUninterruptibly();
        }
        log.info("{} shutdown success.", this.getClass().getSimpleName());
        super.stop(listener);
    }

    private void createNioServer(Listener listener) {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(
                4, new DefaultThreadFactory(nettyUdpConf.getBoosThreadName())
        );
        eventLoopGroup.setIoRatio(100);
        this.eventLoopGroup = eventLoopGroup;
        createServer(listener, () -> new NioDatagramChannel(InternetProtocolFamily.IPv4));
    }


    private void createEpollServer(Listener listener) {
        EpollEventLoopGroup eventLoopGroup = new EpollEventLoopGroup(
                4, new DefaultThreadFactory(nettyUdpConf.getBoosThreadName())
        );
        eventLoopGroup.setIoRatio(100);
        this.eventLoopGroup = eventLoopGroup;
        createServer(listener, EpollDatagramChannel::new);
    }

    private void createServer(Listener listener, ChannelFactory<? extends DatagramChannel> channelFactory) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup);
            bootstrap.channelFactory(channelFactory)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(udpChannelHandler);

            initOption(bootstrap);

            bootstrap.bind(nettyUdpConf.getPort()).addListener(future -> {
                if (future.isSuccess()) {
                    serverState.set(ServerStateEnum.Started);
                    log.info("server start success on:{}", nettyUdpConf.getPort());
                    if (listener != null) {
                        listener.onSuccess(nettyUdpConf.getPort());
                    }
                } else {
                    log.info("server start failure on:{}", nettyUdpConf.getPort(), future.cause());
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
            throw new ServiceException("server start exception, port=" + nettyUdpConf.getPort(), e);
        }
    }


    private ThreadFactory getThreadFactory() {
        return new DefaultThreadFactory(nettyUdpConf.getBoosThreadName());
    }

    private void initOption(Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
    }
}
