package com.gllis.gateway.server.enums;

/**
 * 指令下发类型
 *
 * @author glli
 * @date 2023/8/16
 */
public enum SendCommandTypeEnum {
    CUSTOM(1, "自定义指令");

    public final int value;
    public final String desc;
    SendCommandTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
