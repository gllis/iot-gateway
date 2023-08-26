package com.gllis.gateway.server.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 状态
 *
 * @author glli
 * @date 2023/8/26
 */
@Data
public class Status implements Serializable {
    private static final long serialVersionUID = -1840329075511686107L;

    private Integer acc;
    private Integer gps;
    private Integer lbs;
    private Integer wifi;

    public Status() {
        this.acc = 0;
        this.gps = 0;
        this.lbs = 0;
        this.wifi = 0;
    }
}
