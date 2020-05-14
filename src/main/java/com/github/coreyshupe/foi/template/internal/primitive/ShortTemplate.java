package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ShortTemplate extends SizedTemplate<Short> {
    @Getter private final static ShortTemplate instance = new ShortTemplate();
    @Getter
    private final static CollectionTemplate<Short> collectionInstance = new CollectionTemplate<>(Short.class, instance, ArrayList::new);

    private ShortTemplate() {
        super(Short.class, Short.BYTES);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == short.class;
    }

    @Override public void writeToBuffer(@NotNull Short object, @NotNull ByteBuffer buffer) {
        buffer.putShort(object);
    }

    @NotNull @Override public Short read(ByteBuffer buffer) {
        return buffer.getShort();
    }
}
