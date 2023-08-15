package com.gllis.gateway.server.main.job;

import lombok.extern.slf4j.Slf4j;

/**
 * 启动job
 *
 * @author glli
 * @date 2023/8/15
 */
@Slf4j
public abstract class BootJob {

    protected BootJob next;
    protected abstract void start();
    protected abstract void stop();

    public void startNext() {
        if (next != null) {
            log.info("start bootstrap job {}", getNextName());
            next.start();
        }
    }

    public void stopNext() {
        if (next != null) {
            log.info("stopped bootstrap job {}", getNextName());
            next.stop();
        }
    }

    public BootJob setNext(BootJob next) {
        this.next = next;
        return next;
    }


    protected String getNextName() {
        return next.getName();
    }

    protected String getName() {
        return this.getClass().getSimpleName();
    }
}
