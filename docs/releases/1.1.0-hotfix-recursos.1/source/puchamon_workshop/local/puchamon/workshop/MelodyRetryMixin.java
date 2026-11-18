package local.puchamon.workshop.mixin;
import immersive_melodies.resources.*;import net.minecraft.class_2960;import java.util.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ClientMelodyManager.class,remap=false)
public abstract class MelodyRetryMixin {
 @Shadow @Final static Map<class_2960,Melody> melodies;
 @Shadow @Final static Set<class_2960> requested;
 @Unique private static final Map<class_2960,Long> puchamon$attempts=new HashMap<>();
 @Inject(method="getMelody",at=@At("HEAD"))
 private static void retryDroppedRequest(class_2960 id,CallbackInfoReturnable<Melody> ci){
  if(melodies.containsKey(id)){puchamon$attempts.remove(id);return;}
  long now=System.nanoTime(),previous=puchamon$attempts.getOrDefault(id,Long.MIN_VALUE/2);
  if(!requested.contains(id)||now-previous>=10_000_000_000L){requested.remove(id);puchamon$attempts.put(id,now);}
 }
}
