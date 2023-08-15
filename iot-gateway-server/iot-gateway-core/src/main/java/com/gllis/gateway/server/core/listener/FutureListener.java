package com.gllis.gateway.server.core.listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听器
 *
 * @author glli
 * @date 2023/8/15
 */
public class FutureListener extends CompletableFuture<Boolean> implements Listener {
    private final Listener listener;
    private final AtomicBoolean started;

    public FutureListener(AtomicBoolean started) {
        this.started = started;
        this.listener = null;
    }

    public FutureListener(Listener listener, AtomicBoolean started) {
        this.listener = listener;
        this.started = started;
    }

    @Override
    public void onSuccess(Object... args) {
        if (isDone()) {
            return;
        }
        complete(started.get());
        if (listener != null) {
            listener.onSuccess(args);
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        if (isDone()) {
            return;
        }
        completeExceptionally(cause);
        if (listener != null) {
            listener.onFailure(cause);
        }
        throw new RuntimeException(cause);
    }
}
