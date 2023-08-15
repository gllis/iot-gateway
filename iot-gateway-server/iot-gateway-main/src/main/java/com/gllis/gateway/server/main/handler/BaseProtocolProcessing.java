package com.gllis.gateway.server.main.handler;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.handler.ProtocolProcessing;
import com.gllis.gateway.server.domain.Packet;

/**
 * 协议处理基类
 *
 * @author glli
 * @date 2023/8/15
 */
public class BaseProtocolProcessing implements ProtocolProcessing {
    @Override
    public void handler(Packet packet, Connection connection) {

    }
}
