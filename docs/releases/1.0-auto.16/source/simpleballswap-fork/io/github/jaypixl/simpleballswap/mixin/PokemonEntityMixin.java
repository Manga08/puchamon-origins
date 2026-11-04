/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.cobblemon.mod.common.CobblemonCosmeticItems
 *  com.cobblemon.mod.common.api.tags.CobblemonItemTags
 *  com.cobblemon.mod.common.entity.pokemon.PokemonEntity
 *  com.cobblemon.mod.common.item.PokeBallItem
 *  com.cobblemon.mod.common.pokemon.Pokemon
 *  com.cobblemon.mod.common.pokemon.cosmetic.CosmeticItemDefinition
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_5455
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package io.github.jaypixl.simpleballswap.mixin;

import com.cobblemon.mod.common.CobblemonCosmeticItems;
import com.cobblemon.mod.common.api.tags.CobblemonItemTags;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.cosmetic.CosmeticItemDefinition;
import io.github.jaypixl.simpleballswap.SimpleBallSwapMod;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_5455;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PokemonEntity.class}, remap=false)
public abstract class PokemonEntityMixin {
    @Redirect(method={"showInteractionWheel"}, at=@At(value="INVOKE", target="Lcom/cobblemon/mod/common/CobblemonCosmeticItems;findValidCosmeticForPokemonAndItem(Lnet/minecraft/class_5455;Lcom/cobblemon/mod/common/pokemon/Pokemon;Lnet/minecraft/class_1799;)Lcom/cobblemon/mod/common/pokemon/cosmetic/CosmeticItemDefinition;"))
    private CosmeticItemDefinition simpleballswap$allowPokeBallTagAsCosmetic(class_5455 registryAccess, Pokemon pokemon, class_1799 itemStack) {
        CosmeticItemDefinition result = CobblemonCosmeticItems.findValidCosmeticForPokemonAndItem((class_5455)registryAccess, (Pokemon)pokemon, (class_1799)itemStack);
        if (result != null || !itemStack.method_31573(CobblemonItemTags.POKE_BALLS)) {
            return result;
        }
        return new CosmeticItemDefinition();
    }

    @Inject(method={"method_5992"}, at={@At(value="HEAD")}, cancellable=true)
    private void simpleballswap$preventClientBallUseWhenOpeningWheel(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        if (!player.method_37908().method_8608() || hand != class_1268.field_5808 || !player.method_5715()) {
            return;
        }
        PokemonEntity pokemonEntity = (PokemonEntity)(Object)this;
        if (!player.method_5667().equals(pokemonEntity.method_6139())) {
            return;
        }
        if (!player.method_5998(hand).method_31573(CobblemonItemTags.POKE_BALLS)) {
            return;
        }
        cir.setReturnValue(class_1269.field_51370);
    }

    @Inject(method={"offerItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void simpleballswap$handlePokeBallCosmeticOffer(class_1657 player, class_1799 stack, boolean isCosmetic, CallbackInfoReturnable<Boolean> cir) {
        class_1792 class_17922;
        if (!isCosmetic || !((class_17922 = stack.method_7909()) instanceof PokeBallItem)) {
            return;
        }
        PokeBallItem pokeBallItem = (PokeBallItem)class_17922;
        PokemonEntity pokemonEntity = (PokemonEntity)(Object)this;
        if (pokemonEntity.isBusy() || !player.method_5667().equals(pokemonEntity.method_6139())) {
            cir.setReturnValue(false);
            return;
        }
        if (player.method_37908().method_8608()) {
            cir.setReturnValue(false);
            return;
        }
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon.getCaughtBall() == pokeBallItem.getPokeBall()) {
            player.method_43496((class_2561)class_2561.method_43470((String)(pokemon.getDisplayName(false).getString() + "'s ball is already " + pokeBallItem.method_7864(stack).getString() + "!")).method_27692(class_124.field_1061));
            cir.setReturnValue(true);
            return;
        }
        if (SimpleBallSwapMod.CONFIG.returnOldBall) {
            player.method_7270(pokemon.getCaughtBall().stack(1));
        }
        pokemon.setCaughtBall(pokeBallItem.getPokeBall());
        if (!player.method_7337() && SimpleBallSwapMod.CONFIG.consumeBall) {
            stack.method_7934(1);
        }
        player.method_43496((class_2561)class_2561.method_43470((String)("Changed " + pokemon.getDisplayName(false).getString() + "'s ball to " + pokeBallItem.method_7864(stack).getString() + "!")));
        cir.setReturnValue(true);
    }
}

