package com.gllis.gateway.server.core.factory;

import com.gllis.gateway.server.annotation.SendCmdHandler;
import com.gllis.gateway.server.core.handler.CommandProcessing;
import com.gllis.gateway.server.domain.Command;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.enums.SendCommandTypeEnum;
import com.gllis.gateway.server.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 发送消息消息处理工厂
 *
 * @author glli
 * @date 2023/8/16
 */
@Slf4j
@Component
public class SendCmdHandlerFactory {

    private static final String HANDLER_KEY = "{0}-{1}";

    private static Map<String, CommandProcessing> handlerMap = new HashMap<>();

    public void init(ApplicationContext ctx) {
        // 获取所有的处理实现类
        Map<String, CommandProcessing> beansMap = ctx.getBeansOfType(CommandProcessing.class);
        if (beansMap == null || beansMap.isEmpty()) {
            log.error("找不到处理实现类");
            throw new ServiceException("找不到处理实现类");
        }
        log.info("一共找到 {} 个handler类.", beansMap.size());
        Iterator<Map.Entry<String, CommandProcessing>> it = beansMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CommandProcessing> en = it.next();
            CommandProcessing handler = en.getValue();
            // 从注解中解析所处理的设备类型
            ProtocolEnum pType;
            SendCommandTypeEnum cmdType;
            try {
                SendCmdHandler sendCmdHandler = handler.getClass().getAnnotation(SendCmdHandler.class);
                if (sendCmdHandler == null) {
                    continue;
                }
                pType = sendCmdHandler.protocol();
                cmdType = sendCmdHandler.cmdType();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }

            if (pType == null) {
                continue;
            }
            if (cmdType == null) {
                continue;
            }

            handlerMap.put(MessageFormat.format(HANDLER_KEY, pType.value, cmdType.value), handler);
        }
    }


    /**
     * 获取对应协议的Handler
     *
     * @return
     */
    public CommandProcessing getHandler(Command command) {
        return handlerMap.get(MessageFormat.format(HANDLER_KEY, command.getProtocolEnum().value, command.getCmdType()));
    }
}
