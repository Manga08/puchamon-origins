package local.migaja.skins;

import java.util.*;
import net.minecraft.class_3222;
import net.minecraft.class_2596;
import net.minecraft.class_2817;
import xyz.nikitacartes.easyauth.interfaces.PlayerAuth;
import com.quickskin.mod.networking.payloads.ProtocolHelloPayload;
import dev.architectury.impl.NetworkAggregator.BufCustomPacketPayload;
import de.maxhenkel.voicechat.net.RequestSecretPacket;
import xaero.lib.common.packet.payload.PacketPayload;
import xaero.lib.common.packet.PacketHandlerFull;
import xaero.hud.packet.basic.HandshakePacket;
import io.netty.buffer.Unpooled;
import net.minecraft.class_2540;

/** Holds ONLY a bounded, read-only protocol hello. Uploads and gameplay never enter this queue. */
public final class AuthHelloQueue {
 private record Pending(class_3222 player,class_2817 packet,long expires){}
 private static final Map<UUID,Pending> QUEUE=new LinkedHashMap<>();
 private static final Map<UUID,Pending> VOICE_QUEUE=new LinkedHashMap<>();
 private static final Map<UUID,Pending> MAP_QUEUE=new LinkedHashMap<>();
 private static final int LIMIT=64;
 public static synchronized void remember(class_3222 player,class_2596<?> packet){
  if(!(packet instanceof class_2817 custom)||((PlayerAuth)player).easyAuth$isAuthenticated())return;
  var payload=custom.comp_1647();
  if(payload instanceof PacketPayload<?> map&&map.getPacket() instanceof HandshakePacket hello){
   if(!map.method_56479().comp_2242().toString().equals("xaerominimap:main")||map.getPacketType()==null||map.getPacketType().getIndex()!=1)return;
   var buffer=new class_2540(Unpooled.buffer(4));
   try{hello.write(buffer);if(buffer.readableBytes()!=4||buffer.readInt()!=3)return;}finally{buffer.release();}
   if(MAP_QUEUE.size()>=LIMIT&&!MAP_QUEUE.containsKey(player.method_5667()))return;
   var handler=(PacketHandlerFull)xaero.common.HudMod.INSTANCE.getMessageHandler();
   var copy=new class_2817(handler.createPayload(new HandshakePacket()));
   MAP_QUEUE.put(player.method_5667(),new Pending(player,copy,System.nanoTime()+180_000_000_000L));return;
  }
  if(payload instanceof RequestSecretPacket voice){
   if(voice.getCompatibilityVersion()!=20||VOICE_QUEUE.size()>=LIMIT&&!VOICE_QUEUE.containsKey(player.method_5667()))return;
   VOICE_QUEUE.put(player.method_5667(),new Pending(player,new class_2817(new RequestSecretPacket(20)),System.nanoTime()+180_000_000_000L));return;
  }
  if(payload instanceof ProtocolHelloPayload hello){if(hello.nonce()<=0)return;}
  else if(payload instanceof BufCustomPacketPayload buf){
   if(!buf.method_56479().comp_2242().toString().equals("quickskin:protocol_hello")||buf.payload().length>64)return;
   custom=new class_2817(new BufCustomPacketPayload(buf._type(),buf.payload().clone()));
  }else return;
  if(QUEUE.size()>=LIMIT&&!QUEUE.containsKey(player.method_5667()))return;
  QUEUE.put(player.method_5667(),new Pending(player,custom,System.nanoTime()+180_000_000_000L));
 }
 public static synchronized int pending(){return QUEUE.size()+VOICE_QUEUE.size()+MAP_QUEUE.size();}
 public static synchronized void tick(Object server){
  for(var queue:List.of(QUEUE,VOICE_QUEUE,MAP_QUEUE)){
  Iterator<Pending> it=queue.values().iterator();
  while(it.hasNext()){
   Pending p=it.next();class_3222 player=p.player();
   if(System.nanoTime()>p.expires()||!player.field_13987.method_48106()){it.remove();continue;}
   if(!((PlayerAuth)player).easyAuth$isAuthenticated())continue;
   it.remove();
   // Normal packet handler performs its original nonce, connection and rate-limit checks.
   p.packet().method_12199(player.field_13987);
   System.out.println("[Migaja Skin Bridge] Replayed "+(queue==VOICE_QUEUE?"Voice Chat handshake":queue==MAP_QUEUE?"Xaero Minimap handshake":"Quick Skin hello")+" AFTER authentication");
  }
  }
 }
}
