package com.vanillaplus.core;

import com.vanillaplus.keybinds.KeybindManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ModuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VanillaPlus/ModuleManager");
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public ModuleManager() {
    }

    public void registerModule(Module module) {
        modules.put(module.getId(), module);
        LOGGER.info("Módulo registrado: {} [{}]", module.getName(), module.getId());
    }

    public Optional<Module> getModule(String id) {
        return Optional.ofNullable(modules.get(id));
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> list = new ArrayList<>();
        for (Module m : modules.values()) {
            if (m.getCategory() == category) {
                list.add(m);
            }
        }
        return list;
    }

    public void initEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) {
                return;
            }

            KeybindManager.handleKeyInputs(this);

            for (Module module : modules.values()) {
                if (module.isEnabled()) {
                    try {
                        module.onClientTick(client);
                    } catch (Exception e) {
                        LOGGER.error("Erro durante tick do módulo {}", module.getId(), e);
                    }
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("Desconectado da sessão. Resetando módulos ativos.");
            for (Module module : modules.values()) {
                module.onDisconnect();
            }
        });
    }
}
