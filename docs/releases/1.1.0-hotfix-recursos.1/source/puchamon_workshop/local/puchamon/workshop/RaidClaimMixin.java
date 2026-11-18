package local.puchamon.workshop.mixin;
import java.util.*;
import java.util.concurrent.*;
import com.necro.raid.dens.common.network.packets.RewardResponsePacket;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidRewardPostEvent;
import local.puchamon.workshop.RewardFlags;
import java.util.function.Consumer;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Preserve the capture choice after automatic loot; serialize claims on the server. */
@Mixin(value=RewardResponsePacket.class,remap=false)
public abstract class RaidClaimMixin {
 @Unique private static final Set<UUID> puchamon$claiming=new HashSet<>();
 @Inject(method="handleServer",at=@At("HEAD"),cancellable=true)
 private void claimOnce(class_3222 player,CallbackInfo ci){
  ci.cancel();var id=player.method_5667();var handler=RaidHelper.REWARD_QUEUE.get(id);
  if(handler==null||!puchamon$claiming.add(id))return;
  boolean capture=((RewardResponsePacket)(Object)this).catchPokemon();
  CompletableFuture<Boolean> future;
  try{future=capture?handler.givePokemonToPlayer(player):handler.giveItemToPlayer(player,false);}
  catch(RuntimeException error){puchamon$claiming.remove(id);throw error;}
  future.whenCompleteAsync((success,error)->{
   puchamon$claiming.remove(id);
   if(error==null&&Boolean.TRUE.equals(success)){
    handler.raidBoss((Consumer<RaidBoss>)boss->{
     var flags=(RewardFlags)handler;
     if(!flags.puchamon$money()){
      flags.puchamon$restore(flags.puchamon$items(),true);
      RaidEvents.RAID_REWARD_POST.emit(new RaidRewardPostEvent(boss,player));
     }
    },player.method_5682());
    RaidHelper.REWARD_QUEUE.remove(id,handler);
   }
  },player.method_5682());
 }
}
