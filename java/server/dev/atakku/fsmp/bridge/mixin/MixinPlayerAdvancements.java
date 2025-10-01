// Copyright 2025 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.atakku.fsmp.bridge.Bridge;

@Mixin(PlayerAdvancements.class)
abstract class MixinPlayerAdvancements {
  @Shadow
  ServerPlayer player;

  @Inject(method = "Lnet/minecraft/server/PlayerAdvancements;award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z", at = @At(value = "INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
  public void award(AdvancementHolder adve, String criterionName, CallbackInfoReturnable<Boolean> cir) {
    if (adve == null) {
      return;
    }
    Advancement adv = adve.value();
    if (adv != null && adv.display() != null && adv.display().isPresent() && adv.display().get().shouldAnnounceChat()) {
      Bridge.onPlayerAdvancement(player, adv);
    }
  }
}
