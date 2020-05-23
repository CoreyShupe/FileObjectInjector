package com.github.coreyshupe.foi.template.auto.builder;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Buildable<K, V> {
    K getBuilder();

    V completeBuilder(K builder);

    public static <K, V> Buildable<K, V> from(Supplier<K> supplier, Function<K, V> function) {
        return new Buildable<K, V>() {
            @Override public K getBuilder() {
                return supplier.get();
            }

            @Override public V completeBuilder(K builder) {
                return function.apply(builder);
            }
        };
    }
}
