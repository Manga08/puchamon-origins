/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.cobblemon.mod.common.entity.pokemon.PokemonEntity
 *  net.minecraft.class_1799
 *  net.minecraft.class_3222
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package io.github.jaypixl.simpleballswap.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.class_1799;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={PokemonEntity.class}, remap=false)
public interface PokemonEntityInvoker {
    @Invoker(value="showInteractionWheel")
    public void simpleballswap$showInteractionWheel(class_3222 var1, class_1799 var2);
}

