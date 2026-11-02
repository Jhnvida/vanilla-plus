package com.vanillaplus.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
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

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Ativar Auto Breed"), config.autoBreedEnabled)
                                .setDefaultValue(false)
                                .setTooltip(Component.literal("Alimenta animais elegíveis próximos automaticamente"))
                                .setSaveConsumer(newValue -> config.autoBreedEnabled = newValue)
                                .build());

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

                automation.addEntry(
                                entryBuilder.startBooleanToggle(Component.literal("Procriar Vacas"), config.breedCows)
                                                .setDefaultValue(true)
                                                .setSaveConsumer(newValue -> config.breedCows = newValue)
                                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Procriar Ovelhas"), config.breedSheep)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.breedSheep = newValue)
                                .build());

                automation.addEntry(
                                entryBuilder.startBooleanToggle(Component.literal("Procriar Porcos"), config.breedPigs)
                                                .setDefaultValue(true)
                                                .setSaveConsumer(newValue -> config.breedPigs = newValue)
                                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Procriar Galinhas"), config.breedChickens)
                                .setDefaultValue(true)
                                .setSaveConsumer(newValue -> config.breedChickens = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Ativar Auto Replant"), config.autoReplantEnabled)
                                .setDefaultValue(false)
                                .setTooltip(Component.literal("Colhe e replanta automaticamente plantações maduras"))
                                .setSaveConsumer(newValue -> config.autoReplantEnabled = newValue)
                                .build());

                automation.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Apenas Right-Click Harvest"),
                                                config.rightClickHarvestOnly)
                                .setDefaultValue(true)
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

                ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Movimentação"));

                movement.addEntry(entryBuilder
                                .startBooleanToggle(Component.literal("Ativar Freecam"), config.freecamEnabled)
                                .setDefaultValue(false)
                                .setTooltip(Component.literal("Permite inspecionar o mundo com câmera independente"))
                                .setSaveConsumer(newValue -> config.freecamEnabled = newValue)
                                .build());

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

                builder.setSavingRunnable(ConfigManager::save);

                return builder.build();
        }
}
