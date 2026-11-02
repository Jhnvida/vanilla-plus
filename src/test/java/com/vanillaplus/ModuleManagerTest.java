package com.vanillaplus;

import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.modules.automation.AutoBreedModule;
import com.vanillaplus.modules.automation.AutoFishModule;
import com.vanillaplus.modules.automation.AutoRefillModule;
import com.vanillaplus.modules.automation.AutoReplantModule;
import com.vanillaplus.modules.automation.AutoToolModule;
import com.vanillaplus.modules.movement.FreecamModule;
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
        AutoFishModule autoFish = new AutoFishModule();
        AutoRefillModule autoRefill = new AutoRefillModule();
        FreecamModule freecam = new FreecamModule();
        FullbrightModule fullbright = new FullbrightModule();
        LightLevelOverlayModule lightOverlay = new LightLevelOverlayModule();

        manager.registerModule(autoBreed);
        manager.registerModule(autoReplant);
        manager.registerModule(autoTool);
        manager.registerModule(autoFish);
        manager.registerModule(autoRefill);
        manager.registerModule(freecam);
        manager.registerModule(fullbright);
        manager.registerModule(lightOverlay);

        assertEquals(8, manager.getModules().size());
        assertTrue(manager.getModule("auto_breed").isPresent());
        assertTrue(manager.getModule("auto_replant").isPresent());
        assertTrue(manager.getModule("auto_tool").isPresent());
        assertTrue(manager.getModule("auto_fish").isPresent());
        assertTrue(manager.getModule("auto_refill").isPresent());
        assertTrue(manager.getModule("freecam").isPresent());
        assertTrue(manager.getModule("fullbright").isPresent());
        assertTrue(manager.getModule("light_level_overlay").isPresent());
        assertFalse(manager.getModule("invalid_id").isPresent());

        assertEquals(5, manager.getModulesByCategory(ModuleCategory.AUTOMATION).size());
        assertEquals(1, manager.getModulesByCategory(ModuleCategory.MOVEMENT).size());
        assertEquals(2, manager.getModulesByCategory(ModuleCategory.RENDER).size());
        assertEquals(0, manager.getModulesByCategory(ModuleCategory.GENERAL).size());
    }
}
