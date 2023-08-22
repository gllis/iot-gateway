package com.gllis.gateway.server.core.connection;

import com.gllis.gateway.server.enums.DataFormatEnum;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * 连接器接口
 *
 * @author glli
 * @date 2023/8/15
 */
public interface Connection {
    void init(Channel channel, boolean security);
    Channel getChannel();
    String getSn();
    void setSn(String sn);
    void setTopic(String topic);
    void close();
    void setAddress(InetSocketAddress address);
    void writeAndFlush(byte[] data, DataFormatEnum df);
}
