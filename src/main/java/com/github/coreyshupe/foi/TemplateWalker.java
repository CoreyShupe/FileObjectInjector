package com.github.coreyshupe.foi;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

@FunctionalInterface
public interface TemplateWalker {
    @NotNull ByteBuffer readSizeOf(int size) throws IOException;
}
