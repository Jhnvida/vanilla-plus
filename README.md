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
- **Overlay HUD Minimalista e Animado:** Interface visual no canto superior com animação fluida (slide e fade com interpolação cúbica), ordenação inteligente por tamanho de texto e destaque sutil de modos ativos.

---

## Funcionalidades Principais

- **Auto Mine (Escavação por Área):** Escavação automatizada de áreas cúbicas delimitadas por dois pontos com marcação 3D interativa em tempo real, mineração segura em camadas (de cima para baixo) e seleção dinâmica de ferramentas.
- **Auto Breed com Indicador 3D:** Alimentação automática e indicador tridimensional discreto com o modelo oficial do alimento flutuando sobre animais prontos para reprodução.
- **Crop Indicator (Indicador 3D de Plantações):** Exibe modelos 3D nativos dos produtos sobre culturas 100% maduras (Trigo, Cenoura, Batata, Beterraba e Fungo do Nether) com renderização ultra-otimizada.
- **Auto Replant & Right-Click Harvest:** Colheita e replantio atômico de plantações maduras com indicação clara do modo ativo (`[click]` ou `[area]`).
- **Freecam Segura e Suave:** Desacoplamento da câmera com movimentação tridimensional livre, rotação precisa pelo mouse e supressão de pacotes anômalos.
- **Auto Tool com Proteção:** Troca dinâmica para a ferramenta mais rápida da hotbar para o bloco minerado, poupando ferramentas com durabilidade crítica.
- **Auto Refill:** Reposição instantânea de itens ou blocos consumidos na hotbar diretamente do inventário do jogador.
- **Fullbright:** Iluminação nítida para exploração subterrânea sem alteração de pacotes no servidor.
- **Light Level Overlay:** Marcadores no chão indicando condições de luz propícias para o surgimento de monstros hostis.
- **Mob ESP (Detecção 3D de Inimigos):** Contorno tridimensional (hitbox) límpido e etiquetas de texto flutuantes (nome, distância, vida) visíveis através de blocos via Gizmos nativos, com filtros individuais e escala responsiva.
- **Portal ESP (Localizador de Portais 3D):** Identifica portais do Nether e do End através de blocos com caixa unificada, feixe indicador vertical (beacon beam) e medidor de distância em tempo real.

---

## Módulos do Sistema

### 1. Automação (`automation`)

- **Auto Mine:** Permite selecionar dois pontos no mundo e escavar sequencialmente toda a área delimitada de cima para baixo, respeitando o alcance vanilla e alternando automaticamente a ferramenta ideal da hotbar.
- **Auto Breed:** Alimenta animais adultos (Vacas, Coguvacas, Ovelhas, Porcos e Galinhas) dentro do raio configurado respeitando intervalos de cooldown.
- **Auto Replant:** Colhe e replanta Trigo, Cenoura, Batata e Beterraba mantendo o solo arado hidratado, operando em modo de varredura por área ou exclusivamente por clique com botão direito.
- **Auto Tool:** Avalia o bloco em quebra e ativa automaticamente a ferramenta ideal da barra de acesso rápido, com limite configurável de durabilidade mínima e retorno ao slot original.
- **Auto Refill:** Detecta o esgotamento do stack selecionado na hotbar e puxa automaticamente o próximo lote correspondente do inventário.

### 2. Movimentação & Câmera (`movement`)

- **Freecam:** Desacopla a perspectiva visual do jogador, permitindo inspeção tridimensional suave com controle pelo mouse, ajuste dinâmico de velocidade com sprint e retenção do modelo do personagem no ponto de origem.

### 3. Visual & Renderização (`render`)

- **Auto Breed Indicator:** Destaca animais aptos a procriar exibindo o modelo 3D do seu alimento favorito em rotação e flutuação suave sobre a cabeça.
- **Crop Indicator:** Destaca plantações totalmente maduras com o modelo 3D flutuante do seu respectivo produto colhível, com orçamento dinâmico de renderização para preservar altas taxas de quadros.
- **Fullbright:** Ajusta dinamicamente a tabela de iluminação local para permitir visão clara em ambientes escuros e cavernas.
- **Light Level Overlay:** Renderiza cruzes visuais sobre blocos geráveis indicando zonas com nível de iluminação zero propícias ao spawn de monstros.
- **Mob ESP:** Renderiza caixas 3D (bounding boxes) e etiquetas flutuantes de identificação de mobs inimigos (com escala responsiva adaptativa à distância, Always On Top e filtros granulares por criatura).
- **Portal ESP:** Destaca estruturas de portais do Nether e do End com caixa 3D envolvente, feixe vertical orientador e distância em metros, prevenindo a perda do ponto de retorno em explorações.

---

## Tecnologias Utilizadas

### Core & Plataforma

- **[Minecraft Java Edition (26.3)](https://www.minecraft.net/):** Versão base do jogo com arquitetura desobfuscada nativa.
- **[Fabric Loader](https://fabricmc.net/):** Carregador de mods moderno, modular e leve.
- **[Fabric API](https://github.com/FabricMC/fabric):** Conjunto essencial de ganchos de eventos do cliente, ciclo de vida, rendering e teclas de atalho.
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
│   │   │   ├── gui/                    # Interfaces e sobreposições visuais
│   │   │   │   ├── StatusHudOverlay.java      # HUD minimalista animado dos módulos
│   │   │   │   └── VanillaPlusModMenuApi.java # Conexão com ModMenu e Cloth Config
│   │   │   ├── keybinds/               # Gerenciamento de atalhos e teclado
│   │   │   │   └── KeybindManager.java # Registro de teclas no KeyMappingHelper
│   │   │   ├── mixin/                  # Injeções de bytecode seguras via Mixins
│   │   │   │   ├── AnimalMixin.java    # Detecção de evento de reprodução de animais
│   │   │   │   ├── CameraMixin.java    # Interceptação e desacoplamento da câmera
│   │   │   │   ├── EntityMixin.java    # Rotação pelo mouse na Freecam
│   │   │   │   ├── GameRendererMixin.java     # Supressão de renderização da mão
│   │   │   │   ├── LightmapRenderStateExtractorMixin.java # Suporte ao Fullbright
│   │   │   │   ├── LocalPlayerMixin.java      # Supressão de pacotes durante Freecam
│   │   │   │   └── ScreenEffectRendererMixin.java # Supressão de sobreposição de blocos
│   │   │   ├── modules/                # Módulos funcionais do mod
│   │   │   │   ├── automation/         # Módulos de automação de tarefas
│   │   │   │   │   ├── AutoBreedModule.java   # Reprodução inteligente de animais
│   │   │   │   │   ├── AutoMineModule.java    # Escavação automatizada de áreas
│   │   │   │   │   ├── AutoRefillModule.java  # Reposição de itens na hotbar
│   │   │   │   │   ├── AutoReplantModule.java # Colheita e replantio de culturas
│   │   │   │   │   └── AutoToolModule.java    # Seleção inteligente de ferramentas
│   │   │   │   ├── movement/           # Módulos de movimentação e exploração
│   │   │   │   │   └── FreecamModule.java     # Câmera livre desacoplada
│   │   │   │   └── render/             # Módulos visuais e de iluminação
│   │   │   │       ├── AutoBreedIndicatorModule.java # Indicador 3D de reprodução
│   │   │   │       ├── BreedIndicatorStyle.java      # Estilos visuais do indicador
│   │   │   │       ├── FullbrightModule.java         # Visão clara e iluminação completa
│   │   │   │       └── LightLevelOverlayModule.java  # Indicador de níveis de luz
│   │   │   └── VanillaPlusClientMod.java      # Ponto de entrada (ClientModInitializer)
│   │   └── resources/
│   │       ├── assets/vanillaplus/     # Texturas, ícone e recursos de internacionalização
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

    O artefato final será gerado no diretório `build/libs/vanillaplus-2.1.0.jar`.

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

O VanillaPlus conta com uma **Central de Gerenciamento Própria** (Menu Interno), inspirada em dashboards modernos no estilo dark theme. Todas as opções, checkboxes, sliders e atribuições de atalhos podem ser gerenciados diretamente na interface.

Por padrão, para evitar conflitos de teclas com o Minecraft ou outros mods, **todos os atalhos de módulos individuais vêm desvinculados** e são 100% opcionais. A única tecla padrão configurada é o **Right Ctrl**.

| Ação / Controle                  | Atalho Padrão      | Descrição                                                                       |
| -------------------------------- | ------------------ | ------------------------------------------------------------------------------- |
| **Menu VanillaPlus (Dashboard)** | `Right Ctrl`       | Abre a Central de Gerenciamento própria com navegação por categorias e busca.   |
| **Configurações pelo ModMenu**   | ModMenu / Opções   | Redireciona diretamente para a Central de Gerenciamento interna do VanillaPlus. |
| **Atalhos Individuais**          | _Opcionais (None)_ | Cada módulo pode ter sua tecla atribuída ou removida diretamente pelo Menu.     |

> [!TIP]
> Para atribuir uma tecla a qualquer módulo, abra o menu com `Right Ctrl`, clique no botão `[ None ]` do módulo desejado e pressione a tecla escolhida. Para desvincular ou remover um atalho, basta pressionar `ESC` ou `Backspace` durante a escuta.

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
