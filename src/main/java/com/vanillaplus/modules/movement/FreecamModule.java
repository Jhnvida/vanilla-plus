package com.vanillaplus.modules.movement;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FreecamModule extends Module {
    private static FreecamModule instance;

    private double cameraX;
    private double cameraY;
    private double cameraZ;
    private float cameraYaw;
    private float cameraPitch;

    private double prevCameraX;
    private double prevCameraY;
    private double prevCameraZ;

    public FreecamModule() {
        super("freecam", "Freecam", "Controla uma câmera desacoplada da posição física do jogador",
                ModuleCategory.MOVEMENT);
        instance = this;
    }

    public static FreecamModule getInstance() {
        return instance;
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            this.cameraX = client.player.getX();
            this.cameraY = client.player.getEyeY();
            this.cameraZ = client.player.getZ();
            this.prevCameraX = cameraX;
            this.prevCameraY = cameraY;
            this.prevCameraZ = cameraZ;
            this.cameraYaw = client.player.getYRot();
            this.cameraPitch = client.player.getXRot();
        }
    }

    @Override
    protected void onDisable() {
        Minecraft client = Minecraft.getInstance();
        if (client.levelRenderer != null) {
            client.levelRenderer.clearVisibleSections();
        }
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.player == null)
            return;

        this.prevCameraX = cameraX;
        this.prevCameraY = cameraY;
        this.prevCameraZ = cameraZ;

        ModConfig config = ConfigManager.getConfig();
        float speed = config.freecamSpeed * 0.5f;

        this.cameraYaw = client.player.getYRot();
        this.cameraPitch = client.player.getXRot();

        float radYaw = (float) Math.toRadians(cameraYaw);
        float radPitch = (float) Math.toRadians(cameraPitch);

        Vec3 forward = new Vec3(-Mth.sin(radYaw) * Mth.cos(radPitch), -Mth.sin(radPitch),
                Mth.cos(radYaw) * Mth.cos(radPitch)).normalize();
        Vec3 right = new Vec3(Mth.cos(radYaw), 0, Mth.sin(radYaw)).normalize();
        Vec3 up = new Vec3(0, 1, 0);

        Vec3 movement = Vec3.ZERO;

        if (client.options.keyUp.isDown()) {
            movement = movement.add(forward.scale(speed));
        }
        if (client.options.keyDown.isDown()) {
            movement = movement.subtract(forward.scale(speed));
        }
        if (client.options.keyRight.isDown()) {
            movement = movement.add(right.scale(speed));
        }
        if (client.options.keyLeft.isDown()) {
            movement = movement.subtract(right.scale(speed));
        }
        if (client.options.keyJump.isDown()) {
            movement = movement.add(up.scale(speed));
        }
        if (client.options.keyShift.isDown()) {
            movement = movement.subtract(up.scale(speed));
        }

        this.cameraX += movement.x;
        this.cameraY += movement.y;
        this.cameraZ += movement.z;
    }

    public Vec3 getInterpolatedCameraPos(float tickDelta) {
        double x = Mth.lerp(tickDelta, prevCameraX, cameraX);
        double y = Mth.lerp(tickDelta, prevCameraY, cameraY);
        double z = Mth.lerp(tickDelta, prevCameraZ, cameraZ);
        return new Vec3(x, y, z);
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }
}
