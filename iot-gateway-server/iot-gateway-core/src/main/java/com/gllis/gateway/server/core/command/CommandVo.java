package com.gllis.gateway.server.core.command;

import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.domain.Packet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 指令下发
 *
 * @author glli
 * @date 2023/8/16
 */
@Data
@Slf4j
public class CommandVo implements Serializable {

    private static final long serialVersionUID = -3395541650486114846L;

    private Packet packet;

    private Connection connection;
}
