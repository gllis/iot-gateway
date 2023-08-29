package com.gllis.gateway.analysis.handler;

import com.gllis.gateway.analysis.ProtocolProcessing;
import com.gllis.gateway.analysis.manager.MqProducerManager;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.util.HexUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 处理基类
 *
 * @author glli
 * @date 2023/8/29
 */
public class BaseHandler implements ProtocolProcessing {
    @Autowired
    protected MqProducerManager mqProducerManager;
    @Override
    public void handler(Packet packet) {
        mqProducerManager.sendDevicePacketUpLog(packet.getSn(), HexUtil.convertByteToHex(packet.getBody()));
    }
}
