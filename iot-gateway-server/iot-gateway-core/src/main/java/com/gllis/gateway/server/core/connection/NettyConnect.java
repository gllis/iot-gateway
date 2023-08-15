package com.gllis.gateway.server.core.connection;

import com.gllis.gateway.server.enums.ConnStateEnum;
import com.gllis.gateway.server.enums.DataFormatEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * netty 连接器
 *
 * @author glli
 * @date 2023/8/15
 */
@Slf4j
public class NettyConnect implements Connection {

    private volatile ConnStateEnum state = ConnStateEnum.NEW;
    private Channel channel;

    private long lastReadTime;
    private long lastWriteTime;
    private InetSocketAddress address;
    private String sn;

    @Override
    public void init(Channel channel, boolean security) {
        this.channel = channel;
        this.lastReadTime = System.currentTimeMillis();
        this.state = ConnStateEnum.CONNECTED;
    }

    @Override
    public void writeAndFlush(byte[] data, DataFormatEnum df) {
        if (df == null) {
            df = DataFormatEnum.HEX;
        }
        ByteBuf buf = Unpooled.copiedBuffer(data);
        this.channel.writeAndFlush(buf);
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public String getSn() {
        return this.sn;
    }

    @Override
    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public void close() {
        if (state == ConnStateEnum.DISCONNECTED) {
            return;
        }
        this.state = ConnStateEnum.DISCONNECTED;
        this.channel.close();
    }
}