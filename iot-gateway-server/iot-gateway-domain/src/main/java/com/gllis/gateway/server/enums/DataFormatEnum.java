package com.gllis.gateway.server.enums;

public enum DataFormatEnum {
    ASCCII("ASCCII", "ASCCII"),
    HEX("HEX", "HEX");

    public final String value;
    public final String name;

    DataFormatEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
