package com.github.coreyshupe.foi;

import com.github.coreyshupe.foi.template.Template;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class FileObjectInjector implements AutoCloseable {
    private final ChannelObjectInjector injector;
    private final FileChannel channel;

    public FileObjectInjector(ChannelObjectInjector injector, FileChannel channel) {
        this.injector = injector;
        this.channel = channel;
    }

    // reading

    public byte read() throws IOException {
        return readObject(Byte.class).orElseThrow(UnreachableException::new);
    }

    public char readChar() throws IOException {
        return readObject(Character.class).orElseThrow(UnreachableException::new);
    }

    public double readDouble() throws IOException {
        return readObject(Double.class).orElseThrow(UnreachableException::new);
    }

    public float readFloat() throws IOException {
        return readObject(Float.class).orElseThrow(UnreachableException::new);
    }

    public int readInt() throws IOException {
        return readObject(Integer.class).orElseThrow(UnreachableException::new);
    }

    public long readLong() throws IOException {
        return readObject(Long.class).orElseThrow(UnreachableException::new);
    }

    public short readShort() throws IOException {
        return readObject(Short.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public String readString() throws IOException {
        return readObject(String.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public UUID readUUID() throws IOException {
        return readObject(UUID.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<Byte> readByteCollection() throws IOException {
        return readCollection(Byte.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<Character> readCharCollection() throws IOException {
        return readCollection(Character.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<Double> readDoubleCollection() throws IOException {
        return readCollection(Double.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<Float> readFloatCollection() throws IOException {
        return readCollection(Float.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<Integer> readIntCollection() throws IOException {
        return readCollection(Integer.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<Long> readLongCollection() throws IOException {
        return readCollection(Long.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<Short> readShortCollection() throws IOException {
        return readCollection(Short.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<String> readStringCollection() throws IOException {
        return readCollection(String.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public Collection<UUID> readUUIDCollection() throws IOException {
        return readCollection(UUID.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public <T> Optional<T> readObject(@NotNull Class<T> clazz) throws IOException {
        ensureOpen();
        return injector.readObject(channel, clazz);
    }

    @NotNull public <T> Optional<Collection<T>> readCollection(@NotNull Class<T> clazz) throws IOException {
        ensureOpen();
        return injector.readCollection(channel, clazz);
    }

    @NotNull public <T> T readObject(@NotNull Template<T> template) throws IOException {
        ensureOpen();
        return injector.readObject(channel, template);
    }

    // writing

    public boolean writeByte(byte b) throws IOException {
        return writeObject(Byte.class, b);
    }

    public boolean writeChar(char c) throws IOException {
        return writeObject(Character.class, c);
    }

    public boolean writeDouble(double d) throws IOException {
        return writeObject(Double.class, d);
    }

    public boolean writeFloat(float f) throws IOException {
        return writeObject(Float.class, f);
    }

    public boolean writeInt(int i) throws IOException {
        return writeObject(Integer.class, i);
    }

    public boolean writeLong(long l) throws IOException {
        return writeObject(Long.class, l);
    }

    public boolean writeShort(short s) throws IOException {
        return writeObject(Short.class, s);
    }

    public boolean writeString(@NotNull String string) throws IOException {
        return writeObject(String.class, string);
    }

    public boolean writeUUID(@NotNull UUID id) throws IOException {
        return writeObject(UUID.class, id);
    }

    public boolean writeByteCollection(@NotNull Collection<Byte> b) throws IOException {
        return writeCollection(Byte.class, b);
    }

    public boolean writeCharCollection(@NotNull Collection<Character> c) throws IOException {
        return writeCollection(Character.class, c);
    }

    public boolean writeDoubleCollection(@NotNull Collection<Double> d) throws IOException {
        return writeCollection(Double.class, d);
    }

    public boolean writeFloatCollection(@NotNull Collection<Float> f) throws IOException {
        return writeCollection(Float.class, f);
    }

    public boolean writeIntCollection(@NotNull Collection<Integer> i) throws IOException {
        return writeCollection(Integer.class, i);
    }

    public boolean writeLongCollection(@NotNull Collection<Long> l) throws IOException {
        return writeCollection(Long.class, l);
    }

    public boolean writeShortCollection(@NotNull Collection<Short> s) throws IOException {
        return writeCollection(Short.class, s);
    }

    public boolean writeStringCollection(@NotNull Collection<String> string) throws IOException {
        return writeCollection(String.class, string);
    }

    public boolean writeUUIDCollection(@NotNull Collection<UUID> uuid) throws IOException {
        return writeCollection(UUID.class, uuid);
    }

    public <T> boolean writeObject(@NotNull T object) throws IOException {
        return injector.writeObject(channel, object);
    }

    public <T> boolean writeObject(@NotNull Class<T> clazz, @NotNull T object) throws IOException {
        ensureOpen();
        return injector.writeObject(channel, clazz, object);
    }

    public <T> boolean writeCollection(@NotNull Class<T> clazz, @NotNull Collection<T> collection) throws IOException {
        ensureOpen();
        return injector.writeCollection(channel, clazz, collection);
    }

    public <T> void writeObject(@NotNull Template<T> template, @NotNull T object) throws IOException {
        ensureOpen();
        injector.writeObject(channel, template, object);
    }

    // channel management

    public void ensureOpen() {
        if (channel == null || !channel.isOpen()) {
            throw new IllegalStateException("Channel closed while reading or writing.");
        }
    }

    @Override public void close() throws IOException {
        channel.close();
    }
}
