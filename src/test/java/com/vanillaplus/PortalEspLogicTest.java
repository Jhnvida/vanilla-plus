package com.vanillaplus;

import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.modules.render.PortalEspModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PortalEspLogicTest {

    @Test
    @DisplayName("Valida metadados e ciclo de vida do PortalEspModule")
    public void testModuleMetadata() {
        PortalEspModule module = new PortalEspModule();
        assertEquals("portal_esp", module.getId());
        assertEquals("Portal ESP", module.getName());
        assertEquals(ModuleCategory.RENDER, module.getCategory());
        assertFalse(module.isEnabled(), "Deve iniciar desativado por padrão");

        module.toggle();
        assertTrue(module.isEnabled());

        module.toggle();
        assertFalse(module.isEnabled());
    }

    @Test
    @DisplayName("Valida agrupamento (clustering) de blocos contíguos de portal em caixa 3D unificada")
    public void testPortalClustering() {
        Map<BlockPos, PortalEspModule.PortalType> blocks = new HashMap<>();

        for (int y = 64; y <= 66; y++) {
            for (int z = 20; z <= 21; z++) {
                blocks.put(new BlockPos(10, y, z), PortalEspModule.PortalType.NETHER);
            }
        }

        blocks.put(new BlockPos(110, 70, 50), PortalEspModule.PortalType.NETHER);

        List<PortalEspModule.PortalCluster> clusters = PortalEspModule.clusterPortalBlocks(blocks);
        assertEquals(2, clusters.size(), "Deve agrupar os blocos conectados em 2 portais distintos");

        PortalEspModule.PortalCluster mainPortal = clusters.stream()
                .filter(c -> c.blockCount() == 6)
                .findFirst()
                .orElse(null);

        assertNotNull(mainPortal, "O portal de 6 blocos deve ter sido agrupado corretamente");
        AABB bounds = mainPortal.bounds();
        assertEquals(10.0, bounds.minX);
        assertEquals(11.0, bounds.maxX);
        assertEquals(64.0, bounds.minY);
        assertEquals(67.0, bounds.maxY);
        assertEquals(20.0, bounds.minZ);
        assertEquals(22.0, bounds.maxZ);
    }

    @Test
    @DisplayName("Valida configurações padrão do Portal ESP")
    public void testDefaultConfig() {
        ModConfig config = new ModConfig();
        assertFalse(config.portalEspEnabled, "Deve vir desativado por padrão");
        assertEquals(64, config.portalEspRadius, "Raio padrão deve ser 64m");
        assertTrue(config.portalEspNether, "Detecção do Nether deve vir ativada");
        assertTrue(config.portalEspEnd, "Detecção do End deve vir ativada");
        assertTrue(config.portalEspShowDistance, "Exibir distância deve vir ativado");
        assertTrue(config.portalEspBoxFill, "Preenchimento 3D deve vir ativado");
    }
}
