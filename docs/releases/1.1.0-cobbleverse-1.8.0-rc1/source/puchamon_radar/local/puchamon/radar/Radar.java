package local.puchamon.radar;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
/** S2C only; clients cannot request coordinates or mutate any server state. */
public final class Radar implements ModInitializer {
 public static final int RADIUS=160,MAX_ENTRIES=256,INTERVAL_TICKS=20;
 public void onInitialize(){
  PayloadTypeRegistry.playS2C().register(RadarPayload.ID,RadarPayload.CODEC);
  ServerTickEvents.END_SERVER_TICK.register(ServerRadar::tick);
 }
}
