package com.vanillaplus.core;

public enum ModuleCategory {
    AUTOMATION("Automação", "Módulos para automação de tarefas cotidianas e agrícolas"),
    MOVEMENT("Movimentação & Exploração", "Módulos de câmera, navegação e exploração"),
    GENERAL("Geral", "Opções gerais e configurações do mod");

    private final String displayName;
    private final String description;

    ModuleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
