package com.vanillaplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VanillaPlus/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "vanillaplus.json");

    private static ModConfig config = new ModConfig();

    public static ModConfig getConfig() {
        return config;
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            LOGGER.info("Arquivo de configuração não encontrado. Criando configurações padrão em {}",
                    CONFIG_FILE.getName());
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) {
                config = loaded;
                LOGGER.info("Configurações do VanillaPlus carregadas com sucesso.");
            }
        } catch (Exception e) {
            LOGGER.error("Falha ao carregar configurações do VanillaPlus. Mantendo valores padrão.", e);
        }
    }

    public static void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
            LOGGER.info("Configurações do VanillaPlus salvas com sucesso em {}", CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Falha ao salvar configurações do VanillaPlus em disco.", e);
        }
    }
}
