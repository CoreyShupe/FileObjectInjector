package com.github.coreyshupe.foi;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.coreyshupe.foi.FileThreadQueue.EMPTY;

@SuppressWarnings("unused") public class FileThreadCommunicator {
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

    public <T> void queueRequest(@NotNull Class<T> type, @NotNull File file, @NotNull Collection<T> object) {
        if (closed) return;
        queueRequest(file, object, ObjectInjector.getDefaultInstance());
    }

    public <T> void queueRequest(@NotNull Class<T> type, @NotNull File file, @NotNull Collection<T> object, @NotNull ObjectInjector injector) {
        if (closed) return;
        queueRequest(FileThreadQueue.CollectionFileRequest.<T>builder()
                .type(type)
                .file(file)
                .object(object)
                .injector(injector)
                .callback(EMPTY)
                .build());
    }

    public void queueRequest(FileThreadQueue.IFileRequest request) {
        inQueue.incrementAndGet();
        request.wrapCallback(innerCallback);
        FileThreadQueue.getInstance().queueRequest(request);
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
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
