package com.github.coreyshupe.foi.template.internal;

import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.template.SizedTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class UUIDTemplate extends SizedTemplate<UUID> {
    @Getter private final static UUIDTemplate instance = new UUIDTemplate();
    @Getter
    private final static CollectionTemplate<UUID> collectionInstance = new CollectionTemplate<>(UUID.class, instance, ArrayList::new);

    private UUIDTemplate() {
        super(UUID.class, Long.BYTES * 2);
    }

    @Override
    public void writeToBuffer(@NotNull TemplateLinker linker, @NotNull UUID object, @NotNull ByteBuffer buffer) {
        buffer.putLong(object.getMostSignificantBits());
        buffer.putLong(object.getLeastSignificantBits());
    }

    @NotNull @Override public UUID read(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
