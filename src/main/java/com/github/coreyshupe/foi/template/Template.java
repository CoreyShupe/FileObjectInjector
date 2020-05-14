package com.github.coreyshupe.foi.template;

import com.github.coreyshupe.foi.TemplateWalker;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Template<T> {
    public abstract boolean isThis(@NotNull Class<?> givenType);

    public abstract int sizeOf(@NotNull T object);

    public abstract void writeToBuffer(@NotNull T object, @NotNull ByteBuffer buffer);

    @NotNull public abstract T readFromWalker(@NotNull TemplateWalker walker) throws IOException;
}