package com.gllis.gateway.server.main.handler;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.handler.ProtocolProcessing;
import com.gllis.gateway.server.core.manager.MqProducerManager;
import com.gllis.gateway.server.domain.Packet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 协议处理基类
 *
 * @author glli
 * @date 2023/8/15
 */
public class BaseProtocolProcessing implements ProtocolProcessing {

    @Autowired
    private MqProducerManager mqProducerManager;

    /**
     * 是否转发，默认true
     */
    protected boolean isForward(){
        return true;
    }
    @Override
    public void handler(Packet packet, Connection connection) {
        if (isForward()) {
            mqProducerManager.sendPacket(packet);
        }
    }
}
