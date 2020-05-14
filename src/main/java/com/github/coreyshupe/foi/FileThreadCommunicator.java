package com.github.coreyshupe.foi;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.coreyshupe.foi.FileThreadQueue.EMPTY;

public class FileThreadCommunicator {
    private final AtomicInteger inQueue;
    private boolean closed;
    private volatile boolean stalled;

    public FileThreadCommunicator() {
        closed = false;
        inQueue = new AtomicInteger(0);
        stalled = false;
        FileThreadQueue.startThread();
    }

    public void queueRequest(@NotNull File file, @NotNull ChannelObjectInjector injector, @NotNull Object object) {
        if (closed) return;
        queueRequest(file, injector, object, EMPTY);
    }

    public void queueRequest(@NotNull File file, @NotNull ChannelObjectInjector injector, @NotNull Object object, boolean debug) {
        if (closed) return;
        queueRequest(file, injector, object, EMPTY, debug);
    }

    public void queueRequest(@NotNull File file, @NotNull ChannelObjectInjector injector, @NotNull Object object, @NotNull Runnable callback) {
        if (closed) return;
        queueRequest(file, injector, object, callback, false);
    }

    public void queueRequest(@NotNull File file, @NotNull ChannelObjectInjector injector, @NotNull Object object, @NotNull Runnable callback, boolean debug) {
        if (closed) return;
        inQueue.incrementAndGet();
        FileThreadQueue.getInstance().queueRequest(
                FileThreadQueue.FileRequest.builder()
                        .file(file)
                        .injector(injector)
                        .object(object)
                        .callback(wrapCallback(callback))
                        .debug(debug)
                        .build()
        );
    }

    public Runnable wrapCallback(Runnable runnable) {
        return () -> {
            if (inQueue.decrementAndGet() == 0 && stalled) {
                synchronized (FileThreadCommunicator.this) {
                    notify();
                }
            }
            runnable.run();
        };
    }

    public void close() {
        closed = true;
    }

    public int size() {
        return inQueue.get();
    }

    public void stall() {
        close();
        if (size() == 0) {
            return;
        }
        stalled = true;
        while (true) {
            synchronized (this) {
                try {
                    wait();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
