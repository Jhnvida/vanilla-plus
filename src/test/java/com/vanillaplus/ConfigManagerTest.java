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

        assertTrue(config.showStatusHud, "HUD de status deve iniciar ativado por padrão");
        assertFalse(config.autoBreedEnabled, "AutoBreed deve iniciar desativado");
        assertFalse(config.autoReplantEnabled, "AutoReplant deve iniciar desativado");
        assertFalse(config.freecamEnabled, "Freecam deve iniciar desativado");
        assertFalse(config.autoToolEnabled, "AutoTool deve iniciar desativado");
        assertFalse(config.autoRefillEnabled, "AutoRefill deve iniciar desativado");
        assertFalse(config.fullbrightEnabled, "Fullbright deve iniciar desativado");
        assertFalse(config.lightLevelOverlayEnabled, "LightLevelOverlay deve iniciar desativado");
        assertFalse(config.autoBreedIndicatorEnabled, "BreedIndicator deve iniciar desativado");

        assertTrue(config.autoBreedRange <= 4.5, "Alcance do AutoBreed não deve exceder reach vanilla");
        assertTrue(config.autoBreedIntervalMs >= 200, "Delay do AutoBreed deve respeitar rate limit");
        assertFalse(config.rightClickHarvestOnly, "AutoReplant deve realizar colheita por proximidade e por clique");
        assertEquals(1.0f, config.freecamSpeed, 0.01f, "Velocidade padrão do Freecam deve ser 1.0");
        assertTrue(config.autoToolProtectDurability, "Proteção de durabilidade do AutoTool deve vir ativa por padrão");
        assertTrue(config.autoToolMinDurability >= 1, "Durabilidade mínima do AutoTool deve ser maior ou igual a 1");
        assertTrue(config.autoBreedIndicatorRange >= 4, "Raio do BreedIndicator deve ser no mínimo 4");
        assertEquals(com.vanillaplus.modules.render.BreedIndicatorStyle.ITEM_3D, config.breedIndicatorStyle,
                "Estilo padrão do indicador de breed deve ser ITEM_3D");
    }

    @Test
    @DisplayName("Fallback gracioso ao deserializar configurações antigas ou estilos removidos")
    public void testLegacyBreedIndicatorStyleDeserialization() {
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .registerTypeAdapter(com.vanillaplus.modules.render.BreedIndicatorStyle.class,
                        (com.google.gson.JsonDeserializer<com.vanillaplus.modules.render.BreedIndicatorStyle>) (json,
                                typeOfT, ctx) -> {
                            if (json != null && json.isJsonPrimitive()) {
                                try {
                                    return com.vanillaplus.modules.render.BreedIndicatorStyle
                                            .valueOf(json.getAsString());
                                } catch (IllegalArgumentException ignored) {
                                    return com.vanillaplus.modules.render.BreedIndicatorStyle.ITEM_3D;
                                }
                            }
                            return com.vanillaplus.modules.render.BreedIndicatorStyle.ITEM_3D;
                        })
                .create();

        String legacyJsonWithHeart = "{\"breedIndicatorStyle\":\"HEART\"}";
        ModConfig loaded = gson.fromJson(legacyJsonWithHeart, ModConfig.class);
        assertNotNull(loaded);
        assertEquals(com.vanillaplus.modules.render.BreedIndicatorStyle.ITEM_3D, loaded.breedIndicatorStyle,
                "Estilo antigo HEART deve recair suavemente para ITEM_3D");
    }
}
