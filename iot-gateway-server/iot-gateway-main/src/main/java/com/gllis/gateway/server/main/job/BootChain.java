package com.gllis.gateway.server.main.job;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public final class BootChain {

    private final BootJob boot = new BootJob() {

        @Override
        protected void start() {
            log.info("bootstrap chain staring...");
            startNext();
        }

        @Override
        protected void stop() {
            stopNext();
            log.info("bootstrap chain stopped.");
        }
    };

    private BootJob last = boot;
    public static BootChain chain() {
        return new BootChain();
    }

    public BootChain boot() {
        return this;
    }

    public void start() {
        boot.start();
    }

    public void stop() {
        boot.stop();
    }

    public void end() {
        setNext(new BootJob() {

            @Override
            protected void start() {
                log.info("bootstrap chain started end.");
            }

            @Override
            protected void stop() {
                log.info("bootstrap chain stopped end.");
            }
        });
    }

    public BootChain setNext(BootJob bootJob) {
        this.last = last.setNext(bootJob);
        return this;
    }


    public BootChain setNext(BootJob bootJob, boolean enabled) {
        if (enabled) {
            this.last = last.setNext(bootJob);
        }
        return this;
    }
}
