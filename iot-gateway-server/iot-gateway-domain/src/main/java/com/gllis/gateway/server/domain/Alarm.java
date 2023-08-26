package com.gllis.gateway.server.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 报警状态
 *
 * @author glli
 * @date 2023/8/26
 */
@Data
public class Alarm implements Serializable {
    private static final long serialVersionUID = 1146648064290216555L;
    /**
     * 低电报警;0:否;1:是;
     */
    private Integer lowPower;
    /**
     * 断电报警;0:否;1:是;
     */
    private Integer powerOff;
}
