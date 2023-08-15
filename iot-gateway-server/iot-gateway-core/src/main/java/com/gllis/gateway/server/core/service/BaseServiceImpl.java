package com.gllis.gateway.server.core.service;


import com.gllis.gateway.server.core.listener.FutureListener;
import com.gllis.gateway.server.core.listener.Listener;
import com.gllis.gateway.server.exception.ServiceException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础服务
 *
 * @author glli
 * @date 2023/8/15
 */
public abstract class BaseServiceImpl implements BaseService {

    protected final AtomicBoolean started = new AtomicBoolean();

    protected void tryStart(Listener l, FunctionEx func) {
        FutureListener listener = wrap(l);
        if (started.compareAndSet(false, true)) {
            try {
                func.apply(listener);
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            if (throwIfStarted()) {
                listener.onFailure(new ServiceException("service already started."));
            } else {
                listener.onSuccess();
            }
        }
    }

    protected void tryStop(Listener l, FunctionEx func) {
        FutureListener listener = wrap(l);
        if (started.compareAndSet(true, false)) {
            try {
                func.apply(listener);
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            if (throwIfStopped()) {
                listener.onFailure(new ServiceException("service already stopped."));
            } else {
                listener.onSuccess();
            }
        }
    }

    @Override
    public CompletableFuture<Boolean> start() {
        FutureListener listener = new FutureListener(started);
        start(listener);
        return listener;
    }

    @Override
    public CompletableFuture<Boolean> stop() {
        FutureListener listener = new FutureListener(started);
        stop(listener);
        return listener;
    }

    @Override
    public boolean syncStart() {
        return start().join();
    }

    @Override
    public boolean syncStop() {
        return stop().join();
    }

    @Override
    public void start(Listener listener) {
        tryStart(listener, this::doStart);
    }

    @Override
    public void stop(Listener listener) {
        tryStart(listener, this::doStop);
    }

    protected void doStart(Listener listener) throws Throwable {
        listener.onSuccess();
    }

    protected void doStop(Listener listener) throws Throwable {
        listener.onSuccess();
    }


    /**
     * 控制当服务已经启动后，重复调用start方法，是否抛出已启动异常
     * 默认是true
     *
     * @return true 抛出异常
     */
    protected boolean throwIfStarted() {
        return true;
    }



    /**
     * 控制当服务已经停止后，重复调用stop方法，是否抛出已启动异常
     * 默认是true
     *
     * @return true 抛出异常
     */
    protected boolean throwIfStopped() {
        return true;
    }

    protected interface FunctionEx {
        void apply(Listener l) throws Throwable;
    }

    private FutureListener wrap(Listener l) {
        if (null == l) {
            return new FutureListener(started);
        }
        if (l instanceof FutureListener) {
            return (FutureListener) l;
        }
        return new FutureListener(l, started);
    }
}
