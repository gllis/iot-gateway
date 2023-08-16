package com.gllis.gateway.server.core.constant;

/**
 * 程序常量
 *
 * @author glli
 * @date 2023/8/14
 */
public interface SysConstant {
    // netty 解码器无效位数
    int PACKET_SIZE_INVALID = -1;
    Byte COMMAND_SEND_NUMBER = 3;

    // 设备上行
    int DEVICE_LOG_PACKET_UP = 1;
    // 设备下行
    int DEVICE_LOG_PACKET_DOWN = 2;
}
