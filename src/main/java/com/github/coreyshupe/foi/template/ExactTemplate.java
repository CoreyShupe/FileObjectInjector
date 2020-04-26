package com.github.coreyshupe.foi.template;

import org.jetbrains.annotations.NotNull;

public abstract class ExactTemplate<T> extends Template<T> {
    @NotNull private final Class<T> internalClass;

    public ExactTemplate(@NotNull Class<T> clazz) {
        this.internalClass = clazz;
    }

    @Override public boolean isThis(@NotNull Class<?> givenType) {
        return givenType == internalClass;
    }
}
