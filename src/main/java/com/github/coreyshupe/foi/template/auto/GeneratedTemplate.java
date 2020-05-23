package com.github.coreyshupe.foi.template.auto;

import com.github.coreyshupe.foi.ObjectInjector;
import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.Template;
import com.github.coreyshupe.foi.template.auto.info.CollectionItemInfo;
import com.github.coreyshupe.foi.template.auto.info.ItemInfo;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused") public abstract class GeneratedTemplate<T> extends Template<T> {
    @NotNull private final List<ItemInfo<T, ?>> itemInfoList;
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

    private <X, Y extends Collection<X>> void addCollection(
            @NotNull Class<X> type,
            @NotNull Function<T, Y> getter,
            @NotNull Supplier<Y> collectionSupplier
    ) {
        Optional<Template<X>> optionalTemplate = linker.getTemplate(type);
        assert optionalTemplate.isPresent();
        CollectionTemplate<X, Y> collectionTemplate = new CollectionTemplate<>(type, optionalTemplate.get(), collectionSupplier);
        itemInfoList.add(new CollectionItemInfo<>(type, collectionTemplate, getter));
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
        for (ItemInfo<?, ?> item : itemInfoList) {
            if (item.isCollection()) {
                items.insertCollection(item.getType(), item.readFromWalker(walker));
            } else {
                items.insertItem(item.getType(), item.readFromWalker(walker));
            }
        }
        return readObject(items);
    }

    @NotNull public abstract T readObject(ResolvedItems items);

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

        @NotNull public <T> T pollItem(@NotNull Class<T> type) {
            List<Object> objectList = objectMap.get(type);
            Object object = objectList.get(0);
            objectList.remove(0);
            //noinspection unchecked
            return (T) object;
        }

        @NotNull public <T> Collection<T> getCollection(@NotNull Class<T> type, int index) {
            //noinspection unchecked
            return (Collection<T>) collectionMap.get(type).get(index);
        }

        @NotNull public <T> Collection<T> pollCollection(@NotNull Class<T> type) {
            List<Object> objectList = collectionMap.get(type);
            Object object = objectList.get(0);
            objectList.remove(0);
            //noinspection unchecked
            return (Collection<T>) object;
        }
    }
}
