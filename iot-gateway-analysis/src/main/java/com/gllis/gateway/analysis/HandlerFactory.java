package com.gllis.gateway.analysis;

import com.gllis.gateway.server.domain.Packet;

/**
 *  处理工厂
 *
 * @author GL
 * @created 2023/8/17
 */
public interface HandlerFactory {

    /**
     * 获取对应的处理类
     *
     * @param packet
     * @return
     */
    ProtocolProcessing getHandler(Packet packet);
}
