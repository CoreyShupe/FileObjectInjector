package com.github.coreyshupe.foi;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
    @NotNull private final BlockingQueue<IFileRequest> fileRequestQueue = new LinkedBlockingQueue<>();

    @Override public void run() {
        while (true) {
            try {
                IFileRequest request = fileRequestQueue.take();
                if (request.isDebug()) System.out.println("Starting write of: " + request.getFile().getAbsolutePath());
                try (FileObjectInjector injector = FileObjectInjector.fromFile(request.getFile(), request.getInjector())) {
                    boolean result = request.writeToInjector(injector);
                    if (request.isDebug()) System.out.println("Write result: " + result);
                } catch (IOException ex) {
                    if (request.isDebug()) ex.printStackTrace();
                }
                request.getCallback().run();
            } catch (InterruptedException e) {
                return; // we just assume the thread stopped
            }
        }
    }

    public void queueRequest(@NotNull IFileRequest request) {
        fileRequestQueue.add(request);
    }

    public interface IFileRequest {
        @NotNull File getFile();

        @NotNull ObjectInjector getInjector();

        @NotNull Runnable getCallback();

        boolean isDebug();

        boolean writeToInjector(@NotNull FileObjectInjector injector) throws IOException;

        void wrapCallback(@NotNull final Runnable callback);
    }

    @Builder @Getter public final static class FileRequest implements IFileRequest {
        @NotNull private final File file;
        @Builder.Default @NotNull private final ObjectInjector injector = ObjectInjector.getDefaultInstance();
        @NotNull private final Object object;
        @Builder.Default @NotNull private Runnable callback = EMPTY;
        @Builder.Default private final boolean debug = false;

        public void wrapCallback(@NotNull final Runnable callback) {
            if (this.callback == EMPTY) {
                this.callback = callback;
            } else {
                Runnable temp = this.callback;
                this.callback = () -> {
                    callback.run();
                    temp.run();
                };
            }
        }

        @Override public boolean writeToInjector(@NotNull FileObjectInjector injector) throws IOException {
            return injector.write(object);
        }
    }

    @Builder @Getter public final static class CollectionFileRequest<T> implements IFileRequest {
        @NotNull private final Class<T> type;
        @NotNull private final File file;
        @Builder.Default @NotNull private final ObjectInjector injector = ObjectInjector.getDefaultInstance();
        @NotNull private final Collection<T> object;
        @Builder.Default @NotNull private Runnable callback = EMPTY;
        @Builder.Default private final boolean debug = false;

        public void wrapCallback(@NotNull final Runnable callback) {
            if (this.callback == EMPTY) {
                this.callback = callback;
            } else {
                Runnable temp = this.callback;
                this.callback = () -> {
                    callback.run();
                    temp.run();
                };
            }
        }

        @Override public boolean writeToInjector(@NotNull FileObjectInjector injector) throws IOException {
            return injector.writeCollection(type, object);
        }
    }
}