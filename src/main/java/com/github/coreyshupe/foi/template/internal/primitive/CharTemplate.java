package com.github.coreyshupe.foi.template.internal.primitive;

import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CharTemplate extends SizedTemplate<Character> {
    @Getter private final static CharTemplate instance = new CharTemplate();
    @Getter
    private final static CollectionTemplate<Character> collectionInstance = new CollectionTemplate<>(Character.class, instance, ArrayList::new);

    private CharTemplate() {
        super(Character.class, Character.BYTES);
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return super.isThis(givenType) || givenType == char.class;
    }

    @Override
    public void writeToBuffer(@NotNull TemplateLinker linker, @NotNull Character object, @NotNull ByteBuffer buffer) {
        buffer.putChar(object);
    }

    @NotNull @Override public Character read(ByteBuffer buffer) {
        return buffer.getChar();
    }
}
