package com.gllis.gateway.server.handler;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.connection.ConnectionManager;
import com.gllis.gateway.server.core.connection.NettyConnect;
import com.gllis.gateway.server.core.message.PacketReceiver;
import com.gllis.gateway.server.core.util.ParsePacketUtil;
import com.gllis.gateway.server.domain.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 处理通道数据
 *
 * @author glli
 * @date 2023/8/14
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {


    @Autowired
    @Qualifier("nettyConnectionManager")
    private ConnectionManager connectionManager;

    @Autowired
    private PacketReceiver receiver;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("client connected conn = {}", ctx.channel());
        }
        Connection connection = new NettyConnect();
        connection.init(ctx.channel(), false);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        if (log.isDebugEnabled()) {
            log.debug("client disconnected conn = {}", connection);
        }
        ctx.fireChannelInactive();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Packet packet = (Packet) msg;
            if (packet.getBody() == null || packet.getBody().length == 0) {
                return;
            }
            Connection connection = connectionManager.get(ctx.channel());
            ParsePacketUtil.parsePacket(packet, ctx, connectionManager);
            receiver.onReceive(packet, connection);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("channelRead error:", e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        log.error("client caught ex, conn = {}, Throwable = {}", connection, cause);
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Connection connection = connectionManager.removeAndClose(ctx.channel());
                if (log.isDebugEnabled()) {
                    log.debug("6分钟没有心跳，client disconnected conn={}", connection);
                }
                ctx.fireUserEventTriggered(event);
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
