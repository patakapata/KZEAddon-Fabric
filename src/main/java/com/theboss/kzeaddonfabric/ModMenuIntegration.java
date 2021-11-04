package com.theboss.kzeaddonfabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    // TODO ModMenu互換のコンフィグボタン

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModUtils::createConfigScreen;
    }
}
