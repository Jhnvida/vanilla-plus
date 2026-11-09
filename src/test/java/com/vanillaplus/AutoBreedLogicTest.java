package com.vanillaplus;

import com.vanillaplus.modules.automation.AutoBreedModule;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AutoBreedLogicTest {

    @Test
    @DisplayName("Garante rejeição de itens nulos ou vazios")
    public void testNullOrEmptyItem() {
        assertFalse(AutoBreedModule.isBreedingFood(null, null));
        assertFalse(AutoBreedModule.isBreedingFood(null, ItemStack.EMPTY));
    }

    @Test
    @DisplayName("Valida propriedades e identificadores do módulo AutoBreed")
    public void testModuleMetadata() {
        AutoBreedModule module = new AutoBreedModule();
        assertEquals("auto_breed", module.getId());
        assertEquals("Auto Breed", module.getName());
        assertFalse(module.isEnabled());

        module.toggle();
        assertTrue(module.isEnabled());

        module.toggle();
        assertFalse(module.isEnabled());
    }

    @Test
    @DisplayName("Valida tempo canônico de espera entre reproduções (5 minutos)")
    public void testBreedingCooldownDuration() {
        assertEquals(300_000L, AutoBreedModule.BREEDING_COOLDOWN_MS,
                "Cooldown de reprodução deve ser de 5 minutos (300.000 ms / 6000 ticks vanilla)");
        assertEquals(30_000L, AutoBreedModule.LOVE_MODE_DURATION_MS,
                "Duração do modo de amor deve ser de 30 segundos (30.000 ms / 600 ticks vanilla)");

        int testAnimalId = 99999;
        assertFalse(AutoBreedModule.isEntityCoolingDown(testAnimalId));

        AutoBreedModule.recordCooldown(testAnimalId, AutoBreedModule.BREEDING_COOLDOWN_MS);
        assertTrue(AutoBreedModule.isEntityCoolingDown(testAnimalId),
                "Animal com cooldown registrado deve constar como em espera");
    }
}
