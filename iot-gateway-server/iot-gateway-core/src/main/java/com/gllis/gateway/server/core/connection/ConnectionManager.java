package com.gllis.gateway.server.core.connection;

import io.netty.channel.Channel;

/**
 * 连接管理接口
 *
 * @author glli
 * @date 2023/8/15
 */
public interface ConnectionManager {
    Connection removeAndClose(Channel channel);

    void add(Connection connection);

    Connection get(Channel channel);
}
