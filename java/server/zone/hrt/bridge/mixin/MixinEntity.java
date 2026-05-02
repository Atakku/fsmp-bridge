// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.bridge.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zone.hrt.bridge.Bridge;

@Mixin(Entity.class)
abstract class MixinEntity {
  @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"), method = "adjustSpawnLocation")
  public BlockPos getSpawnData(Level level) {
    if (((Object) this) instanceof ServerPlayer p) {
      BlockPos pos = Bridge.LOC_CACHE.get(p.getUUID());
      if (pos != null) {
        return pos;
      }
    }
    return level.getSharedSpawnPos();
  }
}
