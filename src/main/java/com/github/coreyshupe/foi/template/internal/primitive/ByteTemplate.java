package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ByteTemplate extends SizedTemplate<Byte> {
    @Getter private final static ByteTemplate instance = new ByteTemplate();

    private ByteTemplate() {
        super(Byte.class, Byte.BYTES);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == byte.class;
    }

    @Override public void writeToBuffer(@NotNull Byte object, @NotNull ByteBuffer buffer) {
        buffer.put(object);
    }

    @NotNull @Override public Byte read(ByteBuffer buffer) {
        return buffer.get();
    }
}
