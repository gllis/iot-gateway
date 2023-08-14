package com.gllis.gateway.server.core.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 命令常量
 *
 * @author glli
 * @date 2023/8/14
 */
public class CmdConstant {
    public static final Map<String, CopyOnWriteArrayList> commandList = new ConcurrentHashMap<>();
    public static Map<String, Byte> commandSendCountMap = new ConcurrentHashMap<>();
    public static final int COMMAND_SEND_NUMBER = 3;
}
