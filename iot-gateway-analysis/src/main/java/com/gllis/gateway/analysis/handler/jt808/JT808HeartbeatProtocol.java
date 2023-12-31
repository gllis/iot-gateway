package com.gllis.gateway.analysis.handler.jt808;

import com.gllis.gateway.analysis.ProtocolProcessing;
import com.gllis.gateway.analysis.handler.BaseHandler;
import com.gllis.gateway.analysis.manager.MqProducerManager;
import com.gllis.gateway.server.annotation.Handler;
import com.gllis.gateway.server.constant.JT808Cmd;
import com.gllis.gateway.server.domain.HeartbeatPacket;
import com.gllis.gateway.server.domain.OutBasePacket;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.enums.ProtocolEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JT808 心跳
 *
 * @author glli
 * @date 2023/8/17
 */
@Handler(protocol = ProtocolEnum.JT808, cmdType = JT808Cmd.HEARTBEAT)
public class JT808HeartbeatProtocol extends BaseHandler {

    @Override
    public void handler(Packet packet) {
        super.handler(packet);
        OutBasePacket outBasePacket = new HeartbeatPacket();
        BeanUtils.copyProperties(packet, outBasePacket);
        mqProducerManager.sendOutPacket(outBasePacket);
    }
}
