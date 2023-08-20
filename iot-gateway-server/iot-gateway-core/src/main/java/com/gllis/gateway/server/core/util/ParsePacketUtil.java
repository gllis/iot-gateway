package com.gllis.gateway.server.core.util;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.manager.SnModelManager;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.domain.SnModel;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.util.HexUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析工具类
 */
@Slf4j
@Component
public class ParsePacketUtil {

    @Autowired
    private SnModelManager snModelManager;
    /**
     * 解析报文
     *
     * @param packet
     * @param ctx
     * @param connection
     */
    public void parsePacket(Packet packet, ChannelHandlerContext ctx, Connection connection) {
        if (connection == null) {
            return;
        }
        byte[] data = packet.getBody();
        String sn = connection.getSn();

        // jt808
        if (data[0] == 0x7E) {
            data = jt808Escaped(data);
            sn = HexUtil.convertByteToHex(ArrayUtils.subarray(data, 5, 11));
            packet.setBody(data);
            packet.setCmd(HexUtil.convertByteToHex(ArrayUtils.subarray(data, 1, 3)));
            packet.setProtocolEnum(ProtocolEnum.JT808);
            packet.setSn(sn);
        }
        if (sn == null) {
            log.error("sn is null");
            ctx.close();
            return;
        }
        SnModel snModel = snModelManager.get(sn);
        if (snModel != null) {
            packet.setModel(snModel.getModel());
            packet.setDeviceId(snModel.getDeviceId());
            packet.setProtocolEnum(ProtocolEnum.get(snModel.getProtocol()));
        }
        connection.setSn(sn);

    }

    /**
     * 转义
     *
     * @param data
     */
    private byte[] jt808Escaped(byte[] data) {
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x7D && data[i+1] == 0x02) {
                bytes.add((byte) 0x7E);
                i++;
            } else if (data[i] == 0x7D && data[i+1] == 0x01) {
                bytes.add((byte) 0x7D);
                i++;
            } else {
                bytes.add(data[i]);
            }
        }
        return ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()]));
    }
}
