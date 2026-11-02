package com.vanillaplus.core;

import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class Module {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;

    public Module(String id, String name, String description, ModuleCategory category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return getName();
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onClientTick(Minecraft client) {
    }

    public void onDisconnect() {
        if (isEnabled()) {
            setEnabled(false);
        }
    }
}
