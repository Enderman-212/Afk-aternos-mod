package com.afkmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AfkFarmerMod implements ClientModInitializer {

    public static boolean isRunning = false;

    private static int state = 0;
    private static int tickCounter = 0;

    // ~2 blocks at normal walk speed (~4.3 b/s) = ~10 ticks
    private static final int MOVE_TICKS = 10;
    private static final int WAIT_TICKS = 20; // 1 second
    private static final int CLICK_TICKS = 2;
    private static final int ROTATE_TICKS = 10;
    private static final float ROTATE_DEGREES_PER_TICK = 90.0f / ROTATE_TICKS;

    private static boolean prevRightAlt = false;
    private static boolean prevLeftAlt = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            long handle = client.getWindow().getHandle();
            boolean rightAltNow = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT);
            boolean leftAltNow  = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT);

            // Right Alt rising edge → START
            if (rightAltNow && !prevRightAlt && !isRunning) {
                isRunning = true;
                state = 0;
                tickCounter = 0;
                client.player.sendMessage(
                    Text.literal("§a[AFK Farmer] Started! Press LEFT ALT to stop."), true);
            }

            // Left Alt rising edge → STOP
            if (leftAltNow && !prevLeftAlt && isRunning) {
                isRunning = false;
                releaseAll(client);
                client.player.sendMessage(
                    Text.literal("§c[AFK Farmer] Stopped."), true);
            }

            prevRightAlt = rightAltNow;
            prevLeftAlt  = leftAltNow;

            if (!isRunning) return;

            switch (state) {
                case 0: // Move forward
                    pressKey(client.options.forwardKey, true);
                    pressKey(client.options.backKey, false);
                    tickCounter++;
                    if (tickCounter >= MOVE_TICKS) {
                        pressKey(client.options.forwardKey, false);
                        tickCounter = 0;
                        state = 1;
                    }
                    break;

                case 1: // Wait 1 second
                    tickCounter++;
                    if (tickCounter >= WAIT_TICKS) {
                        tickCounter = 0;
                        state = 2;
                    }
                    break;

                case 2: // Move back
                    pressKey(client.options.backKey, true);
                    pressKey(client.options.forwardKey, false);
                    tickCounter++;
                    if (tickCounter >= MOVE_TICKS) {
                        pressKey(client.options.backKey, false);
                        tickCounter = 0;
                        state = 3;
                    }
                    break;

                case 3: // Left click once
                    tickCounter++;
                    if (tickCounter == 1) {
                        pressKey(client.options.attackKey, true);
                    } else if (tickCounter >= CLICK_TICKS) {
                        pressKey(client.options.attackKey, false);
                        tickCounter = 0;
                        state = 4;
                    }
                    break;

                case 4: // Rotate 90 degrees right
                    client.player.setYaw(client.player.getYaw() + ROTATE_DEGREES_PER_TICK);
                    tickCounter++;
                    if (tickCounter >= ROTATE_TICKS) {
                        tickCounter = 0;
                        state = 0;
                    }
                    break;
            }
        });
    }

    /**
     * Press or release a key binding without using the deprecated
     * KeyBinding.setKeyPressed(InputUtil.Key, boolean) overload that
     * was removed in 1.21.11. Instead we call setPressed() directly
     * and manually tick the timesPressed counter.
     */
    private void pressKey(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        if (pressed) {
            // Simulate one "press" event so the game registers the input
            key.onKeyPressed();
        }
    }

    private void releaseAll(MinecraftClient client) {
        pressKey(client.options.forwardKey, false);
        pressKey(client.options.backKey, false);
        pressKey(client.options.attackKey, false);
        state = 0;
        tickCounter = 0;
    }
}
