package com.github.coreyshupe.foi.template;

import com.github.coreyshupe.foi.ObjectInjector;
import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused") public abstract class GeneratedTemplate<T> extends Template<T> {
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

    public <X> boolean addItem(@NotNull Class<X> type, @NotNull Function<T, X> getter) {
        Optional<Template<X>> optionalTemplate = linker.getTemplate(type);
        if (optionalTemplate.isPresent()) {
            itemInfoList.add(new ItemInfo<>(type, optionalTemplate.get(), getter));
            return true;
        } else {
            return false;
        }
    }

    public <X> boolean addCollection(@NotNull Class<X> type, @NotNull Function<T, Collection<X>> getter) {
        Optional<CollectionTemplate<X>> optionalTemplate = linker.getCollectionTemplate(type);
        if (optionalTemplate.isPresent()) {
            itemInfoList.add(new CollectionItemInfo<>(type, optionalTemplate.get(), getter));
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

    @Override public void writeToBuffer(@NotNull T object, @NotNull ByteBuffer buffer) {
        itemInfoList.forEach(item -> item.writeToBuffer(object, buffer));
    }

    @NotNull @Override public T readFromWalker(@NotNull TemplateWalker walker) throws IOException {
        ResolvedItems items = new ResolvedItems();
        for (ItemInfo<?> item : itemInfoList) {
            if (item.isCollection()) {
                items.insertCollection(item.type, item.readFromWalker(walker));
            } else {
                items.insertItem(item.type, item.readFromWalker(walker));
            }
        }
        return readObject(items);
    }

    @NotNull public abstract T readObject(ResolvedItems items);

    private class ItemInfo<X> {
        @NotNull private final Class<?> type;
        @NotNull private final Template<X> template;
        @NotNull private final Function<T, X> getter;

        private ItemInfo(@NotNull Class<?> type, @NotNull Template<X> template, @NotNull Function<T, X> getter) {
            this.type = type;
            this.template = template;
            this.getter = getter;
        }

        private int size(@NotNull T object) {
            return template.sizeOf(getter.apply(object));
        }

        public X readFromWalker(@NotNull TemplateWalker walker) throws IOException {
            return template.readFromWalker(walker);
        }

        public void writeToBuffer(@NotNull T object, @NotNull ByteBuffer buffer) {
            template.writeToBuffer(getter.apply(object), buffer);
        }

        public boolean isCollection() {
            return this instanceof CollectionItemInfo;
        }
    }

    private class CollectionItemInfo<X> extends ItemInfo<Collection<X>> {
        private CollectionItemInfo(@NotNull Class<X> type, @NotNull CollectionTemplate<X> template, @NotNull Function<T, Collection<X>> getter) {
            super(type, template, getter);
        }
    }

    public static class ResolvedItems {
        @NotNull private final Map<Class<?>, List<Object>> objectMap;
        @NotNull private final Map<Class<?>, List<Object>> collectionMap;

        public ResolvedItems() {
            objectMap = new HashMap<>();
            collectionMap = new HashMap<>();
        }

        private void insertItem(@NotNull Class<?> type, @NotNull Object object) {
            if (!objectMap.containsKey(type)) {
                objectMap.put(type, new ArrayList<>());
            }
            objectMap.get(type).add(object);
        }

        private void insertCollection(@NotNull Class<?> type, @NotNull Object collection) {
            if (!collectionMap.containsKey(type)) {
                collectionMap.put(type, new ArrayList<>());
            }
            collectionMap.get(type).add(collection);
        }

        @NotNull public <T> T getItem(@NotNull Class<T> type, int index) {
            //noinspection unchecked
            return (T) objectMap.get(type).get(index);
        }

        @NotNull public <T> Collection<T> getCollection(@NotNull Class<T> type, int index) {
            //noinspection unchecked
            return (Collection<T>) objectMap.get(type).get(index);
        }
    }
}
