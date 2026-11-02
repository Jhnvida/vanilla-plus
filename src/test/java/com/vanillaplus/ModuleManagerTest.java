package com.vanillaplus;

import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.modules.automation.AutoBreedModule;
import com.vanillaplus.modules.automation.AutoReplantModule;
import com.vanillaplus.modules.movement.FreecamModule;
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
        FreecamModule freecam = new FreecamModule();

        manager.registerModule(autoBreed);
        manager.registerModule(autoReplant);
        manager.registerModule(freecam);

        assertEquals(3, manager.getModules().size());
        assertTrue(manager.getModule("auto_breed").isPresent());
        assertTrue(manager.getModule("auto_replant").isPresent());
        assertTrue(manager.getModule("freecam").isPresent());
        assertFalse(manager.getModule("invalid_id").isPresent());

        assertEquals(2, manager.getModulesByCategory(ModuleCategory.AUTOMATION).size());
        assertEquals(1, manager.getModulesByCategory(ModuleCategory.MOVEMENT).size());
        assertEquals(0, manager.getModulesByCategory(ModuleCategory.GENERAL).size());
    }
}
