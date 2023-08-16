package com.gllis.gateway.server.core.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandTask {

    @Autowired
    private CommandDispatcher commandDispatcher;

    @Async
    public void send(CommandVo commandVo) {
        commandDispatcher.onReceive(commandVo.getPacket(), commandVo.getConnection());
    }
}
