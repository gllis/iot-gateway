package com.gllis.gateway.server.main.handler.jt808.cmd;

import com.gllis.gateway.server.annotation.SendCmdHandler;
import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.handler.CommandProcessing;
import com.gllis.gateway.server.core.util.HexUtil;
import com.gllis.gateway.server.domain.Command;
import com.gllis.gateway.server.enums.DataFormatEnum;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.enums.SendCommandTypeEnum;
import com.gllis.gateway.server.exception.ServiceException;
import com.gllis.gateway.server.main.util.JT808Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

/**
 * JT808文本指令透传
 *
 * @author glli
 * @date 2023/8/16
 */
@Slf4j
@SendCmdHandler(protocol = ProtocolEnum.JT808, cmdType = SendCommandTypeEnum.CUSTOM)
public class JT808CustomCommand implements CommandProcessing {
    @Override
    public void handler(Command command, Connection connection) {
        connection.writeAndFlush(getCustomCmd(command), DataFormatEnum.HEX);
    }

    /**
     * 拼装文本透传指令
     *
     * @param command
     * @return
     */
    private byte[] getCustomCmd(Command command) {
        if (command.getContent() == null) {
            throw new ServiceException("command context is null");
        }
        byte[] textBytes = command.getContent().trim().getBytes();
        byte[] sn = HexUtil.convertHexToByte(command.getSn());
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(0x8300);
        buf.writeShort(textBytes.length);
        buf.writeByte(JT808Util.getCheckCode(buf.array(), buf.readableBytes()));
        buf.writeBytes(sn);
        buf.writeShort(command.getCmdType());

        byte[] cmd = new byte[buf.readableBytes()];
        buf.readBytes(cmd);
        buf.release();
        return JT808Util.escaped(cmd);
    }
}
