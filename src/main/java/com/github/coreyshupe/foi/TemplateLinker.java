package com.github.coreyshupe.foi;

import com.github.coreyshupe.foi.template.Template;
import com.github.coreyshupe.foi.template.internal.CollectionTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings({"unused", "UnusedReturnValue"}) public class TemplateLinker {
    @NotNull private final Set<Template<?>> templates;
    @NotNull private final Set<Template<?>> collectionTemplates;
    @NotNull private final Map<String, Set<Template<?>>> rawTemplates;

    protected TemplateLinker() {
        this.templates = new HashSet<>();
        this.collectionTemplates = new HashSet<>();
        this.rawTemplates = new HashMap<>();
    }

    public <T> void implementTemplates(Class<T> clazz, Template<T> template) {
        addTemplate(template);
        addCollectionTemplate(new CollectionTemplate<>(clazz, template, ArrayList::new));
    }

    @NotNull
    private Optional<Template<?>> getTemplate0(@NotNull Set<Template<?>> templateSet, @NotNull Class<?> type) {
        return templateSet.stream().filter(template -> template.isThis(type)).findFirst();
    }

    public boolean addTemplate0(@NotNull Set<Template<?>> templateSet, @NotNull Template<?> template) {
        return templateSet.add(template);
    }

    @NotNull public Optional<Template<?>> getTemplate(Object object) {
        return getTemplate0(templates, object.getClass());
    }

    @NotNull public <T> Optional<Template<T>> getTemplate(@NotNull Class<T> type) {
        //noinspection unchecked
        return getTemplate0(templates, type).map(template -> (Template<T>) template);
    }

    public boolean addTemplate(@NotNull Template<?> template) {
        return addTemplate0(templates, template);
    }

    @NotNull public <T> Optional<CollectionTemplate<T>> getCollectionTemplate(@NotNull Class<T> type) {
        //noinspection unchecked
        return getTemplate0(collectionTemplates, type).map(template -> (CollectionTemplate<T>) template);
    }

    public boolean addCollectionTemplate(@NotNull CollectionTemplate<?> template) {
        return addTemplate0(collectionTemplates, template);
    }

    @NotNull public Optional<Template<?>> getTemplateOf(@NotNull String key, @NotNull Class<?> type) {
        if (!rawTemplates.containsKey(key)) {
            return Optional.empty();
        }
        return getTemplate0(rawTemplates.get(key), type);
    }

    public boolean addTemplateOf(@NotNull String key, @NotNull Template<?> template) {
        if (!rawTemplates.containsKey(key)) {
            rawTemplates.put(key, new HashSet<>());
        }
        return rawTemplates.get(key).add(template);
    }
}
