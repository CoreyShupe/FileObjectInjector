package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class IntTemplate extends SizedTemplate<Integer> {
    @Getter private final static IntTemplate instance = new IntTemplate();
    @Getter
    private final static CollectionTemplate<Integer> collectionInstance = new CollectionTemplate<>(Integer.class, instance, ArrayList::new);

    private IntTemplate() {
        super(Integer.class, Integer.BYTES);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == int.class;
    }

    @Override public void writeToBuffer(@NotNull Integer object, @NotNull ByteBuffer buffer) {
        buffer.putInt(object);
    }

    @NotNull @Override public Integer read(ByteBuffer buffer) {
        return buffer.getInt();
    }
}
