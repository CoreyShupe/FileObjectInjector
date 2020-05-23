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

@SuppressWarnings("unused") public abstract class OrderedTemplate<T> extends Template<T> {
    @NotNull private final List<ItemInfo<T, ?>> itemInfoList;
    @NotNull private final Class<T> type;
    @NotNull private final TemplateLinker linker;

    public OrderedTemplate(@NotNull Class<T> type) {
        this(type, ObjectInjector.getDefaultLinker());
    }

    public OrderedTemplate(@NotNull Class<T> type, @NotNull TemplateLinker linker) {
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
        ItemPoll poll = new ItemPoll();
        for (ItemInfo<?, ?> item : itemInfoList) {
            poll.objectQueue.offer(item.readFromWalker(walker));
        }
        return readObject(poll);
    }

    @NotNull public abstract T readObject(ItemPoll poll);

    public static class ItemPoll {
        @NotNull private final Queue<Object> objectQueue = new LinkedList<>();

        public <T> T next() {
            //noinspection unchecked
            return (T) objectQueue.poll();
        }
    }
}
