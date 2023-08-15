package com.gllis.gateway.server.enums;

/**
 * 协议类型
 *
 * @author glli
 * @date 2023/8/14
 */
public enum ProtocolEnum {

    JT808("JT808", "部标JT/T808协议");

    public final String value;
    public final String desc;

    ProtocolEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
