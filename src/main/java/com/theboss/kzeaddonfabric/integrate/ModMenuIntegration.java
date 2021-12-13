package com.theboss.kzeaddonfabric.integrate;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.theboss.kzeaddonfabric.utils.ModUtils;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModUtils::createConfigScreen;
    }
}
