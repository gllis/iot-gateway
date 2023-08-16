package com.gllis.gateway.server.core.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.gllis.gateway.server.constant.RedisKeyConstant;
import com.gllis.gateway.server.core.constant.SysConstant;
import com.gllis.gateway.server.domain.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 命令管理
 *
 * @author glli
 * @date 2023/8/16
 */
@Slf4j
@Component
public class CommandManager {

    @Autowired
    private RedisCacheManager redisCacheManager;

    public static Map<String, CopyOnWriteArrayList<Command>> commandList = new ConcurrentHashMap<>();
    //指令发送次数
    public static Map<String,Byte> commandSendCountMap = new ConcurrentHashMap<>();
    private static final String CMD_KEY = "{0}_{1}_{2}";
    public void addCmdSendNum(Command c) {
        String key = MessageFormat.format(CMD_KEY, c.getSn(), c.getCmdType(), c.getMold());
        Byte num = commandSendCountMap.get(key);
        if (null == num) {
            num = 0;
        }
        num = (byte) (num + 1);
        // 超过次数，删除指令
        if (num >= SysConstant.COMMAND_SEND_NUMBER) {
            // 通知服务指令发送失败

            commandSendCountMap.remove(key);
            removeCommand(c);
        } else {
            commandSendCountMap.put(key, num);
        }
    }

    public void set(String key, Object value) {
        if (null != value) {
            try {
                Command command = (Command) value;
                CopyOnWriteArrayList<Command> list = commandList.get(RedisKeyConstant.SENT_DOWN_COMMAND + key);
                log.debug("load local cache command list key={}, sn={}, model={}, commandtype={}",
                        (RedisKeyConstant.SENT_DOWN_COMMAND + key), command.getSn(), command.getModel(), command.getCmdType());
                log.debug("command list size: " + (list == null ? 0 : list.size()));
                if (CollectionUtils.isEmpty(list)) {
                    String temp = redisCacheManager.get(RedisKeyConstant.SENT_DOWN_COMMAND + key);
                    list = JSONObject.parseObject(temp, new TypeReference<CopyOnWriteArrayList<Command>>() {
                    });
                }

                if (!CollectionUtils.isEmpty(list)) {
                    Iterator<Command> iter = list.iterator();
                    while (iter.hasNext()) {
                        Command c = iter.next();
                        if (c.getCmdType().intValue() == command.getCmdType().intValue()
                                && c.getMold().intValue() == command.getMold().intValue()) {
                            list.remove(c);
                        }
                    }
                }
                if (CollectionUtils.isEmpty(list)) {
                    list = new CopyOnWriteArrayList<>();
                }
                list.add(command);
                commandList.put(RedisKeyConstant.SENT_DOWN_COMMAND + key, list);
                commandSendCountMap.remove(command.getSn() + "_" + command.getCmdType() + "_" + command.getMold());
                log.info("本地指令缓存数量:" + commandList.size() + "");
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public CopyOnWriteArrayList<Command> getCommandList(String key) {
        CopyOnWriteArrayList<Command> commandlist = commandList.get(key);
        return commandlist;
    }

    public Command getCommand(String sn, int type, int mold) {
        CopyOnWriteArrayList<Command> commandlist = this.getCommandList(RedisKeyConstant.SENT_DOWN_COMMAND + sn);
        Command cm = null;
        if (!CollectionUtils.isEmpty(commandlist)) {
            for (Command c : commandlist) {
                if (c.getSn().equals(sn) && c.getCmdType().intValue() == type
                        && c.getMold().intValue() == mold) {
                    cm = c;
                    break;
                }
            }
        }
        return cm;
    }

    public Command getCommand(String sn, int type) {
        CopyOnWriteArrayList<Command> commandlist = this.getCommandList(RedisKeyConstant.SENT_DOWN_COMMAND + sn);
        Command cm = null;
        if (!CollectionUtils.isEmpty(commandlist)) {
            for (Command c : commandlist) {
                if (c.getSn().equals(sn) && c.getCmdType().intValue() == type) {
                    cm = c;
                    break;
                }
            }
        }
        return cm;
    }

    public Command getCommand(String sn) {
        CopyOnWriteArrayList<Command> commandlist = this.getCommandList(RedisKeyConstant.SENT_DOWN_COMMAND + sn);
        Command cm = null;
        if (!CollectionUtils.isEmpty(commandlist)) {
            for (Command c : commandlist) {
                cm = c;
                break;
            }
        }
        return cm;
    }

    public Command getCommand(String sn, Long cmdId) {
        CopyOnWriteArrayList<Command> commandlist = this.getCommandList(RedisKeyConstant.SENT_DOWN_COMMAND + sn);
        Command cm = null;
        if (!CollectionUtils.isEmpty(commandlist)) {
            for (Command c : commandlist) {
                if (c.getId().equals(cmdId)) {
                    cm = c;
                    break;
                }
            }
        }
        return cm;
    }


    public void removeCommand(Command c) {
        if (c == null) {
            return;
        }
        removeCommandFromMap(c);

        removeCommandFromRedis(c);

        redisCacheManager.publish("", JSON.toJSONString(c));
    }

    private void removeCommandFromMap(Command c) {
        CopyOnWriteArrayList<Command> commandlist = commandList.get(RedisKeyConstant.SENT_DOWN_COMMAND + c.getSn());
        if (CollectionUtils.isEmpty(commandlist)) {
            return;
        }

        String cKey = MessageFormat.format(CMD_KEY, c.getSn(), c.getCmdType(), c.getMold());
        for (Command command : commandlist) {
            String tempKey = MessageFormat.format(CMD_KEY, command.getSn(), command.getCmdType(), command.getMold());
            if (cKey.equals(tempKey)) {
                commandlist.remove(command);
                break;
            }
        }

        if (CollectionUtils.isEmpty(commandlist)) {
            commandList.remove(RedisKeyConstant.SENT_DOWN_COMMAND + c.getSn());
        }
    }

    private void removeCommandFromRedis(Command c) {
        String temp = redisCacheManager.get(RedisKeyConstant.SENT_DOWN_COMMAND + c.getSn());
        if (StringUtils.isNotBlank(temp)) {
            CopyOnWriteArrayList<Command> commandlist = JSONObject.parseObject(temp, new TypeReference<CopyOnWriteArrayList<Command>>() {
            });
            if (CollectionUtils.isEmpty(commandlist)) {
                return;
            }
            String cKey = MessageFormat.format(CMD_KEY, c.getSn(), c.getCmdType(), c.getMold());
            for (Command command : commandlist) {
                String tempKey =  MessageFormat.format(CMD_KEY, command.getSn(), command.getCmdType(), command.getMold());
                if (cKey.equals(tempKey)) {
                    commandlist.remove(command);
                    break;
                }
            }
            if (CollectionUtils.isEmpty(commandlist)) {
                redisCacheManager.del(RedisKeyConstant.SENT_DOWN_COMMAND + c.getSn());
                redisCacheManager.delSetKey(RedisKeyConstant.SYN_SENT_DOWN_COMMAND, c.getSn());
            } else {
                redisCacheManager.set(RedisKeyConstant.SENT_DOWN_COMMAND + c.getSn(), JSONObject.toJSONString(commandlist));
            }
        }
    }
}
