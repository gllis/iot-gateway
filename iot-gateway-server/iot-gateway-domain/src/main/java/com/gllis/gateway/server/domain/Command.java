package com.gllis.gateway.server.domain;

import com.gllis.gateway.server.enums.ProtocolEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 命令
 *
 * @author glli
 * @date 2023/8/15
 */
@Data
public class Command implements Serializable {
    private static final long serialVersionUID = 9194516593421109411L;

    private Integer id;
    private String sn;
    private Integer cmdType;
    private String content;
    private String model;
    private Integer mold;
    private ProtocolEnum protocolEnum;
}
