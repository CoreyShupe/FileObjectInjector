package com.github.coreyshupe.foi.template.internal;

import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.SizedTemplate;
import com.github.coreyshupe.foi.template.Template;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.Supplier;

public class CollectionTemplate<T> extends Template<Collection<T>> {
    private final Class<T> internalClass;
    private final Template<T> internalTemplate;
    private final Supplier<Collection<T>> newCollSupplier;

    public CollectionTemplate(@NotNull Class<T> internalClass, @NotNull Template<T> internalTemplate, @NotNull Supplier<Collection<T>> newCollSupplier) {
        this.internalClass = internalClass;
        this.internalTemplate = internalTemplate;
        this.newCollSupplier = newCollSupplier;
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return internalClass.isAssignableFrom(givenType);
    }

    @Override public int sizeOf(@NotNull Collection<T> object) {
        if (internalTemplate instanceof SizedTemplate) {
            return (((SizedTemplate<?>) internalTemplate).getSize() * object.size()) + Integer.BYTES;
        }
        int size = Integer.BYTES;
        for (T t : object) {
            size += internalTemplate.sizeOf(t);
        }
        return size;
    }

    @Override public void writeToBuffer(@NotNull Collection<T> object, @NotNull ByteBuffer buffer) {
        buffer.putInt(object.size());
        for (T t : object) {
            internalTemplate.writeToBuffer(t, buffer);
        }
    }

    @NotNull @Override public Collection<T> readFromWalker(@NotNull TemplateWalker walker) throws IOException {
        int size = walker.readSizeOf(Integer.BYTES).getInt();
        Collection<T> newCollection = newCollSupplier.get();
        for (int i = 0; i < size; i++) {
            newCollection.add(internalTemplate.readFromWalker(walker));
        }
        return newCollection;
    }
}
