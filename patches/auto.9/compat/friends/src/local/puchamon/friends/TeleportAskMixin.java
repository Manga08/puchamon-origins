package local.puchamon.friends.mixin;
import local.puchamon.friends.Friends;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.class_2168;
import com.fibermc.essentialcommands.commands.TeleportAskCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Only /tpa moves its caller; never authorizes /tp or /tpahere. */
@Mixin(value=TeleportAskCommand.class,remap=false)
public abstract class TeleportAskMixin {
 @Inject(method="run",at=@At("HEAD"),cancellable=true,remap=false)
 private void puchamon$direct(CommandContext<class_2168> context,CallbackInfoReturnable<Integer> result)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
  result.setReturnValue(Friends.directTpa(context));
 }
}
