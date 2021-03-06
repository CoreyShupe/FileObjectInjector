package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LongTemplate extends SizedTemplate<Long> {
    @Getter private final static LongTemplate instance = new LongTemplate();

    private LongTemplate() {
        super(Long.class, Long.BYTES);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == long.class;
    }

    @Override public void writeToBuffer(@NotNull Long object, @NotNull ByteBuffer buffer) {
        buffer.putLong(object);
    }

    @NotNull @Override public Long read(ByteBuffer buffer) {
        return buffer.getLong();
    }
}
