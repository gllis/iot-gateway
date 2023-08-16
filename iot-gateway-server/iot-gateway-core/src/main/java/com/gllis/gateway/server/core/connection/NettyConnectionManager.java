package com.gllis.gateway.server.core.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    private final Map<String, Connection> snConnectionMap = new ConcurrentHashMap<>();
    @Override
    public Connection removeAndClose(Channel channel) {
        Connection connection = connectionMap.remove(channel.id());
        if (!StringUtils.isEmpty(connection.getSn())) {
            snConnectionMap.remove(connection.getSn());
        }
        return connection;
    }

    @Override
    public void add(Connection connection) {
        connectionMap.putIfAbsent(connection.getChannel().id(), connection);
        if (!StringUtils.isEmpty(connection.getSn())) {
            snConnectionMap.putIfAbsent(connection.getSn(), connection);
        }

    }

    @Override
    public Connection get(Channel channel) {
        return connectionMap.get(channel.id());
    }

    @Override
    public Connection get(String sn) {
        if (StringUtils.isEmpty(sn)) {
            return null;
        }
        return snConnectionMap.get(sn);
    }

    @PreDestroy
    public void destroy() {
        connectionMap.values().forEach(Connection::close);
        connectionMap.clear();
        snConnectionMap.clear();
    }
}
