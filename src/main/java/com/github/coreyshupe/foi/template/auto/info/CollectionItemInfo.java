package com.github.coreyshupe.foi.template.auto.info;

import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Function;

public class CollectionItemInfo<K, V, Z extends Collection<V>> extends ItemInfo<K, Z> {
    public CollectionItemInfo(@NotNull Class<V> type, @NotNull CollectionTemplate<V, Z> template, @NotNull Function<K, Z> getter) {
        super(type, template, getter);
    }
}