package com.gllis.gateway.server.core.handler;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.domain.Command;

/**
 * 指令处理接口
 *
 * @author glli
 * @date 2023/8/15
 */
public interface CommandProcessing {
    void handler(Command command, Connection connection);
}
