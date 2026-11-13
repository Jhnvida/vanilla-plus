package com.vanillaplus.config;

public class ModConfig {

    public boolean showStatusHud = true;

    public boolean autoBreedEnabled = false;
    public double autoBreedRange = 3.5;
    public int autoBreedIntervalMs = 350;
    public boolean autoBreedAllowHotbarSwap = false;
    public boolean breedCows = true;
    public boolean breedSheep = true;
    public boolean breedPigs = true;
    public boolean breedChickens = true;

    public boolean autoReplantEnabled = false;
    public boolean rightClickHarvestOnly = false;
    public double autoReplantRadius = 3.0;
    public int autoReplantIntervalMs = 250;
    public boolean replantWheat = true;
    public boolean replantCarrots = true;
    public boolean replantPotatoes = true;
    public boolean replantBeetroots = true;

    public boolean freecamEnabled = false;
    public float freecamSpeed = 1.0f;
    public boolean freecamRenderOwnPlayer = true;

    public boolean autoToolEnabled = false;
    public boolean autoToolProtectDurability = true;
    public int autoToolMinDurability = 5;
    public boolean autoToolSwitchBack = true;

    public boolean autoRefillEnabled = false;

    public boolean fullbrightEnabled = false;

    public boolean lightLevelOverlayEnabled = false;
    public int lightLevelOverlayRadius = 12;
    public boolean lightLevelShowSafe = false;

    public boolean autoBreedIndicatorEnabled = false;
    public int autoBreedIndicatorRange = 16;
    public boolean autoBreedIndicatorRequireFood = false;
    public com.vanillaplus.modules.render.BreedIndicatorStyle breedIndicatorStyle = com.vanillaplus.modules.render.BreedIndicatorStyle.ITEM_3D;

    public boolean autoMineEnabled = false;
    public double autoMineRange = 4.5;
    public int autoMineDelayTicks = 2;
    public boolean autoMineAutoTool = true;
    public boolean autoMineAvoidLava = true;

    public boolean cropIndicatorEnabled = false;
    public int cropIndicatorRadius = 10;
    public int cropIndicatorMaxCount = 16;
    public boolean cropIndicatorRequireFarmingItem = false;
    public boolean cropIndicatorShowNetherWart = true;

    public boolean mobEspEnabled = false;
    public int mobEspRange = 24;
    public boolean mobEspShowName = true;
    public boolean mobEspShowDistance = true;
    public boolean mobEspShowHealth = true;
    public com.vanillaplus.modules.render.MobEspColorMode mobEspColorMode = com.vanillaplus.modules.render.MobEspColorMode.WHITE;
    public boolean mobEspCreeper = true;
    public boolean mobEspSkeleton = true;
    public boolean mobEspZombie = true;
    public boolean mobEspSpider = true;
    public boolean mobEspWitch = true;
    public boolean mobEspEnderman = true;
    public boolean mobEspSlime = true;
    public boolean mobEspPhantom = true;
    public boolean mobEspNether = true;
    public boolean mobEspIllager = true;
    public boolean mobEspBoss = true;
    public boolean mobEspOther = true;
    public boolean mobEspPlayers = false;

    public java.util.Map<String, Integer> moduleKeybinds = new java.util.HashMap<>();
    public boolean defaultBindsCleared = false;

    public String lastDashboardTab = "ALL";
    public String lastSelectedModuleId = "auto_breed";

    public ModConfig() {
    }
}
