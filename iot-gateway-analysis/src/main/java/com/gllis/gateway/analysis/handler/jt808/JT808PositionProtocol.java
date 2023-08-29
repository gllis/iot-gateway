package com.gllis.gateway.analysis.handler.jt808;

import com.gllis.gateway.analysis.ProtocolProcessing;
import com.gllis.gateway.analysis.handler.BaseHandler;
import com.gllis.gateway.analysis.manager.MqProducerManager;
import com.gllis.gateway.server.annotation.Handler;
import com.gllis.gateway.server.constant.JT808Cmd;
import com.gllis.gateway.server.domain.Alarm;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.domain.PositionPacket;
import com.gllis.gateway.server.domain.Status;
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
public class JT808PositionProtocol extends BaseHandler {

    @Override
    public void handler(Packet packet) {
        super.handler(packet);
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
        outBasePacket.setAlarm(parseAlarm(alarm));
        outBasePacket.setStatus(parseStatus(status));
        buf.release();

        mqProducerManager.sendOutPacket(outBasePacket);
    }

    /**
     * 解析报警
     *
     * @param alarmBytes
     */
    private Alarm parseAlarm(byte[] alarmBytes) {
        Alarm alarm = new Alarm();
        alarm.setLowPower((alarmBytes[3] & 0B10000000) == 0B10000000 ? 1 : 0);
        alarm.setPowerOff((alarmBytes[2] & 0B00000001) == 0B00000001 ? 1 : 0);
        return alarm;
    }

    /**
     * 解析状态
     *
     * @param statusBytes
     * @return
     */
    private Status parseStatus(byte[] statusBytes) {
        Status status = new Status();
        status.setAcc((statusBytes[3] & 0B00000001) == 0B00000001 ? 1 : 0);
        status.setGps((statusBytes[3] & 0B00000010) == 0B00000010 ? 1 : 0);
        return status;
    }
}
