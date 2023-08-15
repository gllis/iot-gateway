package com.gllis.gateway.server.core.codec;

import com.gllis.gateway.server.core.constant.SysConstant;
import com.gllis.gateway.server.domain.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * netty 解包
 *
 * @author glli
 * @date 2023/8/14
 */
public final class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 计算byteBuf数据总长度
        int totalLength = in.readableBytes();
        if (totalLength < 5) {
            in.resetReaderIndex();
        }
        byte[] header = new byte[5];
        in.readBytes(header);

        int dataLength = -1;
        if (header[0] == 0x7E) {
            byte temp = 0x7E;
            int fromIndex = in.readerIndex() - 5;
            dataLength = in.indexOf(fromIndex + 1, fromIndex + totalLength, temp) + 1;
            dataLength = dataLength > 0 ? dataLength - fromIndex : dataLength;
        }

        if (SysConstant.PACKET_SIZE_INVALID == dataLength) {
            in.clear();
            ctx.close();
            return;
        }
        // 重置读取下标
        in.resetReaderIndex();
        byte[] body = new byte[dataLength];
        in.readBytes(body);
        if (totalLength > dataLength) {
            in.markReaderIndex();
        }
        out.add(Packet.builder().body(body).build());

    }
}
