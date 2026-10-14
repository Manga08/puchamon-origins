package local.puchamon.gate;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import java.util.concurrent.CompletableFuture;
public final class GateClient implements ClientModInitializer {
 public void onInitializeClient(){
  ClientLoginNetworking.registerGlobalReceiver(PackGate.CHANNEL,(client,handler,buffer,callbacks)->{
   int protocol=buffer.method_10816();buffer.method_10800(128);
   var reply=PacketByteBufs.create();reply.method_10804(protocol==1?1:0);reply.method_10814(PackGate.compatibility());
   return CompletableFuture.completedFuture(reply);
  });
 }
}
