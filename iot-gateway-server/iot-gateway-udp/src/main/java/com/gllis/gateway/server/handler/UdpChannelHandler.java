package com.gllis.gateway.server.handler;


import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.connection.ConnectionManager;
import com.gllis.gateway.server.core.connection.NettyConnect;
import com.gllis.gateway.server.core.manager.MqProducerManager;
import com.gllis.gateway.server.core.message.PacketReceiver;
import com.gllis.gateway.server.core.util.ParsePacketUtil;
import com.gllis.gateway.server.domain.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


/**
 * udp 协议处理
 *
 * @author GL
 * @created 2023/8/19.
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class UdpChannelHandler extends ChannelInboundHandlerAdapter {


    @Autowired
    @Qualifier("nettyConnectionManager")
    private ConnectionManager connectionManager;

    private PacketReceiver receiver;

    @Autowired
    private MqProducerManager mqProducerManager;

    public UdpChannelHandler(PacketReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("client connected conn = {}", ctx.channel());
        }
        Connection connection = new NettyConnect(mqProducerManager);
        connection.init(ctx.channel(), false);
        connectionManager.add(connection);
        log.info("init udp channel={}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        log.info("disconnect udp channel={}, connection={}", ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket datagramPacket = (DatagramPacket) msg;

        try {
            ByteBuf buffer = datagramPacket.content();
            byte[] body = new byte[buffer.readableBytes()];
            buffer.readBytes(body);
            buffer.release();

            Connection connection = connectionManager.get(ctx.channel());
            connection.setAddress(datagramPacket.sender());
            Packet packet = Packet.builder().body(body).build();
            ParsePacketUtil.parsePacket(packet, ctx, connection);
            receiver.onReceive(packet, connection);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("channelRead error:" + e.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("udp handler caught an exception, channel={}, conn={}", ctx.channel(), cause);
    }



}
