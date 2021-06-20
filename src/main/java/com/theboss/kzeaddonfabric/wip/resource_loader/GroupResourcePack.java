package com.theboss.kzeaddonfabric.wip.resource_loader;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resource.ResourceNotFoundException;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class GroupResourcePack implements ResourcePack {
    protected final ResourceType type;
    protected final List<ModNioResourcePack> packs;
    @SuppressWarnings("SpellCheckingInspection")
    protected final Map<String, List<ModNioResourcePack>> namespacedPacks = new Object2ObjectOpenHashMap<>();

    public GroupResourcePack(ResourceType type, List<ModNioResourcePack> packs) {
        this.type = type;
        this.packs = packs;
        this.packs.forEach(pack -> pack.getNamespaces(this.type)
                .forEach(namespace -> this.namespacedPacks.computeIfAbsent(namespace, value -> new ArrayList<>())
                        .add(pack)));
    }

    @Override
    public void close() {
        this.packs.forEach(ResourcePack::close);
    }

    @Override
    public boolean contains(ResourceType type, Identifier id) {
        List<ModNioResourcePack> packs = this.namespacedPacks.get(id.getNamespace());

        if (packs == null) {
            return false;
        }

        for (int i = packs.size() - 1; i >= 0; i--) {
            ResourcePack pack = packs.get(i);

            if (pack.contains(type, id)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        List<ModNioResourcePack> packs = this.namespacedPacks.get(namespace);

        if (packs == null) {
            return Collections.emptyList();
        }

        Set<Identifier> resources = new HashSet<>();

        for (int i = packs.size() - 1; i >= 0; i--) {
            ResourcePack pack = packs.get(i);
            Collection<Identifier> modResources = pack.findResources(type, namespace, prefix, maxDepth, pathFilter);

            resources.addAll(modResources);
        }

        return resources;
    }

    public String getFullName() {
        return this.getName() + " (" + this.packs.stream().map(ResourcePack::getName).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return this.namespacedPacks.keySet();
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException {
        List<ModNioResourcePack> packs = this.namespacedPacks.get(id.getNamespace());

        if (packs != null) {
            for (int i = packs.size() - 1; i >= 0; i--) {
                ResourcePack pack = packs.get(i);

                if (pack.contains(type, id)) {
                    return pack.open(type, id);
                }
            }
        }

        throw new ResourceNotFoundException(null, String.format("%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath()));
    }
}
