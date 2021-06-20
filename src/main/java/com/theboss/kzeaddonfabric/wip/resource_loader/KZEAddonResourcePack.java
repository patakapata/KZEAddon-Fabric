package com.theboss.kzeaddonfabric.wip.resource_loader;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class KZEAddonResourcePack extends GroupResourcePack {
    public KZEAddonResourcePack(ResourceType type, List<ModNioResourcePack> packs) {
        super(type, packs);
    }

    @Override
    public String getName() {
        return "KZEAddon-Fabric";
    }

    @Override
    public InputStream openRoot(String fileName) throws IOException {
        if ("pack.mcmeta".equals(fileName)) {
            String description = "Mod resources.";
            String pack = String.format("{\"pack\":{\"pack_format\":" + ModResourcePackCreator.PACK_FORMAT_VERSION + ",\"description\":\"%s\"}}", description);
            return IOUtils.toInputStream(pack, Charsets.UTF_8);
        } else if ("pack.png".equals(fileName)) {
            InputStream stream = FabricLoader.getInstance().getModContainer("kzeaddon-fabric")
                    .flatMap(container -> container.getMetadata().getIconPath(512).map(container::getPath))
                    .filter(Files::exists)
                    .map(iconPath -> {
                        try {
                            return Files.newInputStream(iconPath);
                        } catch (Exception ex) {
                            return null;
                        }
                    }).orElse(null);

            if (stream != null) {
                return stream;
            }
        }

        throw new FileNotFoundException("\"" + fileName + "\" in KZEAddon mod resource pack");
    }

    @Override
    public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
        try {
            InputStream inputStream = this.openRoot("pack.mcmeta");
            Throwable error = null;
            T metadata;

            try {
                metadata = AbstractFileResourcePack.parseMetadata(metaReader, inputStream);
            } catch (Exception ex) {
                error = ex;
                throw ex;
            } finally {
                if (inputStream != null) {
                    if (error != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable ex) {
                            error.addSuppressed(ex);
                        }
                    } else {
                        inputStream.close();
                    }
                }
            }

            return metadata;
        } catch (Exception ex) {
            return null;
        }
    }
}
