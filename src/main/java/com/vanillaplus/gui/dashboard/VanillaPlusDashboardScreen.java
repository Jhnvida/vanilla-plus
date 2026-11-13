package com.vanillaplus.gui.dashboard;

import com.mojang.blaze3d.platform.InputConstants;
import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.keybinds.KeybindManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class VanillaPlusDashboardScreen extends Screen {

    private final @Nullable Screen parentScreen;
    private final ModuleManager moduleManager;

    private int dialogWidth = 490;
    private int dialogHeight = 300;
    private int dialogX;
    private int dialogY;

    private static final int HEADER_HEIGHT = 32;
    private static final int FOOTER_HEIGHT = 18;

    public enum DashboardTab {
        AUTOMATION(ModuleCategory.AUTOMATION, "Automação"),
        MOVEMENT(ModuleCategory.MOVEMENT, "Movimentação"),
        RENDER(ModuleCategory.RENDER, "Visual & Render"),
        ALL(null, "Todos");

        private final @Nullable ModuleCategory category;
        private final String title;

        DashboardTab(@Nullable ModuleCategory category, String title) {
            this.category = category;
            this.title = title;
        }

        public @Nullable ModuleCategory getCategory() {
            return category;
        }

        public String getTitle() {
            return title;
        }
    }

    private DashboardTab currentTab = DashboardTab.ALL;

    private double gridScroll = 0;
    private int maxGridScroll = 0;
    private double drawerScroll = 0;
    private int maxDrawerScroll = 0;

    private @Nullable String selectedModuleId = null;

    private @Nullable String listeningModuleId = null;

    private @Nullable String activeSliderId = null;

    private static final int COLOR_BG_BACKDROP = 0xA008080C;
    private static final int COLOR_WINDOW_BG = 0xFF111216;
    private static final int COLOR_WINDOW_BORDER = 0xFF1F222B;

    private static final int COLOR_TAB_INACTIVE = 0xFF818698;
    private static final int COLOR_TAB_ACTIVE_BG = 0xFF1F222C;
    private static final int COLOR_TAB_ACTIVE_BORDER = 0xFF2F3443;
    private static final int COLOR_ACCENT_BLUE = 0xFF5C68F6;
    private static final int COLOR_ACCENT_BLUE_HOVER = 0xFF707CF8;

    private static final int COLOR_CARD_BG = 0xFF16181F;
    private static final int COLOR_CARD_BORDER = 0xFF232632;
    private static final int COLOR_CARD_HOVER_BG = 0xFF1D2029;
    private static final int COLOR_CARD_HOVER_BORDER = 0xFF353A4B;
    private static final int COLOR_CARD_SELECTED_BG = 0xFF1B1E28;
    private static final int COLOR_CARD_SELECTED_BORDER = 0xFF5C68F6;

    private static final int COLOR_SWITCH_OFF = 0xFF2A2D38;
    private static final int COLOR_SWITCH_OFF_BORDER = 0xFF363A48;
    private static final int COLOR_SWITCH_KNOB_OFF = 0xFF8E93A2;
    private static final int COLOR_SWITCH_KNOB_ON = 0xFFFFFFFF;

    private static final int COLOR_DRAWER_BG = 0xFF13141A;
    private static final int COLOR_DRAWER_BORDER = 0xFF222530;

    private static final int COLOR_TEXT_WHITE = 0xFFF3F4F6;
    private static final int COLOR_TEXT_MUTED = 0xFF8B90A0;
    private static final int COLOR_TEXT_DARK = 0xFF5A5F70;
    private static final int COLOR_RED = 0xFFEF4444;

    public VanillaPlusDashboardScreen(@Nullable Screen parentScreen, ModuleManager moduleManager) {
        super(Component.literal("VanillaPlus — Central de Gerenciamento"));
        this.parentScreen = parentScreen;
        this.moduleManager = moduleManager;
    }

    @Override
    protected void init() {
        KeybindManager.applySavedKeybinds();

        this.dialogWidth = Math.min(490, Math.max(360, this.width - 20));
        this.dialogHeight = Math.min(300, Math.max(240, this.height - 20));
        this.dialogX = (this.width - dialogWidth) / 2;
        this.dialogY = (this.height - dialogHeight) / 2;

        ModConfig config = ConfigManager.getConfig();

        if (config.lastDashboardTab != null) {
            try {
                this.currentTab = DashboardTab.valueOf(config.lastDashboardTab);
            } catch (Exception ignored) {
                this.currentTab = DashboardTab.ALL;
            }
        }

        List<Module> filtered = getFilteredModules();
        if (config.lastSelectedModuleId != null && moduleManager.getModule(config.lastSelectedModuleId).isPresent()) {
            this.selectedModuleId = config.lastSelectedModuleId;
        } else if (!filtered.isEmpty()) {
            this.selectedModuleId = filtered.getFirst().getId();
            config.lastSelectedModuleId = this.selectedModuleId;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, COLOR_BG_BACKDROP);

        drawPanel(guiGraphics, dialogX, dialogY, dialogWidth, dialogHeight, COLOR_WINDOW_BG, COLOR_WINDOW_BORDER);

        renderHeader(guiGraphics, mouseX, mouseY);

        guiGraphics.fill(dialogX, dialogY + HEADER_HEIGHT, dialogX + dialogWidth, dialogY + HEADER_HEIGHT + 1,
                COLOR_WINDOW_BORDER);

        renderContent(guiGraphics, mouseX, mouseY);

        renderFooter(guiGraphics);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderHeader(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int tabStartX = dialogX + 10;
        int tabY = dialogY + 7;
        int tabH = 18;

        for (DashboardTab tab : DashboardTab.values()) {
            boolean isSelected = (tab == currentTab);
            int textW = font.width(tab.getTitle());
            int tabW = textW + 14;

            boolean isHover = mouseX >= tabStartX && mouseX <= tabStartX + tabW &&
                    mouseY >= tabY && mouseY <= tabY + tabH;

            if (isSelected) {
                drawPanel(guiGraphics, tabStartX, tabY, tabW, tabH, COLOR_TAB_ACTIVE_BG, COLOR_TAB_ACTIVE_BORDER);
                guiGraphics.text(font, tab.getTitle(), tabStartX + 7, tabY + 5, COLOR_TEXT_WHITE, false);
            } else {
                int textColor = isHover ? COLOR_TEXT_WHITE : COLOR_TAB_INACTIVE;
                guiGraphics.text(font, tab.getTitle(), tabStartX + 7, tabY + 5, textColor, false);
            }

            tabStartX += tabW + 5;
        }

        int closeX = dialogX + dialogWidth - 22;
        int closeY = dialogY + 8;
        int closeSize = 16;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + closeSize &&
                mouseY >= closeY && mouseY <= closeY + closeSize;

        drawPanel(guiGraphics, closeX, closeY, closeSize, closeSize,
                closeHover ? COLOR_RED : 0xFF191B22,
                closeHover ? 0xFFDC2626 : 0xFF272A36);
        int xW = font.width("✕");
        guiGraphics.text(font, "✕", closeX + (closeSize - xW) / 2, closeY + 4, COLOR_TEXT_WHITE, false);
    }

    private void renderFooter(GuiGraphicsExtractor guiGraphics) {
        int footerY = dialogY + dialogHeight - FOOTER_HEIGHT;
        guiGraphics.fill(dialogX, footerY, dialogX + dialogWidth, footerY + 1, COLOR_WINDOW_BORDER);

        int textY = footerY + 5;
        guiGraphics.text(font, "Menu: [ Right Ctrl ]", dialogX + 10, textY, COLOR_TEXT_DARK, false);

        String ver = "v2.1";
        int verW = font.width(ver);
        guiGraphics.text(font, ver, dialogX + dialogWidth - verW - 10, textY, COLOR_TEXT_DARK, false);
    }

    private List<Module> getFilteredModules() {
        List<Module> result = new ArrayList<>();
        Collection<Module> all = moduleManager.getModules();

        for (Module m : all) {
            if (currentTab.getCategory() != null && m.getCategory() != currentTab.getCategory()) {
                continue;
            }
            result.add(m);
        }

        return result;
    }

    private void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int contentY = dialogY + HEADER_HEIGHT + 1;
        int contentH = dialogHeight - HEADER_HEIGHT - FOOTER_HEIGHT - 2;

        int rightPanelW = 185;
        int rightPanelX = dialogX + dialogWidth - rightPanelW - 6;
        int rightPanelY = contentY + 3;
        int rightPanelH = contentH - 6;

        int gridX = dialogX + 8;
        int gridW = rightPanelX - gridX - 6;
        int gridH = contentH;

        renderModuleGrid(guiGraphics, gridX, contentY, gridW, gridH, mouseX, mouseY);

        if (selectedModuleId != null) {
            Module module = moduleManager.getModule(selectedModuleId).orElse(null);
            if (module != null) {
                renderSettingsDrawer(guiGraphics, module, rightPanelX, rightPanelY, rightPanelW, rightPanelH, mouseX,
                        mouseY);
            }
        }
    }

    private void renderModuleGrid(GuiGraphicsExtractor guiGraphics, int gx, int gy, int gw, int gh, int mouseX,
            int mouseY) {
        List<Module> modules = getFilteredModules();
        int columns = 2;
        int cardGap = 6;
        int cardW = (gw - ((columns - 1) * cardGap)) / columns;
        int cardH = 50;

        int totalRows = (int) Math.ceil((double) modules.size() / columns);
        int totalContentH = (totalRows * (cardH + cardGap)) + 8;

        this.maxGridScroll = Math.max(0, totalContentH - gh);
        if (this.gridScroll > maxGridScroll) {
            this.gridScroll = maxGridScroll;
        }

        guiGraphics.enableScissor(gx, gy + 3, gx + gw, gy + gh - 3);

        int startY = gy + 6 - (int) gridScroll;

        for (int i = 0; i < modules.size(); i++) {
            Module m = Objects.requireNonNull(modules.get(i));
            int col = i % columns;
            int row = i / columns;

            int cx = gx + (col * (cardW + cardGap));
            int cy = startY + (row * (cardH + cardGap));

            if (cy + cardH >= gy && cy <= gy + gh) {
                renderModuleCard(guiGraphics, m, cx, cy, cardW, cardH, mouseX, mouseY);
            }
        }

        guiGraphics.disableScissor();
    }

    private void renderModuleCard(GuiGraphicsExtractor guiGraphics, Module m, int cx, int cy, int cw, int ch,
            int mouseX, int mouseY) {
        boolean isSelected = m.getId().equals(selectedModuleId);
        boolean isCardHover = mouseX >= cx && mouseX <= cx + cw && mouseY >= cy && mouseY <= cy + ch;

        int bg = isSelected ? COLOR_CARD_SELECTED_BG : (isCardHover ? COLOR_CARD_HOVER_BG : COLOR_CARD_BG);
        int border = isSelected ? COLOR_CARD_SELECTED_BORDER
                : (isCardHover ? COLOR_CARD_HOVER_BORDER : COLOR_CARD_BORDER);

        drawPanel(guiGraphics, cx, cy, cw, ch, bg, border);

        int helpX = cx + 6;
        int helpY = cy + 5;
        int helpSize = 11;
        boolean helpHover = mouseX >= helpX && mouseX <= helpX + helpSize && mouseY >= helpY
                && mouseY <= helpY + helpSize;

        drawCircle(guiGraphics, helpX + (helpSize / 2), helpY + (helpSize / 2), 5, helpHover ? 0xFF313645 : 0xFF1D2029);
        int qW = font.width("?");
        guiGraphics.text(font, "?", helpX + (helpSize - qW) / 2 + 1, helpY + 2,
                helpHover ? COLOR_ACCENT_BLUE : COLOR_TEXT_MUTED, false);

        if (helpHover) {
            List<net.minecraft.util.FormattedCharSequence> lines = new ArrayList<>();
            lines.add(Component.literal("§f" + m.getName()).getVisualOrderText());
            lines.addAll(font.split(Component.literal("§7" + m.getDescription()), 160));
            guiGraphics.setTooltipForNextFrame(font, lines, mouseX, mouseY);
        }

        int switchW = 20;
        int switchH = 11;
        int switchX = cx + cw - switchW - 6;
        int switchY = cy + 5;
        boolean switchHover = mouseX >= switchX && mouseX <= switchX + switchW && mouseY >= switchY
                && mouseY <= switchY + switchH;

        drawToggle(guiGraphics, switchX, switchY, m.isEnabled(), switchHover);

        String displayName = m.getName();
        int maxNameWidth = cw - 44;
        if (font.width(displayName) > maxNameWidth) {
            displayName = font.plainSubstrByWidth(displayName, maxNameWidth - 6) + "…";
        }
        guiGraphics.text(font, displayName, cx + 6, cy + ch - 13, COLOR_TEXT_WHITE, false);

        boolean isListening = m.getId().equals(listeningModuleId);
        String keyText = isListening ? "..." : KeybindManager.getKeyDisplayName(m.getId());
        int keyW = font.width(keyText) + 8;
        int keyH = 11;
        int keyX = cx + cw - keyW - 6;
        int keyY = cy + ch - 14;
        boolean keyHover = mouseX >= keyX && mouseX <= keyX + keyW && mouseY >= keyY && mouseY <= keyY + keyH;

        int keyBg = isListening ? 0xFF78350F : (keyHover ? 0xFF2A2E3B : 0xFF1D2028);
        int keyBorder = isListening ? 0xFFF59E0B : (keyHover ? 0xFF3E4456 : 0xFF282B37);
        int keyColor = isListening ? 0xFFFEF3C7 : (keyText.equals("None") ? COLOR_TEXT_MUTED : COLOR_TEXT_WHITE);

        drawPanel(guiGraphics, keyX, keyY, keyW, keyH, keyBg, keyBorder);
        guiGraphics.text(font, keyText, keyX + 4, keyY + 2, keyColor, false);
    }

    private void renderSettingsDrawer(GuiGraphicsExtractor guiGraphics, Module m, int dx, int dy, int dw, int dh,
            int mouseX, int mouseY) {
        drawPanel(guiGraphics, dx, dy, dw, dh, COLOR_DRAWER_BG, COLOR_DRAWER_BORDER);

        String title = m.getName();
        int maxTitleW = dw - 16;
        if (font.width(title) > maxTitleW) {
            title = font.plainSubstrByWidth(title, maxTitleW - 6) + "…";
        }
        guiGraphics.text(font, title, dx + 10, dy + 8, COLOR_TEXT_WHITE, true);

        guiGraphics.fill(dx, dy + 22, dx + dw, dy + 23, COLOR_DRAWER_BORDER);

        int optY = dy + 24;
        int optH = dh - 26;

        guiGraphics.enableScissor(dx, optY, dx + dw, optY + optH);

        int curY = optY + 6 - (int) drawerScroll;

        guiGraphics.text(font, "Atalho de Teclado:", dx + 8, curY, COLOR_TEXT_MUTED, false);
        curY += 12;

        boolean isListening = m.getId().equals(listeningModuleId);
        String bindDisplay = isListening ? "[ Pressione uma tecla... ]"
                : "[ " + KeybindManager.getKeyDisplayName(m.getId()) + " ]";
        int bindW = font.width(bindDisplay) + 8;
        int bindH = 14;
        boolean bindHover = mouseX >= dx + 8 && mouseX <= dx + 8 + bindW && mouseY >= curY && mouseY <= curY + bindH;

        int bindBg = isListening ? 0xFF78350F : (bindHover ? 0xFF262A37 : 0xFF1B1E27);
        int bindBorder = isListening ? 0xFFF59E0B : (bindHover ? 0xFF3D4457 : 0xFF272B38);
        int bindTextColor = isListening ? 0xFFFEF3C7 : COLOR_TEXT_WHITE;

        drawPanel(guiGraphics, dx + 8, curY, bindW, bindH, bindBg, bindBorder);
        guiGraphics.text(font, bindDisplay, dx + 12, curY + 3, bindTextColor, false);
        curY += 18;

        if (KeybindManager.hasCustomKey(m.getId())) {
            String delText = "✕ Remover atalho";
            int delW = font.width(delText) + 8;
            int delH = 12;
            boolean delHover = mouseX >= dx + 8 && mouseX <= dx + 8 + delW && mouseY >= curY && mouseY <= curY + delH;

            drawPanel(guiGraphics, dx + 8, curY, delW, delH, delHover ? 0xFF451A1A : 0xFF2A1515,
                    delHover ? COLOR_RED : 0xFF5C2424);
            guiGraphics.text(font, delText, dx + 12, curY + 2, delHover ? 0xFFFF7777 : COLOR_RED, false);
            curY += 16;
        }

        guiGraphics.fill(dx + 8, curY, dx + dw - 8, curY + 1, COLOR_DRAWER_BORDER);
        curY += 8;

        curY = renderModuleSpecificDrawerSettings(guiGraphics, m, dx + 8, curY, dw - 16, mouseX, mouseY);

        guiGraphics.disableScissor();

        int totalDrawerH = (curY + (int) drawerScroll) - optY + 10;
        this.maxDrawerScroll = Math.max(0, totalDrawerH - optH);
        if (this.drawerScroll > maxDrawerScroll) {
            this.drawerScroll = maxDrawerScroll;
        }
    }

    @SuppressWarnings("null")
    private int renderModuleSpecificDrawerSettings(GuiGraphicsExtractor guiGraphics, Module m, int sx, int sy, int sw,
            int mouseX, int mouseY) {
        ModConfig config = ConfigManager.getConfig();
        String id = m.getId();

        switch (id) {
            case "auto_breed" -> {
                sy = drawSliderControl(guiGraphics, sx, sy, sw, "breed_range", "Raio de Reprodução",
                        String.format(Locale.ROOT, "%.1fm", config.autoBreedRange),
                        (float) ((config.autoBreedRange - 2.0) / 10.0), mouseX, mouseY);

                sy = drawSliderControl(guiGraphics, sx, sy, sw, "breed_interval", "Intervalo",
                        config.autoBreedIntervalMs + "ms",
                        (float) ((config.autoBreedIntervalMs - 150) / 850.0), mouseX, mouseY);

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Troca de Hotbar", config.autoBreedAllowHotbarSwap, mouseX,
                        mouseY);

                guiGraphics.fill(sx, sy + 2, sx + sw, sy + 3, COLOR_DRAWER_BORDER);
                sy += 8;

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Indicador Visual", config.autoBreedIndicatorEnabled,
                        mouseX, mouseY);

                if (config.autoBreedIndicatorEnabled) {
                    sy = drawSliderControl(guiGraphics, sx, sy, sw, "b_ind_radius", "Raio do Indicador",
                            config.autoBreedIndicatorRange + "m",
                            (float) ((config.autoBreedIndicatorRange - 4) / 28.0), mouseX, mouseY);

                    sy = drawSubToggle(guiGraphics, sx, sy, sw, "Apenas c/ Comida",
                            config.autoBreedIndicatorRequireFood, mouseX, mouseY);
                }
            }
            case "auto_replant" -> {
                guiGraphics.text(font, "Modo de Colheita:", sx, sy, COLOR_TEXT_MUTED, false);
                sy += 11;
                String modeName = config.rightClickHarvestOnly ? "Click" : "Area";
                drawPillButton(guiGraphics, sx, sy, font.width(modeName) + 14, 13, modeName, mouseX, mouseY);
                sy += 16;

                if (!config.rightClickHarvestOnly) {
                    sy = drawSliderControl(guiGraphics, sx, sy, sw, "replant_radius", "Raio da Área",
                            String.format(Locale.ROOT, "%.1fm", config.autoReplantRadius),
                            (float) ((config.autoReplantRadius - 1.0) / 5.0), mouseX, mouseY);
                }

                guiGraphics.fill(sx, sy + 2, sx + sw, sy + 3, COLOR_DRAWER_BORDER);
                sy += 8;

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Indicador de Plantas", config.cropIndicatorEnabled, mouseX,
                        mouseY);

                if (config.cropIndicatorEnabled) {
                    sy = drawSliderControl(guiGraphics, sx, sy, sw, "crop_radius", "Raio de Busca",
                            config.cropIndicatorRadius + "m",
                            (float) ((config.cropIndicatorRadius - 4) / 28.0), mouseX, mouseY);

                    sy = drawSliderControl(guiGraphics, sx, sy, sw, "crop_count", "Qtd Máxima",
                            String.valueOf(config.cropIndicatorMaxCount),
                            (float) ((config.cropIndicatorMaxCount - 4) / 28.0), mouseX, mouseY);

                    sy = drawSubToggle(guiGraphics, sx, sy, sw, "Fungo do Nether", config.cropIndicatorShowNetherWart,
                            mouseX, mouseY);
                    sy = drawSubToggle(guiGraphics, sx, sy, sw, "Apenas c/ Enxada",
                            config.cropIndicatorRequireFarmingItem, mouseX, mouseY);
                }
            }
            case "auto_mine" -> {
                sy = drawSliderControl(guiGraphics, sx, sy, sw, "mine_range", "Alcance",
                        String.format(Locale.ROOT, "%.1fm", config.autoMineRange),
                        (float) ((config.autoMineRange - 2.0) / 4.0), mouseX, mouseY);

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Auto Ferramenta", config.autoMineAutoTool, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Evitar Lava", config.autoMineAvoidLava, mouseX, mouseY);
            }
            case "auto_tool" -> {
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Salvar Durabilidade", config.autoToolProtectDurability,
                        mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Voltar Item Anterior", config.autoToolSwitchBack, mouseX,
                        mouseY);
                sy = drawSliderControl(guiGraphics, sx, sy, sw, "tool_durability", "Durab. Mínima",
                        String.valueOf(config.autoToolMinDurability),
                        (float) ((config.autoToolMinDurability - 1) / 19.0), mouseX, mouseY);
            }
            case "freecam" -> {
                sy = drawSliderControl(guiGraphics, sx, sy, sw, "freecam_speed", "Velocidade",
                        String.format(Locale.ROOT, "%.1fx", config.freecamSpeed),
                        (float) ((config.freecamSpeed - 0.5) / 4.5), mouseX, mouseY);

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Renderizar Jogador", config.freecamRenderOwnPlayer, mouseX,
                        mouseY);
            }
            case "light_level_overlay" -> {
                sy = drawSliderControl(guiGraphics, sx, sy, sw, "light_radius", "Raio de Render",
                        config.lightLevelOverlayRadius + "m",
                        (float) ((config.lightLevelOverlayRadius - 4) / 20.0), mouseX, mouseY);

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Mostrar Seguro (>=1)", config.lightLevelShowSafe, mouseX,
                        mouseY);
            }
            case "mob_esp" -> {
                sy = drawSliderControl(guiGraphics, sx, sy, sw, "mob_esp_range", "Raio de Detecção",
                        config.mobEspRange + "m",
                        (float) ((config.mobEspRange - 4) / 60.0), mouseX, mouseY);

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Exibir Nome", config.mobEspShowName, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Exibir Distância", config.mobEspShowDistance, mouseX,
                        mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Vida (Corações)", config.mobEspShowHealth, mouseX,
                        mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Escala Responsiva", config.mobEspResponsiveText, mouseX,
                        mouseY);

                sy = drawSliderControl(guiGraphics, sx, sy, sw, "mob_esp_scale", "Tamanho do Texto",
                        String.format(Locale.ROOT, "%.1fx", config.mobEspTextScale),
                        (float) ((config.mobEspTextScale - 0.5f) / 1.5f), mouseX, mouseY);

                guiGraphics.fill(sx, sy + 2, sx + sw, sy + 3, COLOR_DRAWER_BORDER);
                sy += 8;

                guiGraphics.text(font, "Modo de Cores:", sx, sy, COLOR_TEXT_MUTED, false);
                sy += 11;
                String colorModeName = config.mobEspColorMode == com.vanillaplus.modules.render.MobEspColorMode.WHITE
                        ? "Branco (Minimalista)"
                        : "Dinâmico (Ameaça)";
                drawPillButton(guiGraphics, sx, sy, sw, 15, colorModeName, mouseX, mouseY);
                sy += 20;

                guiGraphics.fill(sx, sy + 2, sx + sw, sy + 3, COLOR_DRAWER_BORDER);
                sy += 8;

                guiGraphics.text(font, "Filtros de Criaturas:", sx, sy, COLOR_TEXT_MUTED, false);
                sy += 12;

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Creeper", config.mobEspCreeper, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Esqueleto", config.mobEspSkeleton, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Zumbi", config.mobEspZombie, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Aranha", config.mobEspSpider, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Bruxa", config.mobEspWitch, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Enderman", config.mobEspEnderman, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Slime & Magma", config.mobEspSlime, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Phantom", config.mobEspPhantom, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Nether (Blaze/Ghast)", config.mobEspNether, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Illagers & Raids", config.mobEspIllager, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Chefes (Warden/Boss)", config.mobEspBoss, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Outros Monstros", config.mobEspOther, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Jogadores", config.mobEspPlayers, mouseX, mouseY);
            }
            case "portal_esp" -> {
                sy = drawSliderControl(guiGraphics, sx, sy, sw, "portal_esp_radius", "Raio de Busca",
                        config.portalEspRadius + "m",
                        (float) ((config.portalEspRadius - 16) / 144.0), mouseX, mouseY);

                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Portal do Nether", config.portalEspNether, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Portal do End", config.portalEspEnd, mouseX, mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Exibir Distância", config.portalEspShowDistance, mouseX,
                        mouseY);
                sy = drawSubToggle(guiGraphics, sx, sy, sw, "Preenchimento 3D", config.portalEspBoxFill, mouseX,
                        mouseY);
            }
            default -> {
                guiGraphics.text(font, "Sem opções adicionais.", sx, sy, COLOR_TEXT_DARK, false);
                sy += 14;
            }
        }

        return sy;
    }

    private int drawSliderControl(GuiGraphicsExtractor guiGraphics, int x, int y, int w, String sliderId, String label,
            String valueText, float ratio, int mouseX, int mouseY) {
        ratio = Mth.clamp(ratio, 0.0f, 1.0f);

        guiGraphics.text(font, label, x, y, COLOR_TEXT_MUTED, false);
        int valW = font.width(valueText);
        guiGraphics.text(font, valueText, x + w - valW, y, COLOR_TEXT_WHITE, false);
        y += 11;

        int trackH = 4;
        int trackY = y + 3;
        boolean isHover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 10;

        guiGraphics.fill(x, trackY, x + w, trackY + trackH, 0xFF242733);

        int fillW = (int) (w * ratio);
        if (fillW > 0) {
            guiGraphics.fill(x, trackY, x + fillW, trackY + trackH, COLOR_ACCENT_BLUE);
        }

        int thumbX = x + fillW - 3;
        int thumbY = y + 1;
        guiGraphics.fill(thumbX, thumbY, thumbX + 6, thumbY + 8, isHover ? COLOR_TEXT_WHITE : 0xFFD1D5DB);

        return y + 14;
    }

    private int drawSubToggle(GuiGraphicsExtractor guiGraphics, int x, int y, int w, String label, boolean state,
            int mouseX, int mouseY) {
        guiGraphics.text(font, label, x, y + 2, COLOR_TEXT_WHITE, false);

        int switchW = 18;
        int switchH = 10;
        int switchX = x + w - switchW;
        int switchY = y + 1;
        boolean switchHover = mouseX >= switchX && mouseX <= switchX + switchW && mouseY >= switchY
                && mouseY <= switchY + switchH;

        drawToggle(guiGraphics, switchX, switchY, state, switchHover);

        return y + 15;
    }

    private void drawPillButton(GuiGraphicsExtractor guiGraphics, int x, int y, int w, int h, String text, int mouseX,
            int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        drawPanel(guiGraphics, x, y, w, h, hover ? 0xFF2B2F3D : 0xFF1E212B, hover ? COLOR_ACCENT_BLUE : 0xFF2E3342);
        int tW = font.width(text);
        guiGraphics.text(font, text, x + (w - tW) / 2, y + 3, COLOR_TEXT_WHITE, false);
    }


    private void drawPanel(GuiGraphicsExtractor guiGraphics, int x, int y, int w, int h, int bg, int border) {
        guiGraphics.fill(x, y, x + w, y + h, bg);
        guiGraphics.fill(x, y, x + w, y + 1, border);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, border);
        guiGraphics.fill(x, y, x + 1, y + h, border);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, border);
    }

    private void drawCircle(GuiGraphicsExtractor guiGraphics, int cx, int cy, int r, int color) {
        guiGraphics.fill(cx - r, cy - r, cx + r + 1, cy + r + 1, color);
    }

    private void drawToggle(GuiGraphicsExtractor guiGraphics, int x, int y, boolean enabled, boolean hover) {
        int w = 20;
        int h = 11;

        int bg = enabled ? (hover ? COLOR_ACCENT_BLUE_HOVER : COLOR_ACCENT_BLUE)
                : (hover ? 0xFF353947 : COLOR_SWITCH_OFF);
        int border = enabled ? COLOR_ACCENT_BLUE : COLOR_SWITCH_OFF_BORDER;

        drawPanel(guiGraphics, x, y, w, h, bg, border);

        int knobSize = 7;
        int knobY = y + 2;
        int knobX = enabled ? (x + w - knobSize - 2) : (x + 2);
        int knobColor = enabled ? COLOR_SWITCH_KNOB_ON : COLOR_SWITCH_KNOB_OFF;

        guiGraphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knobColor);
    }


    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        int button = event.button();
        boolean isLeftClick = (button == 0 || button == 1);
        boolean isRightClick = (button == 2);

        if (!isLeftClick && !isRightClick) {
            return super.mouseClicked(event, isDouble);
        }

        int mx = (int) event.x();
        int my = (int) event.y();

        int closeX = dialogX + dialogWidth - 22;
        int closeY = dialogY + 8;
        if (mx >= closeX && mx <= closeX + 16 && my >= closeY && my <= closeY + 16) {
            this.onClose();
            return true;
        }

        int tabStartX = dialogX + 10;
        int tabY = dialogY + 7;
        int tabH = 18;
        for (DashboardTab tab : DashboardTab.values()) {
            int textW = font.width(tab.getTitle());
            int tabW = textW + 14;
            if (mx >= tabStartX && mx <= tabStartX + tabW && my >= tabY && my <= tabY + tabH) {
                this.currentTab = tab;
                this.gridScroll = 0;
                ModConfig config = ConfigManager.getConfig();
                config.lastDashboardTab = tab.name();
                List<Module> filtered = getFilteredModules();
                if (!filtered.isEmpty()) {
                    boolean hasCurrent = selectedModuleId != null
                            && filtered.stream().anyMatch(m -> m.getId().equals(selectedModuleId));
                    if (!hasCurrent) {
                        this.selectedModuleId = filtered.getFirst().getId();
                        config.lastSelectedModuleId = this.selectedModuleId;
                    }
                }
                ConfigManager.save();
                return true;
            }
            tabStartX += tabW + 5;
        }

        int contentY = dialogY + HEADER_HEIGHT + 1;
        int contentH = dialogHeight - HEADER_HEIGHT - FOOTER_HEIGHT - 2;

        int rightPanelW = 185;
        int rightPanelX = dialogX + dialogWidth - rightPanelW - 6;
        int rightPanelY = contentY + 3;
        int rightPanelH = contentH - 6;

        String selId = this.selectedModuleId;
        if (selId != null && mx >= rightPanelX && mx <= rightPanelX + rightPanelW && my >= rightPanelY
                && my <= rightPanelY + rightPanelH) {
            Module module = moduleManager.getModule(selId).orElse(null);
            if (module != null) {
                int optY = rightPanelY + 24;
                int curY = optY + 6 - (int) drawerScroll;
                curY += 12;

                boolean isListening = module.getId().equals(listeningModuleId);
                String bindDisplay = isListening ? "[ Pressione uma tecla... ]"
                        : "[ " + KeybindManager.getKeyDisplayName(module.getId()) + " ]";
                int bindW = font.width(bindDisplay) + 8;
                int bindH = 14;

                if (mx >= rightPanelX + 8 && mx <= rightPanelX + 8 + bindW && my >= curY && my <= curY + bindH) {
                    this.listeningModuleId = isListening ? null : module.getId();
                    return true;
                }
                curY += 18;

                if (KeybindManager.hasCustomKey(module.getId())) {
                    String delText = "✕ Remover atalho";
                    int delW = font.width(delText) + 8;
                    int delH = 12;
                    if (mx >= rightPanelX + 8 && mx <= rightPanelX + 8 + delW && my >= curY && my <= curY + delH) {
                        KeybindManager.removeModuleKey(module.getId());
                        this.listeningModuleId = null;
                        return true;
                    }
                    curY += 16;
                }

                curY += 8;

                handleDrawerSpecificClicks(module, rightPanelX + 8, curY, rightPanelW - 16, mx, my);
                return true;
            }
        }

        int gridX = dialogX + 8;
        int gridW = rightPanelX - gridX - 6;
        if (mx >= gridX && mx <= gridX + gridW && my >= contentY && my <= contentY + contentH) {
            List<Module> modules = getFilteredModules();
            int columns = 2;
            int cardGap = 6;
            int cardW = (gridW - ((columns - 1) * cardGap)) / columns;
            int cardH = 50;
            int startY = contentY + 6 - (int) gridScroll;

            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                int col = i % columns;
                int row = i / columns;
                int cx = gridX + (col * (cardW + cardGap));
                int cy = startY + (row * (cardH + cardGap));

                if (mx >= cx && mx <= cx + cardW && my >= cy && my <= cy + cardH) {
                    int switchW = 20;
                    int switchH = 11;
                    int switchX = cx + cardW - switchW - 6;
                    int switchY = cy + 5;

                    if (mx >= switchX && mx <= switchX + switchW && my >= switchY && my <= switchY + switchH) {
                        m.toggle();
                        selectedModuleId = m.getId();
                        ConfigManager.getConfig().lastSelectedModuleId = m.getId();
                        ConfigManager.save();
                        return true;
                    }

                    String keyText = KeybindManager.getKeyDisplayName(m.getId());
                    int keyW = font.width(keyText) + 8;
                    int keyH = 11;
                    int keyX = cx + cardW - keyW - 6;
                    int keyY = cy + cardH - 14;

                    if (mx >= keyX && mx <= keyX + keyW && my >= keyY && my <= keyY + keyH) {
                        selectedModuleId = m.getId();
                        listeningModuleId = m.getId();
                        ConfigManager.getConfig().lastSelectedModuleId = m.getId();
                        ConfigManager.save();
                        return true;
                    }

                    selectedModuleId = m.getId();
                    listeningModuleId = null;
                    ConfigManager.getConfig().lastSelectedModuleId = m.getId();
                    ConfigManager.save();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, isDouble);
    }

    private void handleDrawerSpecificClicks(Module m, int sx, int sy, int sw, int mx, int my) {
        ModConfig config = ConfigManager.getConfig();
        String id = m.getId();

        switch (id) {
            case "auto_breed" -> {
                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "breed_range";
                    updateSliderValue("breed_range", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;

                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "breed_interval";
                    updateSliderValue("breed_interval", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.autoBreedAllowHotbarSwap = !config.autoBreedAllowHotbarSwap;
                    ConfigManager.save();
                    return;
                }
                sy += 23;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.autoBreedIndicatorEnabled = !config.autoBreedIndicatorEnabled;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (config.autoBreedIndicatorEnabled) {
                    if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                        activeSliderId = "b_ind_radius";
                        updateSliderValue("b_ind_radius", (float) (mx - sx) / sw);
                        return;
                    }
                    sy += 25;

                    if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                        config.autoBreedIndicatorRequireFood = !config.autoBreedIndicatorRequireFood;
                        ConfigManager.save();
                    }
                }
            }
            case "auto_replant" -> {
                sy += 11;
                int modeW = 55;
                if (mx >= sx && mx <= sx + modeW && my >= sy && my <= sy + 13) {
                    config.rightClickHarvestOnly = !config.rightClickHarvestOnly;
                    ConfigManager.save();
                    return;
                }
                sy += 16;

                if (!config.rightClickHarvestOnly) {
                    if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                        activeSliderId = "replant_radius";
                        updateSliderValue("replant_radius", (float) (mx - sx) / sw);
                        return;
                    }
                    sy += 25;
                }

                sy += 8;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.cropIndicatorEnabled = !config.cropIndicatorEnabled;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (config.cropIndicatorEnabled) {
                    if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                        activeSliderId = "crop_radius";
                        updateSliderValue("crop_radius", (float) (mx - sx) / sw);
                        return;
                    }
                    sy += 25;

                    if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                        activeSliderId = "crop_count";
                        updateSliderValue("crop_count", (float) (mx - sx) / sw);
                        return;
                    }
                    sy += 25;

                    if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                        config.cropIndicatorShowNetherWart = !config.cropIndicatorShowNetherWart;
                        ConfigManager.save();
                        return;
                    }
                    sy += 15;

                    if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                        config.cropIndicatorRequireFarmingItem = !config.cropIndicatorRequireFarmingItem;
                        ConfigManager.save();
                    }
                }
            }
            case "auto_mine" -> {
                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "mine_range";
                    updateSliderValue("mine_range", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.autoMineAutoTool = !config.autoMineAutoTool;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.autoMineAvoidLava = !config.autoMineAvoidLava;
                    ConfigManager.save();
                }
            }
            case "auto_tool" -> {
                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.autoToolProtectDurability = !config.autoToolProtectDurability;
                    ConfigManager.save();
                    return;
                }
                sy += 15;
                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.autoToolSwitchBack = !config.autoToolSwitchBack;
                    ConfigManager.save();
                    return;
                }
                sy += 15;
                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "tool_durability";
                    updateSliderValue("tool_durability", (float) (mx - sx) / sw);
                }
            }
            case "freecam" -> {
                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "freecam_speed";
                    updateSliderValue("freecam_speed", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;
                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.freecamRenderOwnPlayer = !config.freecamRenderOwnPlayer;
                    ConfigManager.save();
                }
            }
            case "light_level_overlay" -> {
                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "light_radius";
                    updateSliderValue("light_radius", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;
                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.lightLevelShowSafe = !config.lightLevelShowSafe;
                    ConfigManager.save();
                }
            }
            case "mob_esp" -> {
                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "mob_esp_range";
                    updateSliderValue("mob_esp_range", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.mobEspShowName = !config.mobEspShowName;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.mobEspShowDistance = !config.mobEspShowDistance;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.mobEspShowHealth = !config.mobEspShowHealth;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.mobEspResponsiveText = !config.mobEspResponsiveText;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "mob_esp_scale";
                    updateSliderValue("mob_esp_scale", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;

                sy += 8;
                sy += 11;
                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 15) {
                    config.mobEspColorMode = config.mobEspColorMode == com.vanillaplus.modules.render.MobEspColorMode.WHITE
                            ? com.vanillaplus.modules.render.MobEspColorMode.THREAT_BASED
                            : com.vanillaplus.modules.render.MobEspColorMode.WHITE;
                    ConfigManager.save();
                    return;
                }
                sy += 20;

                sy += 8;
                sy += 12;

                java.util.function.Consumer<Integer> toggleIndex = idx -> {
                    switch (idx) {
                        case 0 -> config.mobEspCreeper = !config.mobEspCreeper;
                        case 1 -> config.mobEspSkeleton = !config.mobEspSkeleton;
                        case 2 -> config.mobEspZombie = !config.mobEspZombie;
                        case 3 -> config.mobEspSpider = !config.mobEspSpider;
                        case 4 -> config.mobEspWitch = !config.mobEspWitch;
                        case 5 -> config.mobEspEnderman = !config.mobEspEnderman;
                        case 6 -> config.mobEspSlime = !config.mobEspSlime;
                        case 7 -> config.mobEspPhantom = !config.mobEspPhantom;
                        case 8 -> config.mobEspNether = !config.mobEspNether;
                        case 9 -> config.mobEspIllager = !config.mobEspIllager;
                        case 10 -> config.mobEspBoss = !config.mobEspBoss;
                        case 11 -> config.mobEspOther = !config.mobEspOther;
                        case 12 -> config.mobEspPlayers = !config.mobEspPlayers;
                    }
                    ConfigManager.save();
                };

                for (int i = 0; i < 13; i++) {
                    if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                        toggleIndex.accept(i);
                        return;
                    }
                    sy += 15;
                }
            }
            case "portal_esp" -> {
                if (mx >= sx && mx <= sx + sw && my >= sy + 11 && my <= sy + 21) {
                    activeSliderId = "portal_esp_radius";
                    updateSliderValue("portal_esp_radius", (float) (mx - sx) / sw);
                    return;
                }
                sy += 25;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.portalEspNether = !config.portalEspNether;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.portalEspEnd = !config.portalEspEnd;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.portalEspShowDistance = !config.portalEspShowDistance;
                    ConfigManager.save();
                    return;
                }
                sy += 15;

                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 14) {
                    config.portalEspBoxFill = !config.portalEspBoxFill;
                    ConfigManager.save();
                }
            }
        }
    }

    private void updateSliderValue(String sliderId, float ratio) {
        ratio = Mth.clamp(ratio, 0.0f, 1.0f);
        ModConfig config = ConfigManager.getConfig();

        switch (sliderId) {
            case "breed_range" -> config.autoBreedRange = Math.round((2.0 + (ratio * 10.0)) * 10.0) / 10.0;
            case "breed_interval" -> config.autoBreedIntervalMs = Math.round(150 + (ratio * 850.0f));
            case "mine_range" -> config.autoMineRange = Math.round((2.0 + (ratio * 4.0)) * 10.0) / 10.0;
            case "replant_radius" -> config.autoReplantRadius = Math.round((1.0 + (ratio * 5.0)) * 10.0) / 10.0;
            case "tool_durability" -> config.autoToolMinDurability = Math.round(1 + (ratio * 19.0f));
            case "freecam_speed" -> config.freecamSpeed = Math.round((0.5f + (ratio * 4.5f)) * 10.0f) / 10.0f;
            case "crop_radius" -> config.cropIndicatorRadius = Math.round(4 + (ratio * 28.0f));
            case "crop_count" -> config.cropIndicatorMaxCount = Math.round(4 + (ratio * 28.0f));
            case "b_ind_radius" -> config.autoBreedIndicatorRange = Math.round(4 + (ratio * 28.0f));
            case "light_radius" -> config.lightLevelOverlayRadius = Math.round(4 + (ratio * 20.0f));
            case "mob_esp_range" -> config.mobEspRange = Math.round(4 + (ratio * 60.0f));
            case "mob_esp_scale" -> config.mobEspTextScale = Math.round((0.5f + (ratio * 1.5f)) * 10.0f) / 10.0f;
            case "portal_esp_radius" -> config.portalEspRadius = Math.round(16 + (ratio * 144.0f));
        }

        ConfigManager.save();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        String sliderId = this.activeSliderId;
        if (sliderId != null) {
            int mx = (int) event.x();
            int rightPanelW = 185;
            int rightPanelX = dialogX + dialogWidth - rightPanelW - 6;
            int sx = rightPanelX + 8;
            int sw = rightPanelW - 16;

            float ratio = (float) (mx - sx) / (float) sw;
            updateSliderValue(sliderId, ratio);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (activeSliderId != null) {
            activeSliderId = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        int rightPanelW = 185;
        int rightPanelX = dialogX + dialogWidth - rightPanelW - 6;

        if (mx >= rightPanelX && mx <= rightPanelX + rightPanelW) {
            this.drawerScroll -= (vAmount * 16);
            if (this.drawerScroll < 0)
                this.drawerScroll = 0;
            if (this.drawerScroll > maxDrawerScroll)
                this.drawerScroll = maxDrawerScroll;
            return true;
        }

        this.gridScroll -= (vAmount * 16);
        if (this.gridScroll < 0)
            this.gridScroll = 0;
        if (this.gridScroll > maxGridScroll)
            this.gridScroll = maxGridScroll;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        String listeningId = this.listeningModuleId;
        if (listeningId != null) {
            int keyCode = event.key();

            if (keyCode == InputConstants.KEY_ESCAPE) {
                this.listeningModuleId = null;
                return true;
            }

            if (keyCode == InputConstants.KEY_BACKSPACE || keyCode == InputConstants.KEY_DELETE) {
                KeybindManager.removeModuleKey(listeningId);
                this.listeningModuleId = null;
                return true;
            }

            KeybindManager.setModuleKey(listeningId, keyCode);
            this.listeningModuleId = null;
            return true;
        }

        if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RCONTROL) {
            this.onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Minecraft client = Minecraft.getInstance();
        if (client.gui != null) {
            client.gui.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
