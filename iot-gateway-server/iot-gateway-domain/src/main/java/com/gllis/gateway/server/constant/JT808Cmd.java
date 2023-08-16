package com.gllis.gateway.server.constant;

/**
 * JT808协议指令类型
 *
 * @author glli
 * @date 2023/8/15
 */
public interface JT808Cmd {
    // 终端应答
    String DEVICE_RESPONSE = "0001";
    // 终端注册
    String REGISTER = "0100";
    // 终端鉴权
    String AUTHENTICATION = "0102";
    // 心跳
    String HEARTBEAT = "0002";
    // 位置
    String POSITION = "0200";


}
