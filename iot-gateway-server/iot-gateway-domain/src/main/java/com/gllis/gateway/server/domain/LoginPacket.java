package com.gllis.gateway.server.domain;


import lombok.Data;

/**
 * 登录
 *
 * @author glli
 * @date 2023/8/17
 */
@Data
public class LoginPacket extends OutBasePacket {
    private static final long serialVersionUID = 3652961026710357362L;
    // 版本
    private String version;
}
