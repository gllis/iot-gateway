package com.gllis.gateway.server.core.manager;

import com.gllis.gateway.server.core.factory.HandlerFactory;
import com.gllis.gateway.server.core.factory.SendCmdHandlerFactory;
import com.gllis.gateway.server.core.handler.CommandProcessing;
import com.gllis.gateway.server.core.handler.ProtocolProcessing;
import com.gllis.gateway.server.domain.Command;
import com.gllis.gateway.server.domain.Packet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 包管理器
 *
 * @author glli
 * @date 2023/8/15
 */
@Component
public class PackageManager implements ApplicationContextAware, DisposableBean {
    private ApplicationContext ctx;

    @Autowired
    private HandlerFactory handlerFactory;
    @Autowired
    private SendCmdHandlerFactory sendCmdHandlerFactory;

    @PostConstruct
    public void init() {
        handlerFactory.init(ctx);
        sendCmdHandlerFactory.init(ctx);
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

    /**
     * 取得指令下处理
     *
     * @param command
     * @return
     */
    public CommandProcessing getCommandProcessing(Command command) {
        return sendCmdHandlerFactory.getHandler(command);
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
