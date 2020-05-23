package com.github.coreyshupe.foi.template.auto;

import com.github.coreyshupe.foi.ObjectInjector;
import com.github.coreyshupe.foi.TemplateLinker;
import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.Template;
import com.github.coreyshupe.foi.template.auto.builder.Buildable;
import com.github.coreyshupe.foi.template.auto.info.CollectionItemInfo;
import com.github.coreyshupe.foi.template.auto.info.ItemInfo;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused") public class BuildableTemplate<K, V> extends Template<K> {
    @NotNull private final Buildable<V, K> buildable;
    @NotNull private final List<ItemInfo<K, ?>> itemInfoList;
    @NotNull private final List<BuildStep<?>> buildSteps;
    @NotNull private final Class<K> type;
    @NotNull private final TemplateLinker linker;

    public BuildableTemplate(@NotNull Class<K> type, @NotNull Buildable<V, K> buildable) {
        this(type, ObjectInjector.getDefaultLinker(), buildable);
    }

    public BuildableTemplate(@NotNull Class<K> type, @NotNull TemplateLinker linker, @NotNull Buildable<V, K> buildable) {
        this.itemInfoList = new LinkedList<>();
        this.type = type;
        this.linker = linker;
        this.buildable = buildable;
        this.buildSteps = new ArrayList<>();
    }

    public <X> boolean addItem(@NotNull Class<X> type, @NotNull Function<K, X> getter, @NotNull BiConsumer<V, X> buildStep) {
        buildSteps.add(new BuildStep<>(buildStep));
        return addItem(type, getter);
    }

    private <X> boolean addItem(@NotNull Class<X> type, @NotNull Function<K, X> getter) {
        Optional<Template<X>> optionalTemplate = linker.getTemplate(type);
        if (optionalTemplate.isPresent()) {
            itemInfoList.add(new ItemInfo<>(type, optionalTemplate.get(), getter));
            return true;
        } else {
            return false;
        }
    }

    public <X, Y extends Collection<X>> void addCollection(
            @NotNull Class<X> type,
            @NotNull Function<K, Y> getter,
            @NotNull Supplier<Y> collectionSupplier,
            @NotNull BiConsumer<V, Y> buildStep
    ) {
        buildSteps.add(new BuildStep<>(buildStep));
        addCollection(type, getter, collectionSupplier);
    }

    private <X, Y extends Collection<X>> void addCollection(
            @NotNull Class<X> type,
            @NotNull Function<K, Y> getter,
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

    @Override public int sizeOf(@NotNull K object) {
        return itemInfoList.parallelStream().map(item -> item.size(object)).reduce(0, Integer::sum);
    }

    @Override public void writeToBuffer(@NotNull K object, @NotNull ByteBuffer buffer) {
        itemInfoList.forEach(item -> item.writeToBuffer(object, buffer));
    }

    @NotNull @Override public K readFromWalker(@NotNull TemplateWalker walker) throws IOException {
        V builder = buildable.getBuilder();
        int index = 0;
        for (ItemInfo<?, ?> item : itemInfoList) {
            Object obj = item.readFromWalker(walker);
            buildSteps.get(index).applyStep(builder, obj);
            index++;
        }
        return buildable.completeBuilder(builder);
    }

    public class BuildStep<X> {
        private final BiConsumer<V, X> consumerStep;

        public BuildStep(BiConsumer<V, X> consumerStep) {
            this.consumerStep = consumerStep;
        }

        private void applyStep(V builder, Object object) {
            //noinspection unchecked
            consumerStep.accept(builder, (X) object);
        }
    }

    public static class ItemPoll {
        @NotNull private final Queue<Object> objectQueue = new LinkedList<>();

        public <T> T next() {
            //noinspection unchecked
            return (T) objectQueue.poll();
        }
    }

    @NotNull
    public static <K, V> BuildableTemplateBuilder<K, V> buildTemplate(
            Class<V> type,
            @NotNull Supplier<K> builderSupplier,
            @NotNull Function<K, V> builderComplete
    ) {
        return new BuildableTemplateBuilder<>(type, Buildable.from(builderSupplier, builderComplete));
    }

    public static class BuildableTemplateBuilder<K, V> {
        private final BuildableTemplate<V, K> buildableTemplate;

        private BuildableTemplateBuilder(Class<V> type, @NotNull Buildable<K, V> buildable) {
            this.buildableTemplate = new BuildableTemplate<>(type, buildable);
        }

        public <X> BuildableTemplateBuilder<K, V> addItem(@NotNull Class<X> type, @NotNull Function<V, X> getter, @NotNull BiConsumer<K, X> buildStep) {
            buildableTemplate.addItem(type, getter, buildStep);
            return this;
        }

        public <X, Y extends Collection<X>> BuildableTemplateBuilder<K, V> addCollection(
                @NotNull Class<X> type,
                @NotNull Function<V, Y> getter,
                @NotNull Supplier<Y> collectionSupplier,
                @NotNull BiConsumer<K, Y> buildStep
        ) {
            buildableTemplate.addCollection(type, getter, collectionSupplier, buildStep);
            return this;
        }

        public BuildableTemplate<V, K> build() {
            return buildableTemplate;
        }

        public void addToDefaultLinker() {
            ObjectInjector.getDefaultLinker().addTemplate(buildableTemplate);
        }

        public void addToLinker(TemplateLinker linker) {
            linker.addTemplate(buildableTemplate);
        }
    }
}
