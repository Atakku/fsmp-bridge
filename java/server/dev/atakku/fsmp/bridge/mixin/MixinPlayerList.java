// Copyright 2025 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge.mixin;

import java.net.SocketAddress;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.atakku.fsmp.bridge.Bridge;

@Mixin(PlayerList.class)
abstract class MixinPlayerList {
  @Inject(method = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
  private void canPlayerLogin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Component> cir) {
    if (Bridge.getUserData(profile.getId(), true) != null) {
      return;
    }
    cir.setReturnValue(Component.literal(
        "You need to link your account on https://link.neko.rs and be verified on fem.place to play on this server"));
    cir.cancel();
  }

  @Inject(method = "Lnet/minecraft/server/players/PlayerList;placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/network/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V", at = @At("TAIL"))
  private void placeNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie ccd,
      CallbackInfo info) {
    Bridge.onPlayerJoin(player, player.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1);
  }
}
