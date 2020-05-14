package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FloatTemplate extends SizedTemplate<Float> {
    @Getter private final static FloatTemplate instance = new FloatTemplate();
    @Getter
    private final static CollectionTemplate<Float> collectionInstance = new CollectionTemplate<>(Float.class, instance, ArrayList::new);

    private FloatTemplate() {
        super(Float.class, Float.BYTES);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == float.class;
    }

    @Override public void writeToBuffer(@NotNull Float object, @NotNull ByteBuffer buffer) {
        buffer.putFloat(object);
    }

    @NotNull @Override public Float read(ByteBuffer buffer) {
        return buffer.getFloat();
    }
}
