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
}
