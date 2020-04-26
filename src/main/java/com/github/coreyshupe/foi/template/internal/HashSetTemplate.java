package com.github.coreyshupe.foi.template.internal;

import com.github.coreyshupe.foi.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class HashSetTemplate<T> extends CollectionTemplate<T> {
    public HashSetTemplate(@NotNull Class<T> internalClass, @NotNull Template<T> internalTemplate) {
        super(internalClass, internalTemplate, HashSet::new);
    }
}
