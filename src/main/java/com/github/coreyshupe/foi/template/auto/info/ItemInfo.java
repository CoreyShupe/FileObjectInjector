package com.github.coreyshupe.foi.template.auto.info;

import com.github.coreyshupe.foi.TemplateWalker;
import com.github.coreyshupe.foi.template.Template;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;

@ToString public class ItemInfo<K, V> {
    @NotNull @Getter private final Class<?> type;
    @NotNull private final Template<V> template;
    @NotNull private final Function<K, V> getter;

    public ItemInfo(@NotNull Class<?> type, @NotNull Template<V> template, @NotNull Function<K, V> getter) {
        this.type = type;
        this.template = template;
        this.getter = getter;
    }

    public int size(@NotNull K object) {
        return template.sizeOf(getter.apply(object));
    }

    public V readFromWalker(@NotNull TemplateWalker walker) throws IOException {
        return template.readFromWalker(walker);
    }

    public void writeToBuffer(@NotNull K object, @NotNull ByteBuffer buffer) {
        template.writeToBuffer(getter.apply(object), buffer);
    }

    public boolean isCollection() {
        return this instanceof CollectionItemInfo;
    }
}
