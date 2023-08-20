package com.gllis.gateway.server.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * sn
 *
 * @author glli
 * @date 2023/8/20
 */
@Data
public class SnModel implements Serializable {
    private static final long serialVersionUID = 7277041462024605552L;
    private Integer id;
    private Integer deviceId;
    private String sn;
    private String model;
    private String protocol;

}
