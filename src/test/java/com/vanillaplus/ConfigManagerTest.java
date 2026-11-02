package com.vanillaplus;

import com.vanillaplus.config.ModConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigManagerTest {

    @Test
    @DisplayName("Garante valores padrão seguros do ModConfig")
    public void testDefaultConfigValues() {
        ModConfig config = new ModConfig();

        assertFalse(config.autoBreedEnabled, "AutoBreed deve iniciar desativado");
        assertFalse(config.autoReplantEnabled, "AutoReplant deve iniciar desativado");
        assertFalse(config.freecamEnabled, "Freecam deve iniciar desativado");

        assertTrue(config.autoBreedRange <= 4.5, "Alcance do AutoBreed não deve exceder reach vanilla");
        assertTrue(config.autoBreedIntervalMs >= 200, "Delay do AutoBreed deve respeitar rate limit");
        assertTrue(config.rightClickHarvestOnly, "AutoReplant deve preferir Right-Click Harvest por padrão");
        assertEquals(1.0f, config.freecamSpeed, 0.01f, "Velocidade padrão do Freecam deve ser 1.0");
    }
}
