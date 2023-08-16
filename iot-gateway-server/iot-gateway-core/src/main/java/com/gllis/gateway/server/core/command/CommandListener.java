package com.gllis.gateway.server.core.command;


import com.alibaba.fastjson2.JSON;
import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.connection.ConnectionManager;
import com.gllis.gateway.server.core.manager.CommandManager;
import com.gllis.gateway.server.core.manager.PackageManager;
import com.gllis.gateway.server.domain.Command;
import com.gllis.gateway.server.domain.Packet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 监听指令下发
 *
 * @author glli
 * @date 2023/8/16
 */
@Slf4j
@Component
public class CommandListener implements MessageListener {

    @Value("${iot.command.add}")
    private String iotCmdAdd;
    @Value("${iot.command.remove}")
    private String iotCmdRemove;

    @Autowired
    private PackageManager packageManager;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private CommandManager commandManager;

    @Autowired
    private CommandTask commandTask;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (pattern == null) {
            return;
        }
        String topic = new String(pattern);
        log.info("receive an redis channel={},message={}", topic, message);
        if (iotCmdAdd.equals(topic)) {
            Command command = JSON.parseObject(message.toString(), Command.class);
            if (command != null && !StringUtils.isEmpty(command.getSn())) {
                // 先缓存到本地
                commandManager.set(command.getSn(), command);
                // 如果设备在线并无积压指令才下发指令
                Connection connection = connectionManager.get(command.getSn());
                if (connection != null) {
                    Packet packet = Packet.builder().sn(command.getSn()).build();
                    CommandVo commandVo = new CommandVo();
                    commandVo.setPacket(packet);
                    commandVo.setConnection(connection);
                    commandTask.send(commandVo);
                }
            }
        } else if (iotCmdRemove.equals(topic)) {
            Command command = JSON.parseObject(message.toString(), Command.class);
            if (command != null && !StringUtils.isEmpty(command.getSn())) {
                commandManager.removeCommand(command);
            }
        }
    }

}
