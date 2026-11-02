# Especificação Refinada: VanillaPlus

Este documento aprofunda os requisitos e regras de negócio definidos inicialmente em `especificacao.md`, formalizando comportamentos esperados, limitações, parâmetros configuráveis e casos de borda.

---

## 1. Visão Geral do Sistema

O **VanillaPlus** é um mod exclusivamente client-side para Minecraft Java Edition 26.3 (Fabric), projetado para entregar utilitários de Qualidade de Vida (QoL) sem quebrar o equilíbrio do jogo nem introduzir vantagens desleais.

---

## 2. Especificação Detalhada dos Módulos

### 2.1 Módulo: Auto Breed

Auxilia na reprodução de animais dentro do raio de interação legítimo do jogador.

#### 2.1.1 Espécies Suportadas (Versão Inicial)

- **Vaca (`CowEntity`)** e **Coguvaca (`MooshroomEntity`)**: Alimento = Trigo (`Items.WHEAT`).
- **Ovelha (`SheepEntity`)**: Alimento = Trigo (`Items.WHEAT`).
- **Porco (`PigEntity`)**: Alimento = Cenoura (`Items.CARROT`), Batata (`Items.POTATO`) ou Beterraba (`Items.BEETROOT`).
- **Galinha (`ChickenEntity`)**: Alimento = Sementes de Trigo (`Items.WHEAT_SEEDS`), Beterraba (`Items.BEETROOT_SEEDS`), Melão (`Items.MELON_SEEDS`) ou Abóbora (`Items.PUMPKIN_SEEDS`).

#### 2.1.2 Regras de Negócio e Elegibilidade

1. **Condições para alimentar um animal:**
    - O animal deve ser uma instância de `AnimalEntity` adulta (`!entity.isBaby()`).
    - O animal não pode estar em modo de amor ativo (`!entity.isInLove()`).
    - O animal não pode estar em cooldown de reprodução (`entity.getBreedingAge() == 0`).
    - O jogador deve possuir o alimento aceito pela espécie na mão principal (`MAIN_HAND`), na mão secundária (`OFF_HAND`) ou na barra de acesso rápido (Hotbar), conforme opção configurada.
    - O animal deve estar a uma distância máxima configurável (padrão: 3.5 blocos, máximo: 4.5 blocos) com linha de visão livre.
2. **Taxa de Interação e Cooldown:**
    - Para não sobrecarregar a rede nem acionar defesas de anti-cheat, o módulo deve aguardar um intervalo mínimo configurável entre cada alimentação (padrão: 350 milissegundos).
3. **Casos de Borda:**
    - Se o alimento acabar durante a operação, o módulo cessa as interações imediatamente e emite um alerta discreto caso configurado.
    - Se houver apenas um animal elegível no raio, ele não deve ser alimentado repetidamente se não houver um parceiro em potencial.

#### 2.1.3 Parâmetros de Configuração

- `enabled` (boolean): Ativa/desativa o módulo.
- `range` (double): Raio de busca (entre 2.0 e 4.5 blocos, padrão 3.5).
- `feedDelayMs` (int): Atraso entre cada interação em milissegundos (entre 200 e 1000, padrão 350).
- `allowHotbarSwap` (boolean): Permite trocar temporariamente o slot selecionado da hotbar para o alimento adequado (padrão `false` - requer item em mãos).

---

### 2.2 Módulo: Auto Replant

Auxilia na colheita e reposição contínua de plantações agrícolas.

#### 2.2.1 Culturas Suportadas (Versão Inicial)

- **Trigo (`Blocks.WHEAT`)**: Semente = `Items.WHEAT_SEEDS`.
- **Cenoura (`Blocks.CARROTS`)**: Semente = `Items.CARROT`.
- **Batata (`Blocks.POTATOES`)**: Semente = `Items.POTATO`.
- **Beterraba (`Blocks.BEETROOTS`)**: Semente = `Items.BEETROOT_SEEDS`.

#### 2.2.2 Modos de Operação

1. **Modo Right-Click Harvest (Padrão):**
    - Ao interagir (botão direito) em uma cultura madura (`CropBlock.isMature(state) == true`), o mod intercepta a ação, simula a quebra/colheita e replanta instantaneamente com o item de semente correspondente do inventário.
    - Preserva o bloco de terra arada (`FarmlandBlock`) intacto.
2. **Modo Auto-Harvest por Raio (Opcional):**
    - Quando ativado, varre o raio ao redor do jogador e colhe/replanta plantações maduras automaticamente com intervalo de ticks controlado.

#### 2.2.3 Regras de Negócio e Casos de Borda

- **Maturação:** Culturas com `AGE < MAX_AGE` são ignoradas completamente para evitar desperdício de sementes e tempo de crescimento.
- **Falta de Sementes:** Se o jogador não tiver o item de plantio no inventário, a cultura madura não é replantada (ou apenas colhida, conforme opção).
- **Solo:** Se o bloco abaixo não for terra arada hidratada ou padrão (`Farmland`), o replantio é cancelado.

#### 2.2.4 Parâmetros de Configuração

- `enabled` (boolean): Ativa/desativa o módulo.
- `mode` (enum: `RIGHT_CLICK`, `AREA_SCAN`): Modo de colheita/replantio (padrão `RIGHT_CLICK`).
- `harvestRange` (double): Raio para o modo de área (entre 2.0 e 4.0 blocos, padrão 3.0).
- `replantWheat` (boolean), `replantCarrots` (boolean), `replantPotatoes` (boolean), `replantBeetroots` (boolean): Filtro individual por cultura.

---

### 2.3 Módulo: Freecam

Permite ao jogador desacoplar a câmera de seu corpo para inspeção do terreno e auxílio em construções.

#### 2.3.1 Regras de Negócio e Comportamento

1. **Ativação/Desativação:**
    - Ativado e desativado via atalho de teclado dedicado (KeyBinding).
    - Ao ativar, memoriza a posição real (`x, y, z`), ângulos de visão (`pitch, yaw`) e estado de voo do jogador.
    - Ao desativar, a visão retorna instantaneamente para a perspectiva normal do jogador físico.
2. **Movimentação da Câmera:**
    - Utiliza as teclas padrão de movimento (WASD, Espaço para subir, Shift para descer).
    - Permite atravessar blocos no modo espectador local sem enviar dados de colisão anômalos para o servidor.
    - Velocidade da câmera configurável (padrão: 1.0, ajustável com roda do mouse ou opções).
3. **Segurança e Rede:**
    - O corpo do jogador (`ClientPlayerEntity`) permanece imóvel no mundo do servidor.
    - O mod bloqueia o envio de pacotes `PlayerMoveC2SPacket` com posições da câmera livre, evitando detecções de trapaça (anti-cheat de voo ou teletransporte).
    - O corpo do próprio jogador deve ser renderizado no local original.

#### 2.3.2 Parâmetros de Configuração

- `enabled` (boolean): Estado atual da câmera livre.
- `cameraSpeed` (float): Velocidade de translação (entre 0.2 e 5.0, padrão 1.0).
- `renderOwnPlayer` (boolean): Se o modelo do jogador deve continuar visível onde ele parou (padrão `true`).

---

## 3. Configurações Globais e Interface

- As configurações são mantidas em arquivo JSON (`config/vanillaplus.json`).
- Tela de configurações integrada com **ModMenu** e estilizada com **Cloth Config**.
- Todos os atalhos de teclado são registrados no menu nativo de Controles do Minecraft sob a categoria `VanillaPlus`.
