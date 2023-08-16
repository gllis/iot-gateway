package com.gllis.gateway.server.constant;

/**
 * Redis 缓存key 前缀
 *
 * @author glli
 * @created 2023/8/16
 */
public interface RedisKeyConstant {
    /**
     * 设备信息 通过ID查询
     */
    String PRE_DEVICE_INFO = "IOT:DEVICE:ID:";
    /**
     * 设备类型
     */
    String PRE_DEVICE_TYPE = "IOT:DEVICE:TP:";
    /**
     * 设备信息 通过SN查询
     */
    String PRE_DEVICE_SN = "IOT:DEVICE:SN:";

    /**
     * 设备类型
     */
    String PRE_DEVICE_TP = "IOT:DEVICE:TP:";

    /**
     * 所有设备型号
     */
    String PRE_DEVICE_TP_ALL = "IOT:DEVICE:TP:ALL";


    /**
     * 下行命令
     */
    String SENT_DOWN_COMMAND = "IOT:SDC:";

    /**
     * 所有下行命令
     */
    String SYN_SENT_DOWN_COMMAND = "IOT:SYN_SDC_ALL";


}
