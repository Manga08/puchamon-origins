package local.puchamon.quests;

import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.class_2960;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;

/** A successful server-side capture, not catch_pokemon's unsupported species JSON key. */
public final class QuestGuard implements ModInitializer {
 public void onInitialize(){
  CobblemonEvents.POKEMON_CAPTURED.subscribe((Consumer<PokemonCapturedEvent>) QuestGuard::onCapture);
  System.out.println("[Puchamon Quest Guard] Exact successful-capture detection enabled for "+CapturePolicy.SPECIES.size()+" species.");
 }
 public static void onCapture(PokemonCapturedEvent event){
   String id=CapturePolicy.advancementFor(event.getPokemon().getSpecies().getResourceIdentifier().toString());
   if(id==null)return;
   var player=event.getPlayer();
   var server=player.method_5682();
   if(server==null)return;
   var advancement=server.method_3851().method_12896(class_2960.method_60654(id));
   if(advancement==null){System.err.println("[Puchamon Quest Guard] Missing definition: "+id);return;}
   // Minecraft keeps this idempotent. Reward claiming remains FTB's original personal task.
   player.method_14236().method_12878(advancement,"valid");
 }
}
