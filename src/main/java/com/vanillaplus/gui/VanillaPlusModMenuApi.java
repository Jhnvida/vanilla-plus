package com.vanillaplus.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.keybinds.KeybindManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VanillaPlusModMenuApi implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
                return this::createConfigScreen;
        }

        public Screen createConfigScreen(Screen parent) {
                ModConfig config = ConfigManager.getConfig();

                ConfigBuilder builder = ConfigBuilder.create()
                                .setParentScreen(parent)
                                .setTitle(Component.literal("Configurações do VanillaPlus"));

                ConfigEntryBuilder entryBuilder = builder.entryBuilder();

                ConfigCategory automation = builder.getOrCreateCategory(Component.literal("Automação"));

                if (KeybindManager.getToggleAutoBreedKey() != null) {
                        automation.addEntry(entryBuilder
                                        .fillKeybindingField(Component.literal("Tecla de Ativação do Auto Breed"),
                                                        KeybindManager.getToggleAutoBreedKey())
                                        .build());
                }

                automation.addEntry(entryBuilder
                                .startDoubleField(Component.literal("Raio do Auto Breed (blocos)"),
                                                config.autoBreedRange)
                                .setDefaultValue(3.5)
                                .setMin(2.0)
                                .setMax(4.5)
                                .setSaveConsumer(newValue -> config.autoBreedRange = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startIntField(Component.literal("Intervalo entre alimentações (ms)"),
                                                config.autoBreedIntervalMs)
                                .setDefaultValue(350)
                                .setMin(200)
                                .setMax(1000)
                                .setSaveConsumer(newValue -> config.autoBreedIntervalMs = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Procriar Vacas"), config.breedCows)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.breedCows = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Procriar Ovelhas"), config.breedSheep)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.breedSheep = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Procriar Porcos"), config.breedPigs)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.breedPigs = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Procriar Galinhas"), config.breedChickens)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.breedChickens = newValue)
                                .build());

                if (KeybindManager.getToggleAutoReplantKey() != null) {
                        automation.addEntry(entryBuilder
                                        .fillKeybindingField(Component.literal("Tecla de Ativação do Auto Replant"),
                                                        KeybindManager.getToggleAutoReplantKey())
                                        .build());
                }

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Apenas Right-Click Harvest"),
                                                config.rightClickHarvestOnly)
                                .setDefaultValue(false)
                                .setTooltip(Component.literal(
                                                "Requer clique com botão direito na cultura madura para colher e replantar"))
                                .setSaveConsumer(newValue -> config.rightClickHarvestOnly = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startDoubleField(Component.literal("Raio de Varredura (se área ativa)"),
                                                config.autoReplantRadius)
                                .setDefaultValue(3.0)
                                .setMin(2.0)
                                .setMax(4.5)
                                .setSaveConsumer(newValue -> config.autoReplantRadius = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Replantar Trigo"), config.replantWheat)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.replantWheat = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Replantar Cenouras"), config.replantCarrots)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.replantCarrots = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Replantar Batatas"), config.replantPotatoes)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.replantPotatoes = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Replantar Beterrabas"), config.replantBeetroots)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.replantBeetroots = newValue)
                                .build());

                if (KeybindManager.getToggleAutoToolKey() != null) {
                        automation.addEntry(entryBuilder
                                        .fillKeybindingField(Component.literal("Tecla de Ativação do Auto Tool"),
                                                        KeybindManager.getToggleAutoToolKey())
                                        .build());
                }

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Proteger Durabilidade"),
                                                config.autoToolProtectDurability)
                                .setDefaultValue(true)
                                .setTooltip(Component
                                                .literal("Evita usar ferramentas com durabilidade perigosamente baixa"))
                                .setSaveConsumer(newValue -> config.autoToolProtectDurability = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startIntField(Component.literal("Durabilidade Mínima"), config.autoToolMinDurability)
                                .setDefaultValue(5)
                                .setMin(1)
                                .setMax(100)
                                .setTooltip(Component
                                                .literal("Limite mínimo de vida restante para poupar a ferramenta"))
                                .setSaveConsumer(newValue -> config.autoToolMinDurability = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Restaurar Slot Anterior"),
                                                config.autoToolSwitchBack)
                                .setDefaultValue(true)
                                .setTooltip(Component.literal("Restaura o item que estava na mão ao parar de minerar"))
                                .setSaveConsumer(newValue -> config.autoToolSwitchBack = newValue)
                                .build());

                if (KeybindManager.getToggleAutoRefillKey() != null) {
                        automation.addEntry(entryBuilder
                                        .fillKeybindingField(Component.literal("Tecla de Ativação do Auto Refill"),
                                                        KeybindManager.getToggleAutoRefillKey())
                                        .build());
                }

                ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Movimentação"));

                if (KeybindManager.getToggleFreecamKey() != null) {
                        movement.addEntry(entryBuilder
                                        .fillKeybindingField(Component.literal("Tecla de Ativação do Freecam"),
                                                        KeybindManager.getToggleFreecamKey())
                                        .build());
                }

                movement.addEntry(entryBuilder
                                .startFloatField(Component.literal("Velocidade da Câmera"), config.freecamSpeed)
                                .setDefaultValue(1.0f)
                                .setMin(0.2f)
                                .setMax(5.0f)
                                .setSaveConsumer(newValue -> config.freecamSpeed = newValue)
                                .build());

                movement.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Renderizar Corpo do Jogador"),
                                                config.freecamRenderOwnPlayer)
                                .setDefaultValue(true)
                                .setTooltip(Component.literal(
                                                "Mantém o modelo do jogador visível no ponto original durante o Freecam"))
                                .setSaveConsumer(newValue -> config.freecamRenderOwnPlayer = newValue)
                                .build());

                ConfigCategory render = builder.getOrCreateCategory(Component.literal("Visual & Renderização"));

                if (KeybindManager.getToggleFullbrightKey() != null) {
                        render.addEntry(entryBuilder
                                        .fillKeybindingField(Component.literal("Tecla de Ativação do Fullbright"),
                                                        KeybindManager.getToggleFullbrightKey())
                                        .build());
                }

                if (KeybindManager.getToggleLightLevelKey() != null) {
                        render.addEntry(entryBuilder
                                        .fillKeybindingField(Component.literal("Tecla de Ativação do Light Overlay"),
                                                        KeybindManager.getToggleLightLevelKey())
                                        .build());
                }

                render.addEntry(entryBuilder
                                .startIntField(Component.literal("Raio do Overlay (blocos)"),
                                                config.lightLevelOverlayRadius)
                                .setDefaultValue(12)
                                .setMin(4)
                                .setMax(24)
                                .setTooltip(Component.literal(
                                                "Distância horizontal máxima ao redor do jogador para renderizar o overlay"))
                                .setSaveConsumer(newValue -> config.lightLevelOverlayRadius = newValue)
                                .build());

                render.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Mostrar Blocos Seguros"),
                                                config.lightLevelShowSafe)
                                .setDefaultValue(false)
                                .setTooltip(Component.literal(
                                                "Renderiza marcadores verdes/seguros além dos marcadores de perigo"))
                                .setSaveConsumer(newValue -> config.lightLevelShowSafe = newValue)
                                .build());

                builder.setSavingRunnable(() -> {
                        ConfigManager.save();
                        KeyMapping.resetMapping();
                        if (Minecraft.getInstance().options != null) {
                                Minecraft.getInstance().options.save();
                        }
                });

                return builder.build();
        }
}
