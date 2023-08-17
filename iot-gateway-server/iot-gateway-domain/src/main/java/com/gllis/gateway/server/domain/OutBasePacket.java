package com.gllis.gateway.server.domain;

import com.gllis.gateway.server.enums.ProtocolEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据包基类
 *
 * @author glli
 * @date 2023/8/17
 */
@Data
public class OutBasePacket implements Serializable {
    private static final long serialVersionUID = -1085290292672137226L;

    private String sn;             // 设备号
    private Integer deviceId;      // 设备id
    private Long time;             // 设备上报时间
    private Long createTime;       // 接收时间
    private String model;          // 型号
    private ProtocolEnum protocol;  // 协议

    public OutBasePacket() {
        this.createTime = System.currentTimeMillis();
    }
}
