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

public class ChannelObjectInjector {
    @NotNull @Getter private final TemplateLinker templateLinker;

    public ChannelObjectInjector() {
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
        this.templateLinker.addCollectionTemplate(ByteTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(CharTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(DoubleTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(FloatTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(IntTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(LongTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(ShortTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(StringTemplate.getCollectionInstance());
        this.templateLinker.addCollectionTemplate(UUIDTemplate.getCollectionInstance());
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
        Optional<CollectionTemplate<T>> optionalTemplate = templateLinker.getCollectionTemplate(clazz);
        if (optionalTemplate.isPresent()) {
            return Optional.of(readObject(channel, optionalTemplate.get()));
        } else {
            return Optional.empty();
        }
    }

    @NotNull
    public <T> T readObject(@NotNull ReadableByteChannel channel, @NotNull Template<T> template) throws IOException {
        TemplateWalker walker = new TemplateWalker() {
            private ByteBuffer buffer = null;

            @Override public @NotNull ByteBuffer readSizeOf(int size) throws IOException {
                if (buffer == null) {
                    buffer = ByteBuffer.allocate(size);
                    channel.read(buffer);
                    buffer.flip();
                    return buffer;
                }
                if (buffer.capacity() >= size) {
                    buffer.clear();
                    buffer.limit(size);
                } else {
                    buffer = ByteBuffer.allocate(size);
                }
                channel.read(buffer);
                buffer.flip();
                return buffer;
            }
        };
        return template.readFromWalker(templateLinker, walker);
    }

    public <T> boolean writeObject(@NotNull WritableByteChannel channel, @NotNull T object) throws IOException {
        // people shouldn't use this really, but it's available if they want to or are sure
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
        Optional<CollectionTemplate<T>> optionalTemplate = templateLinker.getCollectionTemplate(clazz);
        if (optionalTemplate.isPresent()) {
            writeObject(channel, optionalTemplate.get(), collection);
            return true;
        } else {
            return false;
        }
    }

    public <T> void writeObject(@NotNull WritableByteChannel channel, @NotNull Template<T> template, @NotNull T object) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(template.sizeOf(object));
        template.writeToBuffer(templateLinker, object, buffer);
        buffer.flip();
        channel.write(buffer);
    }
}
