package local.puchamon.workshop.mixin;
import net.minecraft.class_304;import sophisticated.building.ClientEvents;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ClientEvents.class,remap=false)
public abstract class BuildKeysMixin {
 @Shadow public static class_304[] keyBindings;
 @ModifyArg(method="<clinit>",at=@At(value="INVOKE",target="Lnet/minecraft/class_304;<init>(Ljava/lang/String;Lnet/minecraft/class_3675$class_307;ILjava/lang/String;)V"),index=2)
 private static int cleanDefaults(int value){return switch(value){case 342->346;case 334->332;case 90,89->-1;default->value;};}
 @Inject(method="isKeybindDown",at=@At("HEAD"),cancellable=true)
 private static void removeHardcodedLeftAlt(int index,CallbackInfoReturnable<Boolean> ci){if(index==0)ci.setReturnValue(keyBindings[0].method_1434());}
}
