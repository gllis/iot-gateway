package com.gllis.gateway.analysis.handle.jt808;

import com.gllis.gateway.analysis.ProtocolProcessing;
import com.gllis.gateway.analysis.manager.MqProducerManager;
import com.gllis.gateway.server.annotation.Handler;
import com.gllis.gateway.server.constant.JT808Cmd;
import com.gllis.gateway.server.domain.OutBasePacket;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.domain.PositionPacket;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.util.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * JT808 位置汇报
 *
 * @author glli
 * @date 2023/8/17
 */
@Handler(protocol = ProtocolEnum.JT808, cmdType = JT808Cmd.POSITION)
public class JT808PositionProtocol implements ProtocolProcessing {

    @Autowired
    private MqProducerManager mqProducerManager;

    @Override
    public void handler(Packet packet) {
        PositionPacket outBasePacket = new PositionPacket();
        BeanUtils.copyProperties(packet, outBasePacket);
        // 去掉包头
        byte[] data = ArrayUtils.subarray(packet.getBody(), 13, packet.getBody().length - 1);
        System.out.println(HexUtil.convertByteToHex(data));
        ByteBuf buf = Unpooled.copiedBuffer(data);
        byte[] alarm = new byte[4];
        byte[] status = new byte[4];
        buf.readBytes(alarm);
        buf.readBytes(status);
        outBasePacket.setLat((buf.readInt()) / 1000000.0);
        outBasePacket.setLon((buf.readInt()) / 1000000.0);
        outBasePacket.setAltitude(buf.readShort());
        outBasePacket.setSpeed(buf.readShort());
        buf.release();

        mqProducerManager.sendOutPacket(outBasePacket);
    }

}
