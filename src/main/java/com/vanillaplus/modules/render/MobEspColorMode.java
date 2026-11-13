package com.vanillaplus.modules.render;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum MobEspColorMode {
    WHITE("Branco Minimalista"),
    THREAT_BASED("Dinâmico (Por Ameaça)");

    private final String displayName;

    MobEspColorMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
