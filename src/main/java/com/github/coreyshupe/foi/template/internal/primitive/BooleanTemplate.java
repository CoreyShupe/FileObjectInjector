package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BooleanTemplate extends SizedTemplate<Boolean> {
    @Getter private final static BooleanTemplate instance = new BooleanTemplate();

    public BooleanTemplate() {
        super(Boolean.class, Byte.SIZE);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == boolean.class;
    }

    @NotNull @Override public Boolean read(ByteBuffer buffer) {
        return buffer.get() != 0x0;
    }

    @Override public void writeToBuffer(@NotNull Boolean object, @NotNull ByteBuffer buffer) {
        buffer.put((byte) (object ? 0x1 : 0x0));
    }
}
