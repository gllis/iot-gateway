package com.gllis.gateway.server.domain;


import lombok.Data;

/**
 * 位置
 *
 * @author glli
 * @date 2023/8/17
 */
@Data
public class PositionPacket extends OutBasePacket {
    private static final long serialVersionUID = 3652961026710357362L;
    private Alarm alarm;
    private Status status;
    private Double lat;
    private Double lon;
    private Short altitude;
    private Short speed;
    private Short direction;
}
