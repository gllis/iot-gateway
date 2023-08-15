package com.gllis.gateway.server.core.message;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.handler.ProtocolProcessing;
import com.gllis.gateway.server.core.manager.PackageManager;
import com.gllis.gateway.server.domain.Packet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收数据包消息处理
 */
@Slf4j
@Component
public class MessageDispatcher implements PacketReceiver {
    @Autowired
    private PackageManager packageManager;

    @Override
    public void onReceive(Packet packet, Connection connection) {
        log.info("{} dispatch {} message packet", packet.getSn(), packet.getProtocolEnum());
        ProtocolProcessing handler = packageManager.getMessageHandler(packet);
        if (handler != null) {
            handler.handler(packet, connection);
        }
    }

}
