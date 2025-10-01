// Copyright 2025 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.atakku.fsmp.bridge.Bridge;

@Mixin(ServerPlayer.class)
abstract class MixinServerPlayer {
  @Inject(at = @At("HEAD"), method = "Lnet/minecraft/server/level/ServerPlayer;die(Lnet/minecraft/world/damagesource/DamageSource;)V")
  private void die(DamageSource source, CallbackInfo ci) {
    ServerPlayer spe = (ServerPlayer) (Object) this;
    // if (spe.getWorld().getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES))
    // {
    Bridge.onPlayerDeath(spe, source);
    // }
  }
}
