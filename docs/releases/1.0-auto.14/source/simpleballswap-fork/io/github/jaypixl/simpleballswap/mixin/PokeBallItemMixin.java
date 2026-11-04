/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.cobblemon.mod.common.entity.pokemon.PokemonEntity
 *  com.cobblemon.mod.common.item.PokeBallItem
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1271
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1675
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3966
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package io.github.jaypixl.simpleballswap.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1675;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3966;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PokeBallItem.class}, remap=false)
public abstract class PokeBallItemMixin {
    @Inject(method={"method_7836"}, at={@At(value="HEAD")}, cancellable=true)
    private void simpleballswap$preventThrowWhileOpeningWheel(class_1937 world, class_1657 player, class_1268 usedHand, CallbackInfoReturnable<class_1271<class_1799>> cir) {
        if (!player.method_5715()) {
            return;
        }
        class_3966 hitResult = this.simpleballswap$findOwnedPokemonHit(player);
        if (hitResult == null) {
            return;
        }
        cir.setReturnValue(new class_1271(class_1269.field_51370, (Object)player.method_5998(usedHand)));
    }

    private class_3966 simpleballswap$findOwnedPokemonHit(class_1657 player) {
        double range = player.method_55755();
        class_243 start = player.method_33571();
        class_243 end = start.method_1019(player.method_5828(1.0f).method_1021(range));
        class_238 searchBox = player.method_5829().method_18804(end.method_1020(start)).method_1014(1.0);
        return class_1675.method_18077((class_1937)player.method_37908(), (class_1297)player, (class_243)start, (class_243)end, (class_238)searchBox, entity -> {
            if (!(entity instanceof PokemonEntity)) return false;
            PokemonEntity pokemonEntity = (PokemonEntity)entity;
            if (!player.method_5667().equals(pokemonEntity.method_6139())) return false;
            return true;
        });
    }
}

