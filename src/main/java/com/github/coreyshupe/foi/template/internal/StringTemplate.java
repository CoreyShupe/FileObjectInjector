package com.github.coreyshupe.foi.template.internal;

import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.ExactTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class StringTemplate extends ExactTemplate<String> {
    @Getter private final static StringTemplate instance = new StringTemplate();
    @Getter
    private final static CollectionTemplate<String> collectionInstance = new CollectionTemplate<>(String.class, instance, ArrayList::new);

    private StringTemplate() {
        super(String.class);
    }

    @Override public int sizeOf(@NotNull String object) {
        return object.getBytes().length + Integer.BYTES;
    }

    @Override
    public void writeToBuffer(@NotNull TemplateLinker linker, @NotNull String object, @NotNull ByteBuffer buffer) {
        buffer.putInt(object.getBytes().length);
        buffer.put(object.getBytes());
    }

    @NotNull @Override
    public String readFromWalker(@NotNull TemplateLinker linker, @NotNull TemplateWalker walker) throws IOException {
        int size = walker.readSizeOf(Integer.BYTES).getInt();
        byte[] bytes = new byte[size];
        walker.readSizeOf(Byte.BYTES * size).get(bytes);
        return new String(bytes);
    }
}
