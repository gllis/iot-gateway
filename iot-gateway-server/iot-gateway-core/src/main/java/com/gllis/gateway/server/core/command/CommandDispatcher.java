package com.gllis.gateway.server.core.command;

import com.gllis.gateway.server.constant.RedisKeyConstant;
import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.handler.CommandProcessing;
import com.gllis.gateway.server.core.manager.CommandManager;
import com.gllis.gateway.server.core.manager.PackageManager;
import com.gllis.gateway.server.domain.Command;
import com.gllis.gateway.server.domain.Packet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 指令调度
 *
 * @author glli
 * @date 2023/8/16
 */
@Slf4j
@Component
public class CommandDispatcher {
    @Autowired
    private PackageManager packageManager;
    @Autowired
    private CommandManager commandManager;

    public void onReceive(Packet packet, Connection connection) {
        CopyOnWriteArrayList<Command> commandList = commandManager.getCommandList(RedisKeyConstant.SENT_DOWN_COMMAND + packet.getSn());
        if (CollectionUtils.isEmpty(commandList)) {
            return;
        }
        for (Command command : commandList) {
            if (command == null) {
                continue;
            }

            CommandProcessing commandProcessing = packageManager.getCommandProcessing(command);
            if (commandProcessing == null) {
                commandManager.removeCommand(command);
            } else {
                commandProcessing.handler(command, connection);
                log.debug("Get send down cmd handler. handler={}, sn={}, model={}", commandProcessing, packet.getSn(), packet.getModel());
            }
        }
    }
}
