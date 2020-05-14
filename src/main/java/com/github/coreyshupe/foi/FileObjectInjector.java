package com.github.coreyshupe.foi;

import com.github.coreyshupe.foi.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class FileObjectInjector implements AutoCloseable {
    @NotNull private final ObjectInjector injector;
    @Nullable private final FileOutputStream outputStream;
    @Nullable private final FileInputStream inputStream;
    @NotNull private final FileChannel channel;

    public FileObjectInjector(@NotNull ObjectInjector injector, @NotNull FileOutputStream outputStream) {
        this.injector = injector;
        this.outputStream = outputStream;
        this.inputStream = null;
        this.channel = outputStream.getChannel();
    }

    public FileObjectInjector(@NotNull ObjectInjector injector, @NotNull FileInputStream inputStream) {
        this.injector = injector;
        this.outputStream = null;
        this.inputStream = inputStream;
        this.channel = inputStream.getChannel();
    }

    // reading

    public byte read() throws IOException {
        return read(Byte.class).orElseThrow(UnreachableException::new);
    }

    public char readChar() throws IOException {
        return read(Character.class).orElseThrow(UnreachableException::new);
    }

    public double readDouble() throws IOException {
        return read(Double.class).orElseThrow(UnreachableException::new);
    }

    public float readFloat() throws IOException {
        return read(Float.class).orElseThrow(UnreachableException::new);
    }

    public int readInt() throws IOException {
        return read(Integer.class).orElseThrow(UnreachableException::new);
    }

    public long readLong() throws IOException {
        return read(Long.class).orElseThrow(UnreachableException::new);
    }

    public short readShort() throws IOException {
        return read(Short.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public String readString() throws IOException {
        return read(String.class).orElseThrow(UnreachableException::new);
    }

    @NotNull public UUID readUUID() throws IOException {
        return read(UUID.class).orElseThrow(UnreachableException::new);
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

    @NotNull public <T> Optional<T> read(@NotNull Class<T> clazz) throws IOException {
        ensureOpen();
        return injector.readObject(channel, clazz);
    }

    @NotNull public <T> Optional<Collection<T>> readCollection(@NotNull Class<T> clazz) throws IOException {
        ensureOpen();
        return injector.readCollection(channel, clazz);
    }

    @NotNull public <T> T read(@NotNull Template<T> template) throws IOException {
        ensureOpen();
        return injector.readObject(channel, template);
    }

    // writing

    public boolean writeByte(byte b) throws IOException {
        return write(Byte.class, b);
    }

    public boolean writeChar(char c) throws IOException {
        return write(Character.class, c);
    }

    public boolean writeDouble(double d) throws IOException {
        return write(Double.class, d);
    }

    public boolean writeFloat(float f) throws IOException {
        return write(Float.class, f);
    }

    public boolean writeInt(int i) throws IOException {
        return write(Integer.class, i);
    }

    public boolean writeLong(long l) throws IOException {
        return write(Long.class, l);
    }

    public boolean writeShort(short s) throws IOException {
        return write(Short.class, s);
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

    public <T> boolean write(@NotNull T object) throws IOException {
        return injector.writeObject(channel, object);
    }

    public <T> boolean write(@NotNull Class<T> clazz, @NotNull T object) throws IOException {
        ensureOpen();
        return injector.writeObject(channel, clazz, object);
    }

    public <T> boolean writeCollection(@NotNull Class<T> clazz, @NotNull Collection<T> collection) throws IOException {
        ensureOpen();
        return injector.writeCollection(channel, clazz, collection);
    }

    public <T> void write(@NotNull Template<T> template, @NotNull T object) throws IOException {
        ensureOpen();
        injector.writeObject(channel, template, object);
    }

    // channel management

    public void ensureOpen() {
        if (!channel.isOpen()) {
            throw new IllegalStateException("Channel closed while reading or writing.");
        }
    }

    @Override public void close() throws IOException {
        if (inputStream != null) inputStream.close();
        else if (outputStream != null) outputStream.close();
    }

    public static FileObjectInjector fromFile(File file) throws IOException {
        return fromFile(file, ObjectInjector.getDefaultInstance());
    }

    public static FileObjectInjector fromFile(File file, ObjectInjector objectInjector) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        return new FileObjectInjector(objectInjector, stream);
    }

    public static <T> T readFileAsObject(Class<T> type, File file) throws IOException {
        return readFileAsObject(type, file, ObjectInjector.getDefaultInstance());
    }

    public static <T> T readFileAsObject(Class<T> type, File file, ObjectInjector injector) throws IOException {
        FileInputStream stream = null;
        FileObjectInjector fileInjector = null;
        try {
            fileInjector = new FileObjectInjector(injector, stream = new FileInputStream(file));
            return fileInjector.read(type).orElseThrow(() -> new IllegalStateException("Failed to find linker for class."));
        } finally {
            if (fileInjector != null) fileInjector.close();
            else if (stream != null) stream.close();
        }
    }
}
