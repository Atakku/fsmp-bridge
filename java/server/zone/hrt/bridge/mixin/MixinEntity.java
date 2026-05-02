// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.bridge.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zone.hrt.bridge.Bridge;

@Mixin(Entity.class)
abstract class MixinEntity {
  @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"), method = "adjustSpawnLocation")
  private BlockPos getSpawnData(ServerLevel level) {
    System.out.println(((Object) this).toString());
    if (((Object) this) instanceof ServerPlayer p) {
      BlockPos pos = Bridge.LOC_CACHE.get(p.getUUID());
      if (pos != null) {
        return pos;
      }
    }
    return level.getSharedSpawnPos();
  }
}
