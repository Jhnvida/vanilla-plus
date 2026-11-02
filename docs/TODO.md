# VanillaPlus: Acompanhamento de Desenvolvimento (TODO.md)

Este documento registra todas as fases, tarefas e status de implementação do mod **VanillaPlus** para **Minecraft 26.3** (Fabric).

---

## Legenda de Status

- 🟩 **Concluído**
- 🟨 **Em Andamento**
- ⬜ **Pendente**

---

## Fase 1: Organização da Documentação e Pesquisa

**Objetivo:** Estruturar a documentação técnica, mapear requisitos de Minecraft 26.3, definir a arquitetura e estabelecer o plano de implementação.

- [x] 🟩 **1.1 Criar diretório `docs/` e arquivos base**
    - _Status:_ Estrutura de documentação limpa e versionada.
- [x] 🟩 **1.2 Pesquisa técnica do ecossistema 26.3** ([`docs/pesquisa_tecnica.md`](file:///c:/Users/joaov/Desktop/vanilla-plus/docs/pesquisa_tecnica.md))
    - _Status:_ Identificadas arquitetura nativa Mojmap do 26.3, ausência de Yarn oficial e solução com Identity Mappings.
- [x] 🟩 **1.3 Refinamento da especificação** ([`docs/especificacao_refinada.md`](file:///c:/Users/joaov/Desktop/vanilla-plus/docs/especificacao_refinada.md))
    - _Status:_ Regras de negócio, espécies e culturas suportadas formalizadas.
- [x] 🟩 **1.4 Documentação arquitetural** ([`docs/arquitetura.md`](file:///c:/Users/joaov/Desktop/vanilla-plus/docs/arquitetura.md))
    - _Status:_ Padrão de módulos, ciclo de vida e Mixins mapeados.
- [x] 🟩 **1.5 Backlog e controle de tarefas** ([`docs/TODO.md`](file:///c:/Users/joaov/Desktop/vanilla-plus/docs/TODO.md))
    - _Status:_ Acompanhamento contínuo em tempo real.

---

## Fase 2: Ambiente de Desenvolvimento e Scaffolding

**Objetivo:** Configurar o ambiente Java 25, gerar a estrutura de build Gradle e mapeamentos customizados para Minecraft 26.3.

- [x] 🟩 **2.1 Instalação e validação do JDK 25**
    - _Status:_ `Eclipse Temurin OpenJDK 25.0.5 LTS` instalado e verificado.
- [x] 🟩 **2.2 Estrutura Gradle e Fabric Loom para 26.3**
    - _Status:_ Gradle 9.8.2 e Fabric Loom 1.19.3 configurados com o wrapper `gradlew.bat`.
- [x] 🟩 **2.3 Solução de Mapeamentos para Minecraft 26.3**
    - _Status:_ Criado `mappings/mappings-26.3.jar` com os 3 namespaces (`official`, `intermediary`, `named`) para consumo direto do cliente unobfuscated da Mojang.
- [x] 🟩 **2.4 Metadados do mod e recursos**
    - _Status:_ `fabric.mod.json` e `vanillaplus.mixins.json` configurados com target Java 25 e Minecraft >= 26.3.
- [x] 🟩 **2.5 Classe principal do mod**
    - _Status:_ [`com.vanillaplus.VanillaPlusClientMod`](file:///c:/Users/joaov/Desktop/vanilla-plus/src/main/java/com/vanillaplus/VanillaPlusClientMod.java) inicializando módulos e configurações.

---

## Fase 3: Core do Sistema de Módulos e Configuração

**Objetivo:** Implementar a arquitetura base que suporta ativação, desativação, persistência e atalhos de teclado.

- [x] 🟩 **3.1 Classe base abstrata `Module` e enum `ModuleCategory`**
    - _Status:_ Implementadas em [`com.vanillaplus.core`](file:///c:/Users/joaov/Desktop/vanilla-plus/src/main/java/com/vanillaplus/core/).
- [x] 🟩 **3.2 Gerenciador `ModuleManager`**
    - _Status:_ Despachante de ticks conectado ao `ClientTickEvents.END_CLIENT_TICK` e reset no `ClientPlayConnectionEvents.DISCONNECT`.
- [x] 🟩 **3.3 Sistema de Configuração `ModConfig` e `ConfigManager`**
    - _Status:_ Serialização e desserialização atômica em JSON (`config/vanillaplus.json`).
- [x] 🟩 **3.4 Gerenciador de Atalhos `KeybindManager`**
    - _Status:_ Registrado via `KeyMappingHelper` do Fabric API com a nova API `KeyMapping` do Minecraft 26.3.

---

## Fase 4: Implementação do Módulo Auto Breed

**Objetivo:** Permitir a reprodução automatizada e segura de animais próximos.

- [x] 🟩 **4.1 Algoritmo de elegibilidade de animais**
    - _Status:_ Suporte a `Cow`, `Sheep`, `Pig`, `Chicken` usando os novos subpacotes do 26.3 (`animal.cow.Cow`, etc.), validando `isBaby()`, `getAge() == 0` e `isInLove()`.
- [x] 🟩 **4.2 Lógica de alimentação e consumo de itens**
    - _Status:_ Interação segura via `client.gameMode.interact(...)` com `InteractionHand` e animação nativa `SwingAnimation.DEFAULT`.
- [x] 🟩 **4.3 Rate Limiting e prevenção de kicks**
    - _Status:_ Temporizador `autoBreedIntervalMs` (350ms) e alcance delimitado a `autoBreedRange` (3.5 blocos).

---

## Fase 5: Implementação do Módulo Auto Replant

**Objetivo:** Auxiliar na colheita e replantio contínuo de plantações maduras.

- [x] 🟩 **5.1 Detecção de culturas maduras**
    - _Status:_ Suporte a `CropBlock.isMaxAge()` para Trigo, Cenoura, Batata e Beterraba.
- [x] 🟩 **5.2 Mecânica Right-Click Harvest**
    - _Status:_ Colheita e replantio atômico preservando o solo arado via `startDestroyBlock` e `useItemOn`.
- [x] 🟩 **5.3 Modo opcional de varredura por raio**
    - _Status:_ Varredura de área configurável com checagem de inventário de sementes.

---

## Fase 6: Implementação do Módulo Freecam

**Objetivo:** Proporcionar uma visão de câmera livre desacoplada do personagem.

- [x] 🟩 **6.1 Entidade/Estado virtual de câmera e Mixin em `Camera`**
    - _Status:_ `FreecamModule` e `CameraMixin` implementados integrados com a nova API `DeltaTracker` do Minecraft 26.3.
- [x] 🟩 **6.2 Bloqueio de pacotes de movimentação física do jogador**
    - _Status:_ `LocalPlayerMixin` injetado em `LocalPlayer.sendPosition()` cancelando o envio enquanto a câmera livre está ativa.
- [x] 🟩 **6.3 Interpolação suave e controles de voo**
    - _Status:_ Movimentação tridimensional suave utilizando as teclas configuradas no `Options` do cliente.

---

## Fase 7: Interface Gráfica, Configurações e Distribuição

**Objetivo:** Integrar menus amigáveis ao usuário e empacotar o mod.

- [x] 🟩 **7.1 Tela de opções com Cloth Config e integração ao ModMenu**
    - _Status:_ [`VanillaPlusModMenuApi`](file:///c:/Users/joaov/Desktop/vanilla-plus/src/main/java/com/vanillaplus/gui/VanillaPlusModMenuApi.java) integrado via Cloth Config `26.3.159+fabric` (Modrinth) e ModMenu `21.0.0` (TerraformersMC).
- [x] 🟩 **7.2 Suíte de testes unitários**
    - _Status:_ [`ConfigManagerTest.java`](file:///c:/Users/joaov/Desktop/vanilla-plus/src/test/java/com/vanillaplus/ConfigManagerTest.java), [`AutoBreedLogicTest.java`](file:///c:/Users/joaov/Desktop/vanilla-plus/src/test/java/com/vanillaplus/AutoBreedLogicTest.java) e [`ModuleManagerTest.java`](file:///c:/Users/joaov/Desktop/vanilla-plus/src/test/java/com/vanillaplus/ModuleManagerTest.java) executados com 100% de sucesso.
- [x] 🟩 **7.3 Compilação do `.jar` final de release**
    - _Status:_ `./gradlew.bat build` concluído com sucesso, gerando `build/libs/vanillaplus-1.0.0.jar` pronto para uso em Minecraft 26.3.
