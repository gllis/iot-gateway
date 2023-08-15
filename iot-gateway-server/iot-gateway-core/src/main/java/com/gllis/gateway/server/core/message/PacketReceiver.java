package com.gllis.gateway.server.core.message;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.domain.Packet;

/**
 * 数据包接收接口
 *
 * @author glli
 * @date 2023/8/15
 */
public interface PacketReceiver {
    void onReceive(Packet packet, Connection connection);
}
