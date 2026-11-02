# Pesquisa e Análise Técnica: VanillaPlus (Minecraft 26.3 Fabric)

Este documento registra a fundamentação técnica, a arquitetura e as decisões de engenharia adotadas para viabilizar o desenvolvimento do mod **VanillaPlus** diretamente para **Minecraft Java Edition 26.3** ("Wilderness Bound") no ecossistema **Fabric**.

---

## 1. O Desafio do Minecraft 26.3 e o Ecossistema Unobfuscated

### 1.1 A Mudança Estrutural da Mojang no 26.3

No Minecraft 26.3, a Mojang adotou uma alteração histórica:

- **Ausência de Ofuscação ProGuard:** O cliente oficial é distribuído com nomes legíveis diretamente no JAR (`net.minecraft.client.Minecraft`, `LocalPlayer`, `LevelRenderer`).
- **Impacto no Fabric Loom:** O Fabric Loom tradicionalmente esperava arquivos de mapeamento Yarn (`net.fabricmc:yarn:26.3...`) ou arquivos de ofuscação oficiais da Mojang (`client_mappings`). Como a Mojang não fornece mais `client_mappings` para o 26.3 e os repositórios públicos Maven do FabricMC ainda não possuíam artefatos do Yarn indexados para a versão, o Loom falhava com `Configuration 'mappings' has no dependencies` ou `Failed to find official mojang mappings for 26.3`.

### 1.2 A Solução: Identity Mappings Provider (Tiny v2 Local)

Para viabilizar a compilação no 26.3 sem esperar atualizações externas nem regredir de versão:

- Criamos um arquivo de mapeamento de identidade (`mappings/mappings.tiny`):
    ```text
    tiny	2	0	official	intermediary	named
    ```
- Empacotamos em `mappings/mappings-26.3.jar` com a estrutura padrão `/mappings/mappings.tiny`.
- O Fabric Loom consome esse JAR como mapeamento local direto:
    ```groovy
    mappings files("mappings/mappings-26.3.jar")
    ```
- **Resultado:** O Loom trata os símbolos nativos da Mojang como `official`, `intermediary` e `named` simultaneamente, permitindo o remapeamento perfeito das dependências para o target 26.3.

---

## 2. Refatorações da API do Minecraft 26.3 Adotadas no Código

Durante a descompilação e análise dos binários do Minecraft 26.3, mapeamos as seguintes evoluções de API que foram implementadas no VanillaPlus:

| Componente                | Mudança no 26.3                                              | Implementação no VanillaPlus                                                                  |
| ------------------------- | ------------------------------------------------------------ | --------------------------------------------------------------------------------------------- |
| **Classe Principal**      | `MinecraftClient` $\rightarrow$ `Minecraft`                  | `client = Minecraft.getInstance()`                                                            |
| **Jogador Local**         | `ClientPlayerEntity` $\rightarrow$ `LocalPlayer`             | Injeção de Mixin em `sendPosition()` para cancelar movimento físico no Freecam                |
| **Entidades de Animais**  | Subpacotes dedicados por espécie                             | `net.minecraft.world.entity.animal.cow.Cow`, `sheep.Sheep`, `pig.Pig`, `chicken.Chicken`      |
| **Maturação de Culturas** | `isMature()` $\rightarrow$ `isMaxAge()`                      | `CropBlock.isMaxAge(state)`                                                                   |
| **Animação de Interação** | Introdução do record `SwingAnimation`                        | `client.player.swing(hand, SwingAnimation.DEFAULT, true)`                                     |
| **Sistema de Câmera**     | Unificação de delta temporal com `DeltaTracker`              | Injeção em `Camera.update(DeltaTracker)` com `deltaTracker.getGameTimeDeltaPartialTick(true)` |
| **Atalhos de Teclado**    | `KeyBinding` $\rightarrow$ `KeyMapping` e `KeyMappingHelper` | Módulo oficial `fabric-key-mapping-api-v1` com `KeyMapping.Category.register(Identifier)`     |
| **Textos e Componentes**  | `Text` $\rightarrow$ `Component`                             | `Component.literal(...)` para compatibilidade com o Cloth Config                              |

---

## 3. Matriz de Dependências Validadas para Minecraft 26.3

| Dependência       | Versão Validada                    | Repositório                  | Status           |
| ----------------- | ---------------------------------- | ---------------------------- | ---------------- |
| **JDK**           | Eclipse Temurin OpenJDK 25.0.5 LTS | Local (winget)               | 🟩 Compilado     |
| **Gradle**        | 9.8.2                              | Gradle Services              | 🟩 Wrapper ativo |
| **Fabric Loom**   | 1.19.3                             | FabricMC Maven               | 🟩 Configurado   |
| **Fabric Loader** | 0.20.1                             | FabricMC Maven               | 🟩 Integrado     |
| **Fabric API**    | 0.165.0+26.3                       | FabricMC Maven               | 🟩 Remapeado     |
| **Cloth Config**  | 26.3.159+fabric                    | Modrinth Maven               | 🟩 Integrado     |
| **ModMenu**       | 21.0.0                             | TerraformersMC Maven         | 🟩 Integrado     |
| **Mappings**      | Local Identity (Tiny v2)           | `mappings/mappings-26.3.jar` | 🟩 Operacional   |

---

## 4. Testes e Validação de Compilação

O projeto foi validado por meio de:

1. Suíte automatizada de testes JUnit 5 com `gradlew test` (testando regras de negócio de configuração, metadados de módulos e registro no `ModuleManager`).
2. Build completo via `gradlew build`, que executa:
    - Compilação do código Java 25 (`compileJava`).
    - Processamento de recursos e metadados (`processResources`).
    - Empacotamento do JAR (`jar`).
    - Remapeamento e validação de Mixins pelo Loom (`remapJar`).
    - Geração do artefato pronto para distribuição: `build/libs/vanillaplus-1.0.0.jar`.
