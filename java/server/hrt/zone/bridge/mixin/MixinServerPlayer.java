// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package hrt.zone.bridge.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import hrt.zone.bridge.Bridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class MixinServerPlayer {
  @Inject(at = @At("HEAD"), method = "Lnet/minecraft/server/level/ServerPlayer;die(Lnet/minecraft/world/damagesource/DamageSource;)V")
  private void die(DamageSource source, CallbackInfo ci) {
    ServerPlayer spe = (ServerPlayer) (Object) this;
    Bridge.onPlayerDeath(spe, source);
  }
}
