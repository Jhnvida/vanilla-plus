# VanillaPlus — Qualidade de Vida, Automação e Exploração para Minecraft

Mod client-side de Qualidade de Vida (QoL), automação consciente e exploração imersiva desenvolvido para **Minecraft Java Edition** com o ecossistema **Fabric Loader**, construído sobre **Java 25**, **Fabric Loom**, **Cloth Config** e uma arquitetura modular orientada a eventos desacoplados.

---

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-46B431?style=flat&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric_Loader-0.20.1-DBD0C0?style=flat&logo=fabric&logoColor=black)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.8.2-02303A?style=flat&logo=gradle&logoColor=white)](https://gradle.org/)
[![Cloth Config](https://img.shields.io/badge/Cloth_Config-26.3-black?style=flat)](https://github.com/shedaniel/cloth-config)
[![ModMenu](https://img.shields.io/badge/ModMenu-21.0-blue?style=flat)](https://github.com/TerraformersMC/ModMenu)
[![Licença MIT](https://img.shields.io/badge/License-MIT-blue?style=flat)](#licença)

---

## Sumário

- [Visão Geral](#visão-geral)
- [Funcionalidades Principais](#funcionalidades-principais)
- [Módulos do Sistema](#módulos-do-sistema)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Compilação](#instalação-e-compilação)
- [Scripts Disponíveis](#scripts-disponíveis)
- [Controles e Teclas de Atalho](#controles-e-teclas-de-atalho)
- [Padrões de Versionamento](#padrões-de-versionamento)
- [Licença](#licença)

---

## Visão Geral

O **VanillaPlus** foi projetado para aprimorar a experiência do jogador no Minecraft através de utilitários que eliminam ações manuais repetitivas sem romper o equilíbrio original do jogo nem conceder vantagens desleais em ambientes multijogador:

- **Execução 100% Client-Side:** Opera exclusivamente na camada do cliente sem exigir modificações, permissões ou plugins adicionais no lado do servidor.
- **Arquitetura Modular Desacoplada:** Estruturado no padrão de módulos autônomos (`Module`), gerenciados centralmente por um `ModuleManager` que orquestra o ciclo de vida e o despacho de ticks de forma otimizada.
- **Prevenção de Abuso e Anti-Ban:** Mecanismos rigorosos de controle de taxa de interação (_rate limiting_) e interceptação via Mixins para bloquear o envio de pacotes suspeitos de posicionamento durante o uso de câmera livre.
- **Configuração Atômica e Visual:** Integração nativa com a Cloth Config API e o catálogo ModMenu, permitindo ajustes em tempo real com persistência atômica em formato JSON (`.minecraft/config/vanillaplus.json`).

---

## Funcionalidades Principais

- **Auto Breed Inteligente:** Alimentação automática de animais elegíveis dentro do raio de interação legítimo, com checagem rigorosa de idade, cooldown de reprodução e estado de amor.
- **Auto Replant & Right-Click Harvest:** Colheita e replantio atômico de plantações maduras com preservação da terra arada, além de modo opcional de varredura por proximidade.
- **Freecam Segura e Suave:** Desacoplamento da câmera com movimentação tridimensional livre, interpolação suave na taxa de quadros e retenção do modelo do jogador na posição original.
- **Painel de Configurações Gráfico:** Menu completo para personalizar raios de alcance, intervalos em milissegundos, teclas de atalho e filtros individuais de espécies e culturas.
- **Resiliência e Desconexão Segura:** Restauração instantânea de estados visuais e desativação preventiva de módulos voláteis em eventos de troca de dimensão ou desconexão do servidor.

---

## Módulos do Sistema

### 1. Auto Breed (`automation`)

Automatiza o processo de reprodução de animais na fazenda sem sobrecarregar a conexão com o servidor.

- **Espécies Suportadas:** Vacas, Coguvacas, Ovelhas, Porcos e Galinhas.
- **Regras de Validação:** Apenas animais adultos (`!isBaby()`), sem cooldown ativo (`getBreedingAge() == 0`) e que não estejam em modo de amor (`!isInLove()`).
- **Alimentação Adequada:** Detecta automaticamente o alimento correspondente à espécie na mão do jogador ou na barra de acesso rápido (_hotbar_).
- **Parâmetros Customizáveis:** Raio de busca (padrão de 3.5 blocos) e atraso mínimo entre alimentações (`feedDelayMs`, padrão de 350ms).

### 2. Auto Replant (`automation`)

Facilita a manutenção de fazendas agrícolas, minimizando cliques manuais e evitando quebra acidental de blocos de solo.

- **Culturas Suportadas:** Trigo, Cenoura, Batata e Beterraba.
- **Modo Right-Click Harvest (Padrão):** Ao clicar com o botão direito sobre uma cultura madura (`CropBlock.isMature()`), o módulo executa a colheita e replanta uma nova semente instantaneamente a partir do inventário.
- **Modo Varredura por Área (Opcional):** Monitora plantações vizinhas no raio configurado e realiza a colheita/replantio de forma automatizada com cadência controlada.

### 3. Freecam (`movement`)

Permite inspecionar estruturas, terrenos e projetos de engenharia sem alterar o posicionamento físico do personagem.

- **Câmera Virtual Independente:** Intercepta a rotação e os eixos de projeção através do `CameraMixin` utilizando a API `DeltaTracker`.
- **Supressão de Pacotes de Movimento:** O `LocalPlayerMixin` bloqueia o envio de `PlayerMoveC2SPacket` enquanto a câmera livre estiver em operação, prevenindo kicks por anti-cheat de voo ou teletransporte.
- **Visibilidade em Terceira Pessoa:** O corpo do jogador continua totalmente visível no mundo exatamente onde permaneceu.

---

## Tecnologias Utilizadas

### Core & Plataforma

- **[Minecraft Java Edition (26.3)](https://www.minecraft.net/):** Versão base do jogo com arquitetura desobfuscada nativa.
- **[Fabric Loader](https://fabricmc.net/):** Carregador de mods moderno, modular e leve.
- **[Fabric API](https://github.com/FabricMC/fabric):** Conjunto essencial de ganchos de eventos do cliente, ciclo de vida e teclas de atalho.
- **[Java 25 (OpenJDK)](https://openjdk.org/):** Plataforma de execução com suporte às melhorias de desempenho mais recentes da JVM.

### Interface & Configuração

- **[Cloth Config API](https://github.com/shedaniel/cloth-config):** Construtor de telas de opções elegantes com categorias, seletores, toggles e sliders.
- **[ModMenu](https://github.com/TerraformersMC/ModMenu):** Interface de listagem de mods no menu principal do jogo com acesso direto às opções.
- **[Gson](https://github.com/google/gson):** Mecanismo de serialização e desserialização de arquivos de preferências em formato JSON.

### Ferramental de Compilação & Mapeamentos

- **[Gradle 9.8.2](https://gradle.org/):** Sistema corporativo de automação de compilação, resolução de dependências e empacotamento.
- **[Fabric Loom 1.19.3](https://github.com/FabricMC/fabric-loom):** Plugin oficial de instrumentação de bytecode, injeção de Mixins e compilação do Fabric.
- **Mapeamentos Unificados Mojmap:** Mapeamentos customizados nos namespaces `official`, `intermediary` e `named` empacotados em `mappings/mappings-26.3.jar`.

### Qualidade de Código & Testes

- **[JUnit Jupiter 5](https://junit.org/junit5/):** Bateria de testes unitários para validação de ciclo de vida, persistência e regras de negócio.

---

## Estrutura do Projeto

```text
vanilla-plus/
├── docs/                               # Documentação técnica e especificações
│   ├── arquitetura.md                  # Padrões de projeto, ciclo de vida e Mixins
│   ├── especificacao_refinada.md       # Regras de negócio, restrições e casos de borda
│   ├── pesquisa_tecnica.md             # Análise de ecossistema e mapeamentos Mojmap
│   └── TODO.md                         # Backlog e rastreamento de fases
├── mappings/                           # Mapeamentos de desenvolvimento
│   └── mappings-26.3.jar               # Mappings unificados para Minecraft 26.3
├── src/
│   ├── main/
│   │   ├── java/com/vanillaplus/
│   │   │   ├── config/                 # Gerenciamento e serialização de opções JSON
│   │   │   │   ├── ConfigManager.java  # Leitura e escrita atômica em disco
│   │   │   │   └── ModConfig.java      # Estrutura POJO das configurações do mod
│   │   │   ├── core/                   # Núcleo da arquitetura de módulos
│   │   │   │   ├── Module.java         # Classe abstrata base com ciclo de vida
│   │   │   │   ├── ModuleCategory.java # Categorias de agrupamento dos módulos
│   │   │   │   └── ModuleManager.java  # Registro singleton e despachante de ticks
│   │   │   ├── gui/                    # Integração com menus e interfaces
│   │   │   │   └── VanillaPlusModMenuApi.java # Conexão com ModMenu e Cloth Config
│   │   │   ├── keybinds/               # Gerenciamento de atalhos e teclado
│   │   │   │   └── KeybindManager.java # Registro de teclas no KeyMappingHelper
│   │   │   ├── mixin/                  # Injeções de bytecode seguras via Mixins
│   │   │   │   ├── CameraMixin.java    # Interceptação e desacoplamento da câmera
│   │   │   │   └── LocalPlayerMixin.java # Supressão de pacotes durante Freecam
│   │   │   ├── modules/                # Módulos funcionais de QoL
│   │   │   │   ├── automation/         # Módulos de automação consciente
│   │   │   │   │   ├── AutoBreedModule.java   # Reprodução inteligente de animais
│   │   │   │   │   └── AutoReplantModule.java # Colheita e replantio de culturas
│   │   │   │   └── movement/           # Módulos de movimentação e exploração
│   │   │   │       └── FreecamModule.java     # Câmera livre desacoplada
│   │   │   └── VanillaPlusClientMod.java      # Ponto de entrada (ClientModInitializer)
│   │   └── resources/
│   │       ├── fabric.mod.json         # Manifesto e metadados do Fabric Loader
│   │       └── vanillaplus.mixins.json # Configuração de injeção dos Mixins
│   └── test/java/com/vanillaplus/      # Bateria de testes unitários automatizados
│       ├── AutoBreedLogicTest.java     # Testes de regras de negócio de reprodução
│       ├── ConfigManagerTest.java      # Testes de persistência e integridade JSON
│       └── ModuleManagerTest.java      # Testes de registro e despacho de ciclo de vida
├── build.gradle                        # Script de compilação do Gradle e dependências
├── gradle.properties                   # Variáveis e versões do projeto Fabric/Minecraft
├── gradlew                             # Wrapper Gradle para sistemas Unix/macOS
├── gradlew.bat                         # Wrapper Gradle para sistemas Windows
├── README.md                           # Documentação principal do projeto
└── settings.gradle                     # Definições de projeto e repositórios de plugins
```

---

## Pré-requisitos

Para compilar e executar o projeto localmente, certifique-se de possuir:

- [Java Development Kit (JDK) 25](https://adoptium.net/) (recomendado Eclipse Temurin OpenJDK 25 LTS).
- Minecraft Java Edition 26.3 com o perfil do Fabric Loader instalado.
- Conexão com a internet na primeira execução para download das dependências e bibliotecas do Fabric Loom.

---

## Instalação e Compilação

1. **Clone o repositório:**

    ```bash
    git clone https://github.com/Jhnvida/vanilla-plus.git
    cd vanilla-plus
    ```

2. **Execute a suíte de testes unitários:**

    ```bash
    # Linux / macOS
    ./gradlew test

    # Windows
    .\gradlew.bat test
    ```

3. **Gere o arquivo compilado (`.jar`):**

    ```bash
    # Linux / macOS
    ./gradlew build

    # Windows
    .\gradlew.bat build
    ```

    O artefato final será gerado no diretório `build/libs/vanillaplus-1.0.0.jar`.

4. **Inicie o cliente de desenvolvimento:**

    ```bash
    # Linux / macOS
    ./gradlew runClient

    # Windows
    .\gradlew.bat runClient
    ```

5. **Instalação no Minecraft Oficial:**
   Copie o arquivo `.jar` gerado para o diretório `.minecraft/mods/` junto com as dependências requeridas (**Fabric API**, **Cloth Config** e opcionalmente **ModMenu**).

---

## Scripts Disponíveis

| Comando               | Descrição                                                                         |
| --------------------- | --------------------------------------------------------------------------------- |
| `./gradlew build`     | Executa a verificação completa, testes unitários e compila o `.jar` final do mod. |
| `./gradlew runClient` | Inicializa o cliente do Minecraft em ambiente de depuração com o mod carregado.   |
| `./gradlew test`      | Executa todos os testes unitários automatizados através do JUnit Platform.        |
| `./gradlew check`     | Dispara todas as etapas de verificação e checagem de integridade do código.       |
| `./gradlew clean`     | Remove os diretórios de compilação anteriores na pasta `build/`.                  |

---

## Controles e Teclas de Atalho

| Ação / Módulo             | Atalho Padrão            | Descrição                                                               |
| ------------------------- | ------------------------ | ----------------------------------------------------------------------- |
| **Alternar Freecam**      | `U`                      | Desacopla a câmera do corpo físico ou retorna à perspectiva do jogador. |
| **Alternar Auto Breed**   | Configurável no ModMenu  | Ativa ou suspende a alimentação automática de animais na fazenda.       |
| **Alternar Auto Replant** | Configurável no ModMenu  | Ativa ou suspende a colheita e replantio atômico de plantações.         |
| **Configurações do Mod**  | ModMenu / Menu de Opções | Abre a interface gráfica interativa com os parâmetros da Cloth Config.  |

---

## Padrões de Versionamento

Este repositório adota rigorosamente a convenção do [Conventional Commits](https://www.conventionalcommits.org/pt-br/):

- `feat:` Novos módulos funcionais ou adições de recursos de QoL.
- `fix:` Correções de bugs, comportamentos anômalos ou ajustes em Mixins.
- `docs:` Alterações na documentação técnica e guias de arquitetura.
- `style:` Formatação de código ou ajustes estéticos sem impacto comportamental.
- `refactor:` Refatorações na arquitetura de módulos sem alteração de funcionalidade.
- `perf:` Otimizações no consumo de memória, taxa de ticks ou renderização.
- `test:` Adição ou manutenção de testes unitários automatizados.
- `chore:` Atualizações de dependências, mapeamentos ou tarefas do Gradle.

---

## Licença

Este projeto é distribuído sob os termos da licença [MIT](https://opensource.org/licenses/MIT). Você tem a liberdade de utilizar, modificar e redistribuir o código conforme os termos da licença de software livre.
