package com.vanillaplus.modules.render;

import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;

public class FullbrightModule extends Module {
    private static FullbrightModule instance;

    public FullbrightModule() {
        super("fullbright", "Fullbright", "Aumenta a visibilidade no escuro iluminando o ambiente", ModuleCategory.RENDER);
        instance = this;
    }

    public static FullbrightModule getInstance() {
        return instance;
    }
}
