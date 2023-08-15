package com.gllis.gateway.server.main.job;

import com.gllis.gateway.server.core.listener.Listener;
import com.gllis.gateway.server.core.service.BaseService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author glli
 * @date 2023/8/15
 */
@Slf4j
public final class ServerBoot extends BootJob {
    private final BaseService service;

    public ServerBoot(BaseService service) {
        this.service = service;
    }

    @Override
    protected void start() {
        service.init();
        service.start(new Listener() {
            @Override
            public void onSuccess(Object... args) {
                log.debug("start {} success on:{}", service.getClass().getSimpleName(), args);
                startNext();
            }

            @Override
            public void onFailure(Throwable cause) {
                log.error("start {} failure, jvm exit with code -1", service.getClass().getSimpleName(), cause);
                System.exit(-1);
            }
        });
    }

    @Override
    protected void stop() {
        stopNext();
        log.info("try shutdown {}...", service.getClass().getSimpleName());
        service.stop().join();
        log.info("{} shutdown success.", service.getClass().getSimpleName());
    }
}
