package com.github.coreyshupe.foi;

import com.github.coreyshupe.foi.template.Template;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

public class SimpleTemplateGenerator<T> {
    private final Class<T> clazz;
    private final LinkedList<TemplatePart<T, ?>> templateParts;

    public SimpleTemplateGenerator(Class<T> clazz) {
        this.templateParts = new LinkedList<>();
        this.clazz = clazz;
    }

    public <V> SimpleTemplateGenerator<T> add(Template<V> template, Function<T, V> getFunc) {
        TemplatePart<T, V> part = new TemplatePart<>(getFunc, template);
        templateParts.add(part);
        return this;
    }

    public <V> SimpleTemplateGenerator<T> addCollection(CollectionTemplate<V> template, Function<T, Collection<V>> getFunc) {
        TemplatePart<T, Collection<V>> part = new TemplatePart<>(getFunc, template);
        templateParts.add(part);
        return this;
    }

    public Template<T> generate(Function<Resolver, T> resolverToT) {
        return new Template<T>() {
            private final Class<T> clazz = SimpleTemplateGenerator.this.clazz;
            private final LinkedList<TemplatePart<T, ?>> templateParts = new LinkedList<>(SimpleTemplateGenerator.this.templateParts);

            @Override public boolean isThis(@NotNull Class<?> givenType) {
                return givenType.isAssignableFrom(clazz);
            }

            @Override public int sizeOf(@NotNull T object) {
                return templateParts.parallelStream().map(part -> part.sizeOf(object)).reduce(0, Integer::sum);
            }

            @Override
            public void writeToBuffer(@NotNull TemplateLinker linker, @NotNull T object, @NotNull ByteBuffer buffer) {
                templateParts.forEach(part -> part.write(linker, object, buffer));
            }

            @NotNull @Override
            public T readFromWalker(@NotNull TemplateLinker linker, @NotNull TemplateWalker walker) throws IOException {
                Resolver resolver = new Resolver();
                for (TemplatePart<T, ?> part : templateParts) {
                    resolver.objectLinkedList.addLast(part.read(linker, walker));
                }
                return resolverToT.apply(resolver);
            }
        };
    }

    private static class TemplatePart<K, V> {
        private final Function<K, V> getValueFunction;
        private final Template<V> innerTemplate;

        private TemplatePart(Function<K, V> getValueFunction, Template<V> innerTemplate) {
            this.getValueFunction = getValueFunction;
            this.innerTemplate = innerTemplate;
        }

        private int sizeOf(K object) {
            return innerTemplate.sizeOf(getValueFunction.apply(object));
        }

        private void write(TemplateLinker linker, K object, ByteBuffer buffer) {
            this.innerTemplate.writeToBuffer(linker, getValueFunction.apply(object), buffer);
        }

        private V read(TemplateLinker linker, TemplateWalker walker) throws IOException {
            return innerTemplate.readFromWalker(linker, walker);
        }
    }

    public static class Resolver {
        private final LinkedList<Object> objectLinkedList;

        private Resolver() {
            this.objectLinkedList = new LinkedList<>();
        }

        public <T> T resolveItem(Class<T> t) {
            //noinspection unchecked
            return (T) objectLinkedList.poll();
        }

        public <T> Collection<T> resolveCollection(Class<T> t) {
            //noinspection unchecked
            return (Collection<T>) objectLinkedList.poll();
        }
    }
}
