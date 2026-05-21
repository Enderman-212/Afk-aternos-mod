package com.afkmod.mixin;

import com.afkmod.AfkFarmerMod;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minimal mixin — just a placeholder so the mixins JSON is satisfied.
 * All logic lives in AfkFarmerMod via ClientTickEvents.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // No injection needed; mod logic is tick-event based.
}
