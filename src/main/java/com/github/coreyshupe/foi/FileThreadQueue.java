package com.github.coreyshupe.foi;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileThreadQueue extends Thread {
    @NotNull @Getter(AccessLevel.PROTECTED) private final static FileThreadQueue instance = new FileThreadQueue();
    private static boolean threadStarted = false;

    protected static void startThread() {
        if (threadStarted) return;
        threadStarted = true;
        instance.setDaemon(true);
        instance.start();
    }

    @NotNull protected final static Runnable EMPTY = () -> {
    };
    @NotNull private final BlockingQueue<FileRequest> fileRequestQueue = new LinkedBlockingQueue<>();

    @Override public void run() {
        while (true) {
            try {
                FileRequest request = fileRequestQueue.take();
                if (request.debug) System.out.println("Starting write of: " + request.file.getAbsolutePath());
                try (FileOutputStream stream = new FileOutputStream(request.file)) {
                    try (FileObjectInjector injector = new FileObjectInjector(request.injector, stream.getChannel())) {
                        if (request.debug) System.out.println("Writing: " + request.object);
                        boolean result = injector.write(request.object);
                        if (request.debug) System.out.println("Write result: " + result);
                    } catch (IOException ex) {
                        if (request.debug) ex.printStackTrace();
                    }
                } catch (IOException ex) {
                    if (request.debug) ex.printStackTrace();
                }
                request.callback.run();
            } catch (InterruptedException e) {
                return; // we just assume the thread stopped
            }
        }
    }

    public void queueRequest(@NotNull FileRequest request) {
        fileRequestQueue.add(request);
    }

    @Builder public final static class FileRequest {
        @NotNull private final File file;
        @NotNull private final ChannelObjectInjector injector;
        @NotNull private final Object object;
        @NotNull private final Runnable callback;
        @Builder.Default private final boolean debug = false;
    }
}