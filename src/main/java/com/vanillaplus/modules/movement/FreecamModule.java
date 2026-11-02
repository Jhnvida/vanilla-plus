package com.vanillaplus.modules.movement;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class FreecamModule extends Module {
    private static @Nullable FreecamModule instance;

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

    public static @Nullable FreecamModule getInstance() {
        return instance;
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player != null) {
            this.cameraX = player.getX();
            this.cameraY = player.getEyeY();
            this.cameraZ = player.getZ();
            this.prevCameraX = cameraX;
            this.prevCameraY = cameraY;
            this.prevCameraZ = cameraZ;
            this.cameraYaw = player.getYRot();
            this.cameraPitch = player.getXRot();
        }
        if (client.levelRenderer != null) {
            client.levelRenderer.clearVisibleSections();
            if (client.levelRenderer.sectionOcclusionGraph() != null) {
                client.levelRenderer.sectionOcclusionGraph().invalidate();
            }
        }
    }

    @Override
    protected void onDisable() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player != null) {
            this.cameraX = player.getX();
            this.cameraY = player.getEyeY();
            this.cameraZ = player.getZ();
            this.prevCameraX = cameraX;
            this.prevCameraY = cameraY;
            this.prevCameraZ = cameraZ;
        }
        if (client.levelRenderer != null) {
            client.levelRenderer.clearVisibleSections();
            if (client.levelRenderer.sectionOcclusionGraph() != null) {
                client.levelRenderer.sectionOcclusionGraph().invalidate();
            }
        }
    }

    public void addCameraRotation(float yRot, float xRot) {
        this.cameraYaw += (float) (yRot * 0.15);
        this.cameraPitch = Mth.clamp(this.cameraPitch + (float) (xRot * 0.15), -90.0f, 90.0f);
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.player == null)
            return;

        this.prevCameraX = cameraX;
        this.prevCameraY = cameraY;
        this.prevCameraZ = cameraZ;

        ModConfig config = ConfigManager.getConfig();
        double speed = Math.max(0.1, config.freecamSpeed * 0.4);

        if (client.options.keySprint.isDown()) {
            speed *= 2.2;
        }

        float radYaw = (float) Math.toRadians(cameraYaw);
        float radPitch = (float) Math.toRadians(cameraPitch);

        Vec3 forward = new Vec3(
                -Mth.sin(radYaw) * Mth.cos(radPitch),
                -Mth.sin(radPitch),
                Mth.cos(radYaw) * Mth.cos(radPitch));

        Vec3 right = new Vec3(
                -Mth.cos(radYaw),
                0,
                -Mth.sin(radYaw));

        Vec3 up = new Vec3(0, 1, 0);

        Vec3 movement = Vec3.ZERO;

        if (client.options.keyUp.isDown()) {
            movement = movement.add(forward);
        }
        if (client.options.keyDown.isDown()) {
            movement = movement.subtract(forward);
        }
        if (client.options.keyRight.isDown()) {
            movement = movement.add(right);
        }
        if (client.options.keyLeft.isDown()) {
            movement = movement.subtract(right);
        }
        if (client.options.keyJump.isDown()) {
            movement = movement.add(up);
        }
        if (client.options.keyShift.isDown()) {
            movement = movement.subtract(up);
        }

        if (movement.lengthSqr() > 0.0001) {
            movement = movement.normalize().scale(speed);
            this.cameraX += movement.x;
            this.cameraY += movement.y;
            this.cameraZ += movement.z;
        }
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
