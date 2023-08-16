package com.gllis.gateway.server.main.handler.jt808.protocol;

import com.gllis.gateway.server.annotation.Handler;
import com.gllis.gateway.server.constant.JT808Cmd;
import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.enums.DataFormatEnum;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.main.handler.BaseProtocolProcessing;
import com.gllis.gateway.server.main.util.JT808Util;
import lombok.extern.slf4j.Slf4j;

/**
 * JT808 位置汇报
 *
 * @date 2023/8/14
 */
@Slf4j
@Handler(protocol = ProtocolEnum.JT808, cmdType = JT808Cmd.POSITION)
public class JT808PositionProtocol extends BaseProtocolProcessing {

    @Override
    public void handler(Packet packet, Connection connection) {
        connection.writeAndFlush(JT808Util.getServerResponse(packet), DataFormatEnum.HEX);
        super.handler(packet, connection);
    }
}
