package com.github.coreyshupe.foi.template;

import com.github.coreyshupe.foi.TemplateWalker;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class SizedTemplate<T> extends ExactTemplate<T> {
    @Getter private final int size;

    public SizedTemplate(@NotNull Class<T> clazz, int size) {
        super(clazz);
        this.size = size;
    }

    @Override public int sizeOf(@NotNull T object) {
        return size;
    }

    @Override public @NotNull T readFromWalker(@NotNull TemplateWalker walker) throws IOException {
        return read(walker.readSizeOf(getSize()));
    }

    @NotNull public abstract T read(ByteBuffer buffer);
}
