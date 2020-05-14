package com.github.coreyshupe.foi.template;

import com.github.coreyshupe.foi.ObjectInjector;
import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.TemplateWalker;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

public abstract class GeneratedTemplate<T> extends Template<T> {
    @NotNull private final List<ItemInfo<?>> itemInfoList;
    @NotNull private final Class<T> type;
    @NotNull private final TemplateLinker linker;

    public GeneratedTemplate(@NotNull Class<T> type) {
        this(type, ObjectInjector.getDefaultLinker());
    }

    public GeneratedTemplate(@NotNull Class<T> type, @NotNull TemplateLinker linker) {
        this.itemInfoList = new LinkedList<>();
        this.type = type;
        this.linker = linker;
    }

    public <X> boolean addItem(Class<X> type, Function<T, X> getter) {
        Optional<Template<X>> optionalTemplate = linker.getTemplate(type);
        if (optionalTemplate.isPresent()) {
            itemInfoList.add(new ItemInfo<>(type, optionalTemplate.get(), getter));
            return true;
        } else {
            return false;
        }
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return type.isAssignableFrom(givenType);
    }

    @Override public int sizeOf(@NotNull T object) {
        return itemInfoList.parallelStream().map(item -> item.size(object)).reduce(0, Integer::sum);
    }

    @Override public void writeToBuffer(@NotNull TemplateLinker linker, @NotNull T object, @NotNull ByteBuffer buffer) {
        itemInfoList.forEach(item -> item.writeToBuffer(linker, object, buffer));
    }

    @NotNull @Override
    public T readFromWalker(@NotNull TemplateLinker linker, @NotNull TemplateWalker walker) throws IOException {
        ResolvedItems items = new ResolvedItems();
        for (ItemInfo<?> item : itemInfoList) {
            items.insertItem(item.type, item.readFromWalker(linker, walker));
        }
        return readObject(items);
    }

    public abstract T readObject(ResolvedItems items);

    private class ItemInfo<X> {
        @NotNull private final Class<X> type;
        @NotNull private final Template<X> template;
        @NotNull private final Function<T, X> getter;

        private ItemInfo(@NotNull Class<X> type, @NotNull Template<X> template, @NotNull Function<T, X> getter) {
            this.type = type;
            this.template = template;
            this.getter = getter;
        }

        private int size(@NotNull T object) {
            return template.sizeOf(getter.apply(object));
        }

        public X readFromWalker(@NotNull TemplateLinker linker, @NotNull TemplateWalker walker) throws IOException {
            return template.readFromWalker(linker, walker);
        }

        public void writeToBuffer(@NotNull TemplateLinker linker, @NotNull T object, @NotNull ByteBuffer buffer) {
            template.writeToBuffer(linker, getter.apply(object), buffer);
        }
    }

    public static class ResolvedItems {
        private final Map<Class<?>, List<Object>> objectMap;

        public ResolvedItems() {
            objectMap = new HashMap<>();
        }

        private void insertItem(Class<?> type, Object object) {
            if (!objectMap.containsKey(type)) {
                objectMap.put(type, new ArrayList<>());
            }
            objectMap.get(type).add(object);
        }

        public <T> T getItem(Class<T> type, int index) {
            //noinspection unchecked
            return (T) objectMap.get(type).get(index);
        }
    }
}
