package com.github.coreyshupe.foi.template.internal;

import com.github.coreyshupe.foi.ObjectInjector;
import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.Template;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class CollectionTemplate<K, V extends Collection<K>> extends Template<V> {
    private final Class<K> internalClass;
    private final Template<K> internalTemplate;
    private final Supplier<V> newCollSupplier;

    public CollectionTemplate(
            @NotNull Class<K> internalClass,
            @NotNull Template<K> internalTemplate,
            @NotNull Supplier<V> newCollSupplier
    ) {
        this.internalClass = internalClass;
        this.internalTemplate = internalTemplate;
        this.newCollSupplier = newCollSupplier;
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return internalClass.isAssignableFrom(givenType);
    }

    @Override public int sizeOf(@NotNull V object) {
        if (internalTemplate instanceof SizedTemplate) {
            return (((SizedTemplate<?>) internalTemplate).getSize() * object.size()) + Integer.BYTES;
        }
        int size = Integer.BYTES;
        for (K k : object) {
            size += internalTemplate.sizeOf(k);
        }
        return size;
    }

    @Override public void writeToBuffer(@NotNull V object, @NotNull ByteBuffer buffer) {
        buffer.putInt(object.size());
        for (K k : object) {
            internalTemplate.writeToBuffer(k, buffer);
        }
    }

    public int sizeOfPrim(@NotNull Collection<K> object) {
        if (internalTemplate instanceof SizedTemplate) {
            return (((SizedTemplate<?>) internalTemplate).getSize() * object.size()) + Integer.BYTES;
        }
        int size = Integer.BYTES;
        for (K k : object) {
            size += internalTemplate.sizeOf(k);
        }
        return size;
    }

    public void writeToBufferPrim(@NotNull Collection<K> object, @NotNull ByteBuffer buffer) {
        buffer.putInt(object.size());
        for (K k : object) {
            internalTemplate.writeToBuffer(k, buffer);
        }
    }

    @NotNull @Override public V readFromWalker(@NotNull TemplateWalker walker) throws IOException {
        int size = walker.readSizeOf(Integer.BYTES).getInt();
        V newCollection = newCollSupplier.get();
        for (int i = 0; i < size; i++) {
            newCollection.add(internalTemplate.readFromWalker(walker));
        }
        return newCollection;
    }

    public static <K, V extends Collection<K>> Optional<CollectionTemplate<K, V>> of(
            @NotNull Class<K> internalClass,
            @NotNull Supplier<V> newCollSupplier
    ) {
        return of(internalClass, ObjectInjector.getDefaultLinker(), newCollSupplier);
    }

    public static <K, V extends Collection<K>> Optional<CollectionTemplate<K, V>> of(
            @NotNull Class<K> internalClass,
            @NotNull TemplateLinker linker,
            @NotNull Supplier<V> newCollSupplier
    ) {
        return linker.getTemplate(internalClass).map(template -> of(internalClass, template, newCollSupplier));
    }

    public static <K, V extends Collection<K>> CollectionTemplate<K, V> of(
            @NotNull Class<K> internalClass,
            @NotNull Template<K> internalTemplate,
            @NotNull Supplier<V> newCollSupplier
    ) {
        return new CollectionTemplate<K, V>(internalClass, internalTemplate, newCollSupplier);
    }
}
