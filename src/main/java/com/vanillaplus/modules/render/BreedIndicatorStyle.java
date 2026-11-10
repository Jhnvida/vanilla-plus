package com.vanillaplus.modules.render;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum BreedIndicatorStyle {
    ITEM_3D("Item 3D Flutuante");

    private final String displayName;

    BreedIndicatorStyle(String displayName) {
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
