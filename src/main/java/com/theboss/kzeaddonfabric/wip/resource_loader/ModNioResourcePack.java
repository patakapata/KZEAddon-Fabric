package com.theboss.kzeaddonfabric.wip.resource_loader;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ModNioResourcePack extends AbstractFileResourcePack implements ModResourcePack {
    private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_]]");
    private final ModMetadata modInfo;
    private final Path basePath;
    private final AutoCloseable closer;
    private final String separator;
    private final ActivationType activationType;

    public ModNioResourcePack(ModMetadata modInfo, Path basePath, AutoCloseable closer, ActivationType activationType) {
        super(null);
        this.modInfo = modInfo;
        this.basePath = basePath;
        this.closer = closer;
        this.separator = basePath.getFileSystem().getSeparator();
        this.activationType = activationType;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean contains(ResourceType type, Identifier id) {
        return false;
    }

    @Override
    protected boolean containsFile(String name) {
        return false;
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        return null;
    }

    public ActivationType getActivationType() {
        return this.activationType;
    }

    @Override
    public ModMetadata getFabricModMetadata() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return null;
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException {
        return null;
    }

    @Override
    protected InputStream openFile(String name) throws IOException {
        return null;
    }

    @Override
    public InputStream openRoot(String fileName) throws IOException {
        return null;
    }

    @Override
    public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
        return null;
    }

    public enum ActivationType {
        NORMAL, DEFAULT_ENABLED, ALWAYS_ENABLED;

        public boolean isEnabledByDefault(ActivationType type) { return type == DEFAULT_ENABLED || type == ALWAYS_ENABLED; }
    }
}
