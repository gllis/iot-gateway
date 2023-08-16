package com.gllis.gateway.server.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备日志
 *
 * @author glli
 * @date 2023/8/11
 */
@Data
@NoArgsConstructor
public class DeviceLog implements Serializable {
    private String sn;
    private int type;    // 1:设备上传; 2：网关下传
    private String msg;
    private String hosts;
    private Date createTime;

    public DeviceLog(String sn, int type, String msg, String hosts) {
        this.sn = sn;
        this.type = type;
        this.msg = msg;
        this.hosts = hosts;
        this.createTime = new Date();
    }
}
