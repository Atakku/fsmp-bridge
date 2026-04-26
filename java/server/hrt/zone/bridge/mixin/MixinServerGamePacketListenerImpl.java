// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package hrt.zone.bridge.mixin;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import hrt.zone.bridge.Bridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class MixinServerGamePacketListenerImpl {
  @Shadow
  public ServerPlayer player;

  @Inject(method = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V", at = @At("HEAD"))
  private void broadcastChatMessage(PlayerChatMessage msg, CallbackInfo ci) {
    if (msg.decoratedContent().getString().startsWith("/"))
      return;
    Bridge.onPlayerMessage(player, msg);
  }

  @Inject(method = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;onDisconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
  private void onDisconnect(DisconnectionDetails info, CallbackInfo ci) {
    Bridge.onPlayerLeft(player, info.reason());
  }
}
