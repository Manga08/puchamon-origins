package local.migaja.skins;

import java.util.*;
import net.minecraft.class_3222;
import net.minecraft.class_2596;
import net.minecraft.class_2817;
import xyz.nikitacartes.easyauth.interfaces.PlayerAuth;
import com.quickskin.mod.networking.payloads.ProtocolHelloPayload;
import dev.architectury.impl.NetworkAggregator.BufCustomPacketPayload;

/** Holds ONLY a bounded, read-only protocol hello. Uploads and gameplay never enter this queue. */
public final class AuthHelloQueue {
 private record Pending(class_3222 player,class_2817 packet,long expires){}
 private static final Map<UUID,Pending> QUEUE=new LinkedHashMap<>();
 private static final int LIMIT=64;
 public static synchronized void remember(class_3222 player,class_2596<?> packet){
  if(!(packet instanceof class_2817 custom)||((PlayerAuth)player).easyAuth$isAuthenticated())return;
  var payload=custom.comp_1647();
  if(payload instanceof ProtocolHelloPayload hello){if(hello.nonce()<=0)return;}
  else if(payload instanceof BufCustomPacketPayload buf){
   if(!buf.method_56479().comp_2242().toString().equals("quickskin:protocol_hello")||buf.payload().length>64)return;
   custom=new class_2817(new BufCustomPacketPayload(buf._type(),buf.payload().clone()));
  }else return;
  if(QUEUE.size()>=LIMIT&&!QUEUE.containsKey(player.method_5667()))return;
  QUEUE.put(player.method_5667(),new Pending(player,custom,System.nanoTime()+180_000_000_000L));
 }
 public static synchronized int pending(){return QUEUE.size();}
 public static synchronized void tick(Object server){
  Iterator<Pending> it=QUEUE.values().iterator();
  while(it.hasNext()){
   Pending p=it.next();class_3222 player=p.player();
   if(System.nanoTime()>p.expires()||!player.field_13987.method_48106()){it.remove();continue;}
   if(!((PlayerAuth)player).easyAuth$isAuthenticated())continue;
   it.remove();
   // Normal packet handler performs its original nonce, connection and rate-limit checks.
   p.packet().method_12199(player.field_13987);
   System.out.println("[Migaja Skin Bridge] Replayed Quick Skin hello AFTER authentication");
  }
 }
}
