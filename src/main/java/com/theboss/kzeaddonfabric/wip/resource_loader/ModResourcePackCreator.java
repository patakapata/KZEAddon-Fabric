package com.theboss.kzeaddonfabric.wip.resource_loader;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ModResourcePackCreator implements ResourcePackProvider {
    public static final ResourcePackSource RESOURCE_PACK_SOURCE = text -> new TranslatableText("pack.nameAndSource", text, new TranslatableText("pack.source.kzeaddon"));
    public static final int PACK_FORMAT_VERSION = SharedConstants.getGameVersion().getPackVersion();
    public static final ModResourcePackCreator CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackCreator(ResourceType.CLIENT_RESOURCES);
    private static final Set<Pair<String, ModNioResourcePack>> builtinResourcePacks = new HashSet<>();
    public static Pair<String, ModNioResourcePack> builtinResourcePack;
    private final ResourceType type;

    public static void appendModResourcePacks(List<ModNioResourcePack> packs, ResourceType type, @Nullable String subPath) {
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            if (container.getMetadata().getType().equals("builtin")) {
                continue;
            }

            Path path = container.getRootPath();

            if (subPath != null) {
                Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).toAbsolutePath().normalize();

                if (!childPath.startsWith(path) || !Files.exists(childPath)) {
                    continue;
                }

                path = childPath;
            }

            ModNioResourcePack pack = new ModNioResourcePack(container.getMetadata(), path, null, ModNioResourcePack.ActivationType.ALWAYS_ENABLED);

            if (!pack.getNamespaces(type).isEmpty()) {
                packs.add(pack);
            }
        }
    }

    public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, ModNioResourcePack.ActivationType activationType) {
        String separator = container.getRootPath().getFileSystem().getSeparator();
        subPath = subPath.replace("/", separator);

        Path resourcePackPath = container.getRootPath().resolve(subPath).toAbsolutePath().normalize();

        if (!Files.exists(resourcePackPath)) {
            return false;
        }

        String name = id.getNamespace() + "/" + id.getPath();
        builtinResourcePack = new Pair<>(name, new ModNioResourcePack(container.getMetadata(), resourcePackPath, null, activationType));

        return true;
    }


    public static void registerBuiltinResourcePacks(ResourceType resourceType, Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory) {
        // Loop through each registered built-in resource packs and add them if valid.
        for (Pair<String, ModNioResourcePack> entry : builtinResourcePacks) {
            ModNioResourcePack pack = entry.getRight();

            // Add the built-in pack only if namespaces for the specified resource type are present.
            if (!pack.getNamespaces(resourceType).isEmpty()) {
                // Make the resource pack profile for built-in pack, should never be always enabled.
                ResourcePackProfile profile = ResourcePackProfile.of(entry.getLeft(),
                        pack.getActivationType() == ModNioResourcePack.ActivationType.ALWAYS_ENABLED,
                        entry::getRight, factory, ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.PACK_SOURCE_BUILTIN);
                if (profile != null) {
                    consumer.accept(profile);
                }
            }
        }
    }

    public ModResourcePackCreator(ResourceType type) {
        this.type = type;
    }

    @Override
    public void register(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory) {
        // Modのリソパのリストを作成
        List<ModNioResourcePack> packs = new ArrayList<>();
        appendModResourcePacks(packs, this.type, null);

        if (!packs.isEmpty()) {
            // Modリソパ用のリソパプロフィール作成
            ResourcePackProfile profile = ResourcePackProfile.of("KZEAddon-Fabric", true, () -> new KZEAddonResourcePack(this.type, packs), factory, ResourcePackProfile.InsertionPosition.TOP, RESOURCE_PACK_SOURCE);
            if (profile != null) {
                profileAdder.accept(profile);
            }
        }

        registerBuiltinResourcePacks(this.type, profileAdder, factory);
    }
}
