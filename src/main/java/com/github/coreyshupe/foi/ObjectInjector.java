package com.github.coreyshupe.foi;

import com.github.coreyshupe.foi.template.Template;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import com.github.coreyshupe.foi.template.internal.StringTemplate;
import com.github.coreyshupe.foi.template.internal.UUIDTemplate;
import com.github.coreyshupe.foi.template.internal.primitive.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.Optional;

public class ObjectInjector {
    @NotNull @Getter private final static ObjectInjector defaultInstance = new ObjectInjector();

    public static TemplateLinker getDefaultLinker() {
        return defaultInstance.templateLinker;
    }

    @NotNull @Getter private final TemplateLinker templateLinker;

    public ObjectInjector() {
        this.templateLinker = new TemplateLinker();
        // string & primitives
        this.templateLinker.addTemplate(ByteTemplate.getInstance());
        this.templateLinker.addTemplate(CharTemplate.getInstance());
        this.templateLinker.addTemplate(DoubleTemplate.getInstance());
        this.templateLinker.addTemplate(FloatTemplate.getInstance());
        this.templateLinker.addTemplate(IntTemplate.getInstance());
        this.templateLinker.addTemplate(LongTemplate.getInstance());
        this.templateLinker.addTemplate(ShortTemplate.getInstance());
        this.templateLinker.addTemplate(StringTemplate.getInstance());
        this.templateLinker.addTemplate(UUIDTemplate.getInstance());
        // string & primitive collections
    }

    @NotNull
    public <T> Optional<T> readObject(@NotNull ReadableByteChannel channel, @NotNull Class<T> clazz) throws IOException {
        Optional<Template<T>> optionalTemplate = templateLinker.getTemplate(clazz);
        if (optionalTemplate.isPresent()) {
            return Optional.of(readObject(channel, optionalTemplate.get()));
        } else {
            return Optional.empty();
        }
    }

    @NotNull
    public <T> Optional<Collection<T>> readCollection(@NotNull ReadableByteChannel channel, @NotNull Class<T> clazz) throws IOException {
        Optional<CollectionTemplate<T, ? extends Collection<T>>> optionalTemplate = templateLinker.getCollectionTemplate(clazz);
        if (optionalTemplate.isPresent()) {
            return Optional.of(readObject(channel, optionalTemplate.get()));
        } else {
            return Optional.empty();
        }
    }

    @NotNull
    public <T> T readObject(@NotNull ReadableByteChannel channel, @NotNull Template<T> template) throws IOException {
        TemplateWalker walker = new TemplateWalker() {
            private ByteBuffer buffer = ByteBuffer.allocate(1024);

            @Override public @NotNull ByteBuffer readSizeOf(int size) throws IOException {
                if (buffer.capacity() >= size) {
                    if (buffer.position() != 0) {
                        buffer.flip();
                    }
                    buffer.limit(size);
                } else {
                    buffer = ByteBuffer.allocate(size);
                }
                channel.read(buffer);
                buffer.flip();
                return buffer;
            }
        };
        return template.readFromWalker(walker);
    }

    public <T> boolean writeObject(@NotNull WritableByteChannel channel, @NotNull T object) throws IOException {
        //noinspection unchecked
        Optional<Template<T>> optionalTemplate = templateLinker.getTemplate(object).map(template -> (Template<T>) template);
        if (optionalTemplate.isPresent()) {
            writeObject(channel, optionalTemplate.get(), object);
            return true;
        } else {
            return false;
        }
    }

    public <T> boolean writeObject(@NotNull WritableByteChannel channel, @NotNull Class<T> clazz, @NotNull T object) throws IOException {
        Optional<Template<T>> optionalTemplate = templateLinker.getTemplate(clazz);
        if (optionalTemplate.isPresent()) {
            writeObject(channel, optionalTemplate.get(), object);
            return true;
        } else {
            return false;
        }
    }

    public <T> boolean writeCollection(@NotNull WritableByteChannel channel, @NotNull Class<T> clazz, @NotNull Collection<T> collection) throws IOException {
        Optional<CollectionTemplate<T, ? extends Collection<T>>> optionalTemplate = templateLinker.getCollectionTemplate(clazz);
        if (optionalTemplate.isPresent()) {
            CollectionTemplate<T, ? extends Collection<T>> template = optionalTemplate.get();
            ByteBuffer buffer = ByteBuffer.allocate(template.sizeOfPrim(collection));
            template.writeToBufferPrim(collection, buffer);
            buffer.flip();
            channel.write(buffer);
            return true;
        } else {
            return false;
        }
    }

    public <T> void writeObject(@NotNull WritableByteChannel channel, @NotNull Template<T> template, @NotNull T object) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(template.sizeOf(object));
        template.writeToBuffer(object, buffer);
        buffer.flip();
        channel.write(buffer);
    }
}
