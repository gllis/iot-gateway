package com.gllis.gateway.server.core.manager;

import com.gllis.gateway.server.core.factory.HandlerFactory;
import com.gllis.gateway.server.core.handler.ProtocolProcessing;
import com.gllis.gateway.server.domain.Packet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class PackageManager implements ApplicationContextAware, DisposableBean {
    private ApplicationContext ctx;

    @Autowired
    private HandlerFactory handlerFactory;

    @PostConstruct
    public void init() {
        handlerFactory.init(ctx);
    }

    /**
     * 取得消息处理
     *
     * @param packet
     * @return
     */
    public ProtocolProcessing getMessageHandler(Packet packet) {
        return handlerFactory.getHandler(packet);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

    @Override
    public void destroy() throws Exception {
        this.ctx = null;
    }
}
