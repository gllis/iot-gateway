package com.gllis.gateway.server.core.connection;

import com.gllis.gateway.server.enums.DataFormatEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

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
    void close();
    void writeAndFlush(byte[] data, DataFormatEnum df);
}
