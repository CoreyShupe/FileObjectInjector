package com.github.coreyshupe.foi;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.coreyshupe.foi.FileThreadQueue.EMPTY;

public class FileThreadCommunicator {
    private final AtomicInteger inQueue;
    private boolean closed;
    private volatile boolean stalled;
    private final Runnable innerCallback;

    public FileThreadCommunicator() {
        closed = false;
        inQueue = new AtomicInteger(0);
        stalled = false;
        innerCallback = this::update;
        FileThreadQueue.startThread();
    }

    public void queueRequest(@NotNull File file, @NotNull Object object) {
        if (closed) return;
        queueRequest(file, object, ObjectInjector.getDefaultInstance());
    }

    public void queueRequest(@NotNull File file, @NotNull Object object, @NotNull ObjectInjector injector) {
        if (closed) return;
        queueRequest(FileThreadQueue.FileRequest.builder()
                .file(file)
                .object(object)
                .injector(injector)
                .callback(EMPTY)
                .build());
    }

    public void queueRequest(FileThreadQueue.FileRequest request) {
        inQueue.incrementAndGet();
        FileThreadQueue.getInstance().queueRequest(FileThreadQueue.FileRequest.wrapCallback(request, innerCallback));
    }

    private void update() {
        if (inQueue.decrementAndGet() == 0 && stalled) {
            synchronized (FileThreadCommunicator.this) {
                notify();
            }
        }
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
