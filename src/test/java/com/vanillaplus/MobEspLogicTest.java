package com.vanillaplus;

import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.modules.render.MobEspColorMode;
import com.vanillaplus.modules.render.MobEspModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MobEspLogicTest {

    @Test
    @DisplayName("Valida metadados e ciclo de vida do MobEspModule")
    public void testModuleMetadata() {
        MobEspModule module = new MobEspModule();
        assertEquals("mob_esp", module.getId());
        assertEquals("Mob ESP", module.getName());
        assertEquals(ModuleCategory.RENDER, module.getCategory());
        assertFalse(module.isEnabled(), "Deve iniciar desativado por padrão");

        module.toggle();
        assertTrue(module.isEnabled(), "Após toggle deve ficar ativo");

        module.toggle();
        assertFalse(module.isEnabled(), "Após segundo toggle deve desativar");
    }

    @Test
    @DisplayName("Valida modos de cor e valores do enum MobEspColorMode")
    public void testColorModes() {
        assertEquals("Branco Minimalista", MobEspColorMode.WHITE.getDisplayName());
        assertEquals("Dinâmico (Por Ameaça)", MobEspColorMode.THREAT_BASED.getDisplayName());

        ModConfig config = new ModConfig();
        config.mobEspColorMode = MobEspColorMode.WHITE;
        assertEquals(MobEspColorMode.WHITE, config.mobEspColorMode);

        config.mobEspColorMode = MobEspColorMode.THREAT_BASED;
        assertEquals(MobEspColorMode.THREAT_BASED, config.mobEspColorMode);
    }

    @Test
    @DisplayName("Valida configurações padrão do Mob ESP")
    public void testDefaultConfig() {
        ModConfig config = new ModConfig();
        assertEquals(24, config.mobEspRange);
        assertTrue(config.mobEspShowName);
        assertTrue(config.mobEspShowDistance);
        assertTrue(config.mobEspShowHealth);
        assertEquals(MobEspColorMode.WHITE, config.mobEspColorMode);

        assertTrue(config.mobEspCreeper);
        assertTrue(config.mobEspSkeleton);
        assertTrue(config.mobEspZombie);
        assertTrue(config.mobEspSpider);
        assertTrue(config.mobEspWitch);
        assertTrue(config.mobEspEnderman);
        assertTrue(config.mobEspSlime);
        assertTrue(config.mobEspPhantom);
        assertTrue(config.mobEspNether);
        assertTrue(config.mobEspIllager);
        assertTrue(config.mobEspBoss);
        assertTrue(config.mobEspOther);
        assertTrue(config.mobEspResponsiveText, "Escala responsiva deve vir ativada por padrão");
        assertEquals(1.0f, config.mobEspTextScale, 0.01f, "Escala padrão de texto deve ser 1.0x");
        assertFalse(config.mobEspPlayers, "Jogadores devem vir desativados por padrão");
    }

    @Test
    @DisplayName("Valida responsividade da escala do label conforme a distância mantendo legibilidade de perto")
    public void testResponsiveLabelScale() {
        ModConfig config = new ModConfig();
        config.mobEspResponsiveText = true;
        config.mobEspTextScale = 1.0f;

        float closeScale = MobEspModule.calculateLabelScale(1.5, config);
        assertTrue(closeScale >= 0.24f, "Em curta distância a escala deve ser suficientemente legível (>= 0.24f)");
        assertTrue(closeScale <= 0.35f, "Em curta distância a escala não deve ultrapassar o limite confortável");

        float midScale = MobEspModule.calculateLabelScale(12.0, config);
        assertTrue(midScale > closeScale, "A escala no espaço 3D deve aumentar com a distância para manter proporção constante na tela");

        float farScale = MobEspModule.calculateLabelScale(35.0, config);
        assertTrue(farScale <= 0.75f, "A escala deve ser limitada a um teto máximo seguro");

        config.mobEspResponsiveText = false;
        float fixedScale = MobEspModule.calculateLabelScale(1.5, config);
        float fixedScaleFar = MobEspModule.calculateLabelScale(35.0, config);
        assertEquals(fixedScale, fixedScaleFar, 0.001f, "Modo não-responsivo deve manter escala de mundo fixa independente da distância");
    }

    @Test
    @DisplayName("Valida renderização dos corações estilo Minecraft para vida dos mobs")
    public void testFormatHearts() {
        String fullHp = MobEspModule.formatHearts(20.0f, 20.0f);
        assertEquals("§c❤§c❤§c❤§c❤§c❤§c❤§c❤§c❤§c❤§c❤", fullHp);

        String zeroHp = MobEspModule.formatHearts(0.0f, 20.0f);
        assertEquals("§8❤§8❤§8❤§8❤§8❤§8❤§8❤§8❤§8❤§8❤", zeroHp);

        String halfHp = MobEspModule.formatHearts(10.0f, 20.0f);
        assertEquals("§c❤§c❤§c❤§c❤§c❤§8❤§8❤§8❤§8❤§8❤", halfHp);

        String partialHp = MobEspModule.formatHearts(15.0f, 20.0f);
        assertEquals("§c❤§c❤§c❤§c❤§c❤§c❤§c❤§6❤§8❤§8❤", partialHp);
    }
}
