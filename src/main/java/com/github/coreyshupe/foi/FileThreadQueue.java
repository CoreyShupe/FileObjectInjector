package com.github.coreyshupe.foi;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

    @NotNull public final static Runnable EMPTY = () -> {
    };
    @NotNull private final BlockingQueue<FileRequest> fileRequestQueue = new LinkedBlockingQueue<>();

    @Override public void run() {
        while (true) {
            try {
                FileRequest request = fileRequestQueue.take();
                if (request.debug) System.out.println("Starting write of: " + request.file.getAbsolutePath());
                try (FileObjectInjector injector = FileObjectInjector.fromFile(request.file, request.injector)) {
                    if (request.debug) System.out.println("Writing: " + request.object);
                    boolean result = injector.write(request.object);
                    if (request.debug) System.out.println("Write result: " + result);
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

    @Builder @Getter public final static class FileRequest {
        @NotNull private final File file;
        @Builder.Default @NotNull private final ObjectInjector injector = ObjectInjector.getDefaultInstance();
        @NotNull private final Object object;
        @Builder.Default @NotNull private final Runnable callback = EMPTY;
        @Builder.Default private final boolean debug = false;

        public static FileRequest wrapCallback(final FileRequest fileRequest, final Runnable callback) {
            if (fileRequest.callback == EMPTY) {
                return FileRequest.builder()
                        .file(fileRequest.file)
                        .injector(fileRequest.injector)
                        .object(fileRequest.object)
                        .callback(callback)
                        .debug(fileRequest.debug)
                        .build();
            } else {
                return FileRequest.builder()
                        .file(fileRequest.file)
                        .injector(fileRequest.injector)
                        .object(fileRequest.object)
                        .callback(() -> {
                            callback.run();
                            fileRequest.callback.run();
                        })
                        .debug(fileRequest.debug)
                        .build();
            }
        }
    }
}