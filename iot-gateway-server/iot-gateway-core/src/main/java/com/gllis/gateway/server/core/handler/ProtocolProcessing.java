package com.gllis.gateway.server.core.handler;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.domain.Packet;

/**
 * 协议处理
 *
 * @author glli
 * @date 2023/8/23
 */
public interface ProtocolProcessing {
    void handler(Packet packet, Connection connection);
}
