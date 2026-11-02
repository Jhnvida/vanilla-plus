# Arquitetura de Software: VanillaPlus

Este documento descreve a estrutura de pacotes, padrões de projeto, ciclo de vida dos módulos e pontos de extensão do mod **VanillaPlus**.

---

## 1. Visão Geral da Arquitetura

O VanillaPlus foi concebido com foco em **baixo acoplamento**, **alta coesão** e **segurança para execução client-side**. O mod opera exclusivamente no cliente Fabric, sem criar dependências no lado do servidor.

```text
com.vanillaplus
├── VanillaPlusClientMod.java      // Ponto de entrada (ClientModInitializer)
├── config/
│   ├── ModConfig.java             // POJO de configuração com Gson
│   └── ConfigManager.java         // Leitura, escrita e serialização em disco
├── core/
│   ├── Module.java                // Classe base abstrata para todos os módulos
│   ├── ModuleCategory.java        // Categorias (AUTOMATION, MOVEMENT, GENERAL)
│   └── ModuleManager.java         // Registro, ciclo de vida e dispatch de eventos
├── modules/
│   ├── automation/
│   │   ├── AutoBreedModule.java   // Lógica de reprodução de animais
│   │   └── AutoReplantModule.java // Lógica de colheita e replantio de culturas
│   └── movement/
│       └── FreecamModule.java     // Controle de câmera desacoplada
├── gui/
│   └── VanillaPlusModMenuApi.java // Conexão com ModMenu e Cloth Config Screen
├── keybinds/
│   └── KeybindManager.java        // Registro no Fabric KeyBindingHelper
└── mixin/
    ├── CameraMixin.java           // Interceptação de posição e ângulos da câmera
    ├── ClientPlayerEntityMixin.java// Supressão de movimentação física durante Freecam
    └── WorldRendererMixin.java    // Renderização do jogador em terceira pessoa no Freecam
```

---

## 2. Padrões de Projeto Aplicados

### 2.1 Padrão Module (Template Method / State)

A classe abstrata `Module` define a estrutura fundamental compartilhada por todos os módulos:

- `getId()`: Identificador único (`"auto_breed"`, `"auto_replant"`, `"freecam"`).
- `getName()`: Nome legível exibido na interface.
- `getDescription()`: Descrição textual do recurso.
- `getCategory()`: Categoria (`AUTOMATION`, `MOVEMENT`, etc.).
- `isEnabled()` / `setEnabled(boolean)`: Controle de ativação com chamada aos ganchos:
    - `onEnable()`: Executado ao ativar o módulo.
    - `onDisable()`: Executado ao desativar o módulo.
    - `onClientTick(MinecraftClient client)`: Chamado a cada tick de renderização/lógica do cliente quando ativado.

### 2.2 Padrão Singleton / Registry (`ModuleManager`)

O `ModuleManager` atua como o mediador do sistema:

- Mantém a lista de instâncias dos módulos.
- Conecta-se ao `ClientTickEvents.END_CLIENT_TICK` da Fabric API.
- Distribui o ciclo de ticks para os módulos ativos de forma controlada, garantindo que nenhum módulo execute lógica pesada quando o jogo estiver no menu de pausa.

### 2.3 Padrão Data Transfer Object (DTO) para Configuração

O `ModConfig` é uma estrutura de dados plana e tipada, facilmente serializável para JSON via `Gson`:

- Carregamento inicial na inicialização do mod (`ClientModInitializer`).
- Atualização em tempo real quando o jogador salva a tela de opções do Cloth Config.
- Gravação atômica em disco no diretório `.minecraft/config/vanillaplus.json`.

---

## 3. Mixins e Injeção de Código

Os Mixins são aplicados apenas onde as APIs públicas do Fabric não oferecem pontos de extensão nativos:

| Classe Alvo                                       | Método Injetado       | Finalidade                                                                                                |
| ------------------------------------------------- | --------------------- | --------------------------------------------------------------------------------------------------------- |
| `net.minecraft.client.render.Camera`              | `update`              | Substitui as coordenadas de visualização pelas coordenadas da câmera do `FreecamModule` quando ativo.     |
| `net.minecraft.client.network.ClientPlayerEntity` | `sendMovementPackets` | Evita envio de pacotes espúrios ou inconsistentes enquanto a câmera livre está sendo movida.              |
| `net.minecraft.client.render.WorldRenderer`       | `render`              | Garante que o modelo do próprio jogador continue sendo renderizado na cena durante a execução do Freecam. |

---

## 4. Segurança e Resiliência

1. **Validação de Nulidade:** Toda chamada que acessa `client.player`, `client.world` ou `client.crosshairTarget` verifica nulidade antes de processar blocos ou entidades.
2. **Rate Limiting:** Todas as interações automatizadas utilizam temporizadores baseados em ticks ou `System.currentTimeMillis()`, impedindo rajadas de pacotes de rede.
3. **Desativação Segura:** Se o jogador desconectar de um mundo ou servidor (`ClientPlayConnectionEvents.DISCONNECT`), todos os módulos com estados voláteis (como Freecam) são restaurados imediatamente ao estado padrão para evitar corrupção de visão na próxima conexão.
