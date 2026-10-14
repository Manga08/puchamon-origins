package local.puchamon.safety.mixin;
import local.puchamon.safety.MineSafety;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock","net.geforcemods.securitycraft.blocks.mines.MineBlock","net.geforcemods.securitycraft.blocks.mines.ClaymoreBlock","net.geforcemods.securitycraft.blocks.mines.BouncingBettyBlock","net.geforcemods.securitycraft.blocks.mines.TrackMineBlock"},remap=false)
public abstract class BlockMineMixin {
 @Inject(method="explode",at=@At("HEAD"),cancellable=true,require=1,remap=false)
 private void puchamon$guard(class_1937 world,class_2338 pos,CallbackInfo ci){
  if(MineSafety.blockedNear(world,pos.method_10263()+0.5,pos.method_10264()+0.5,pos.method_10260()+0.5))ci.cancel();
 }
}
