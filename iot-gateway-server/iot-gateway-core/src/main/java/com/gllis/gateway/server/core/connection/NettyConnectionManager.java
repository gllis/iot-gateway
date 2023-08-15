package com.gllis.gateway.server.core.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty 连接管理
 *
 * @author glli
 * @date 2023/8/15
 */
@Component
public class NettyConnectionManager implements ConnectionManager {
    private final Map<ChannelId, Connection> connectionMap = new ConcurrentHashMap<>();
    @Override
    public Connection removeAndClose(Channel channel) {
        return connectionMap.remove(channel.id());
    }

    @Override
    public void add(Connection connection) {
        connectionMap.putIfAbsent(connection.getChannel().id(), connection);
    }

    @Override
    public Connection get(Channel channel) {
        return connectionMap.get(channel.id());
    }

    @PreDestroy
    public void destroy() {
        connectionMap.values().forEach(Connection::close);
        connectionMap.clear();
    }
}
