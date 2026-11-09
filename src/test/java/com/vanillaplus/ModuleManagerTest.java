package com.vanillaplus;

import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.modules.automation.AutoBreedModule;
import com.vanillaplus.modules.automation.AutoMineModule;
import com.vanillaplus.modules.automation.AutoRefillModule;
import com.vanillaplus.modules.automation.AutoReplantModule;
import com.vanillaplus.modules.automation.AutoToolModule;
import com.vanillaplus.modules.movement.FreecamModule;
import com.vanillaplus.modules.render.AutoBreedIndicatorModule;
import com.vanillaplus.modules.render.CropIndicatorModule;
import com.vanillaplus.modules.render.FullbrightModule;
import com.vanillaplus.modules.render.LightLevelOverlayModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleManagerTest {

    @Test
    @DisplayName("Valida registro, recuperação e categorização de módulos")
    public void testModuleManagerRegistration() {
        ModuleManager manager = new ModuleManager();

        AutoBreedModule autoBreed = new AutoBreedModule();
        AutoReplantModule autoReplant = new AutoReplantModule();
        AutoToolModule autoTool = new AutoToolModule();
        AutoRefillModule autoRefill = new AutoRefillModule();
        FreecamModule freecam = new FreecamModule();
        FullbrightModule fullbright = new FullbrightModule();
        LightLevelOverlayModule lightOverlay = new LightLevelOverlayModule();
        AutoBreedIndicatorModule breedIndicator = new AutoBreedIndicatorModule();
        AutoMineModule autoMine = new AutoMineModule();
        CropIndicatorModule cropIndicator = new CropIndicatorModule();

        manager.registerModule(autoBreed);
        manager.registerModule(autoReplant);
        manager.registerModule(autoTool);
        manager.registerModule(autoRefill);
        manager.registerModule(freecam);
        manager.registerModule(fullbright);
        manager.registerModule(lightOverlay);
        manager.registerModule(breedIndicator);
        manager.registerModule(autoMine);
        manager.registerModule(cropIndicator);

        assertEquals(10, manager.getModules().size());
        assertTrue(manager.getModule("auto_breed").isPresent());
        assertTrue(manager.getModule("auto_replant").isPresent());
        assertTrue(manager.getModule("auto_tool").isPresent());
        assertTrue(manager.getModule("auto_refill").isPresent());
        assertTrue(manager.getModule("freecam").isPresent());
        assertTrue(manager.getModule("fullbright").isPresent());
        assertTrue(manager.getModule("light_level_overlay").isPresent());
        assertTrue(manager.getModule("breed_indicator").isPresent());
        assertTrue(manager.getModule("auto_mine").isPresent());
        assertTrue(manager.getModule("crop_indicator").isPresent());
        assertFalse(manager.getModule("invalid_id").isPresent());

        assertEquals(5, manager.getModulesByCategory(ModuleCategory.AUTOMATION).size());
        assertEquals(1, manager.getModulesByCategory(ModuleCategory.MOVEMENT).size());
        assertEquals(4, manager.getModulesByCategory(ModuleCategory.RENDER).size());
        assertEquals(0, manager.getModulesByCategory(ModuleCategory.GENERAL).size());
    }

    @Test
    @DisplayName("Garante exibição correta do modo ativo do AutoReplant no HUD ([click] ou [area])")
    public void testAutoReplantDisplayNameModes() {
        AutoReplantModule autoReplant = new AutoReplantModule();
        com.vanillaplus.config.ModConfig config = com.vanillaplus.config.ConfigManager.getConfig();

        config.rightClickHarvestOnly = true;
        assertEquals("Auto Replant [click]", autoReplant.getDisplayName(),
                "Quando rightClickHarvestOnly for true, deve exibir apenas [click]");

        config.rightClickHarvestOnly = false;
        assertEquals("Auto Replant [area]", autoReplant.getDisplayName(),
                "Quando rightClickHarvestOnly for false, deve exibir apenas [area]");
    }

    @Test
    @DisplayName("Valida estados, cálculo de AABB e reset do AutoMine")
    public void testAutoMineLifecycleAndBoundingBox() {
        AutoMineModule autoMine = new AutoMineModule();
        assertEquals("auto_mine", autoMine.getId());
        assertEquals(ModuleCategory.AUTOMATION, autoMine.getCategory());
        assertEquals("Auto Mine", autoMine.getDisplayName());
        assertEquals(AutoMineModule.MineState.IDLE, autoMine.getMineState());

        net.minecraft.core.BlockPos p1 = new net.minecraft.core.BlockPos(10, 64, 10);
        net.minecraft.core.BlockPos p2 = new net.minecraft.core.BlockPos(12, 66, 15);
        net.minecraft.world.phys.AABB box = AutoMineModule.createAABB(p1, p2);

        assertEquals(10.0, box.minX, 0.001);
        assertEquals(64.0, box.minY, 0.001);
        assertEquals(10.0, box.minZ, 0.001);
        assertEquals(13.0, box.maxX, 0.001);
        assertEquals(67.0, box.maxY, 0.001);
        assertEquals(16.0, box.maxZ, 0.001);

        net.minecraft.world.phys.AABB reversedBox = AutoMineModule.createAABB(p2, p1);
        assertEquals(box, reversedBox, "A caixa delimitadora deve ser invariante à ordem dos pontos");

        autoMine.setPos1(p1);
        autoMine.setPos2(p2);
        assertEquals(p1, autoMine.getPos1());
        assertEquals(p2, autoMine.getPos2());

        autoMine.resetSelection();
        assertNull(autoMine.getPos1(), "Reset deve limpar pos1");
        assertNull(autoMine.getPos2(), "Reset deve limpar pos2");
    }

    @Test
    @DisplayName("Valida metadados e ciclo do CropIndicatorModule")
    public void testCropIndicatorMetadata() {
        CropIndicatorModule module = new CropIndicatorModule();
        assertEquals("crop_indicator", module.getId());
        assertEquals("Crop Indicator", module.getName());
        assertEquals(ModuleCategory.RENDER, module.getCategory());
        assertFalse(module.isEnabled());

        module.toggle();
        assertTrue(module.isEnabled());

        module.toggle();
        assertFalse(module.isEnabled());
    }
}
