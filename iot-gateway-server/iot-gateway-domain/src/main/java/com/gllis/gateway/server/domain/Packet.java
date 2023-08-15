package com.gllis.gateway.server.domain;

import com.gllis.gateway.server.enums.ProtocolEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 协议包
 * @author glli
 * @date 2023/8/14
 */
@Data
@Builder
public class Packet implements Serializable {

    private static final long serialVersionUID = 6619137478059504425L;

    // 协议号
    private String cmd;
    // 设备编号
    private String sn;
    // 设备型号
    private String model;
    // 设备ID
    private Integer deviceId;
    // 协议类型
    private ProtocolEnum protocolEnum;
    // 报文
    private byte[] body;
}
