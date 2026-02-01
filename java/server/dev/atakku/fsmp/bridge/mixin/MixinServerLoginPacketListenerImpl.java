// Copyright 2025 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge.mixin;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.atakku.fsmp.bridge.Bridge;

@Mixin(ServerLoginPacketListenerImpl.class)
abstract public class MixinServerLoginPacketListenerImpl {
  @Accessor(remap = false)
  abstract public void setAuthenticatedProfile(GameProfile profile);

  @Inject(method = "startClientVerification(Lcom/mojang/authlib/GameProfile;)V", at = @At("TAIL"), remap = false)
  private void init(GameProfile profile, CallbackInfo ci) {
    String data = Bridge.getUserData(profile.getId());
    if (data != null) {
      ((AccessorGameProfile) profile).setName(data);
      this.setAuthenticatedProfile(profile);
    }
  }
}
