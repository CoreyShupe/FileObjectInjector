package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DoubleTemplate extends SizedTemplate<Double> {
    @Getter private final static DoubleTemplate instance = new DoubleTemplate();
    @Getter
    private final static CollectionTemplate<Double> collectionInstance = new CollectionTemplate<>(Double.class, instance, ArrayList::new);

    private DoubleTemplate() {
        super(Double.class, Double.BYTES);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == double.class;
    }

    @Override public void writeToBuffer(@NotNull Double object, @NotNull ByteBuffer buffer) {
        buffer.putDouble(object);
    }

    @NotNull @Override public Double read(ByteBuffer buffer) {
        return buffer.getDouble();
    }
}
