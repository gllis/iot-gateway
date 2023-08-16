package com.gllis.gateway.server.main.util;

import com.gllis.gateway.server.domain.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JT808工具类
 *
 * @author glli
 * @date 2023/8/15
 */
public class JT808Util {

    /**
     * 获取应答内容
     *
     * @param packet
     * @return
     */
    public static byte[] getServerResponse(Packet packet) {
        byte[] serialNumber = Arrays.copyOfRange(packet.getBody(), 11, 13);
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(0x8001);
        buf.writeShort(0x0005);
        buf.writeBytes(Arrays.copyOfRange(packet.getBody(), 5, 11));
        buf.writeBytes(serialNumber);
        buf.writeBytes(serialNumber);
        buf.writeShort(Integer.parseInt(packet.getCmd(), 16));
        buf.writeByte(0x0);
        buf.writeByte(getCheckCode(buf.array(), buf.readableBytes()));

        byte[] resp = new byte[buf.readableBytes()];
        buf.readBytes(resp);
        buf.release();
        resp = escaped(resp);
        return resp;
    }

    /**
     * 生成检验码
     *
     * @param data
     * @param length
     * @return
     */
    public static byte getCheckCode(byte[] data, int length) {
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = data[i] ^ value;
        }
        return (byte) value;
    }

    /**
     * 转义
     *
     * @param data
     */
    public static byte[] escaped(byte[] data) {
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) 0x7E);
        for (byte tmp : data) {
            if (tmp == 0x7D) {
                bytes.add((byte) 0x7D);
                bytes.add((byte) 0x01);
            } else if (tmp == 0x7E) {
                bytes.add((byte) 0x7E);
                bytes.add((byte) 0x02);
            } else {
                bytes.add(tmp);
            }
        }
        bytes.add((byte) 0x7E);
        return ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()]));
    }
}
