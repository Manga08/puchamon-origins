package local.puchamon.workshop;
import net.minecraft.*;import sophisticated.building.utilities.*;import sophisticated.building.item.upgrade.BuildingUpgradeHelper;import java.util.*;
public final class BuildGuard {
 public static final Map<UUID,Long> requests=new HashMap<>();
 public static int limit(class_1657 p){return Math.min(256,Math.max(32,BuildingUpgradeHelper.getMaxBlocksForPlayer(p)));}
 public static boolean request(class_1657 p,BlockSet blocks){
  if(!(p instanceof class_3222 sp)||!Healing.ready(sp)||blocks.size()>limit(p))return false;
  long now=p.method_37908().method_8510(),old=requests.getOrDefault(p.method_5667(),Long.MIN_VALUE/2);if(now-old<5)return false;requests.put(p.method_5667(),now);return true;
 }
 public static boolean valid(class_1657 p,BlockEntry b,boolean breaking){
  if(!(p instanceof class_3222 sp)||!Healing.ready(sp))return false;
  var w=p.method_37908();var pos=b.blockPos;
  if(!w.method_24794(pos)||!w.method_8505(p,pos)||!w.method_22340(pos)||p.method_5707(class_243.method_24953(pos))>24*24)return false;
  var before=w.method_8320(pos);if(w.method_8321(pos)!=null||before.method_26214(w,pos)<0||before.method_26204() instanceof class_2323)return false;
  String oldId=class_7923.field_41175.method_10221(before.method_26204()).toString();if(oldId.startsWith("securitycraft:")||oldId.startsWith("lootr:")||oldId.startsWith("legendarymonuments:"))return false;
  if(breaking)return false; // Mass breaking and survival undo are disabled; Ultimine remains available.
  if(b.newBlockState==null||b.newBlockState.method_26215()||b.item==null||b.newBlockState.method_26204().method_8389()!=b.item)return false;
  if(b.newBlockState.method_31709())return false;
  String id=class_7923.field_41175.method_10221(b.newBlockState.method_26204()).toString();if(id.startsWith("securitycraft:")||id.startsWith("lootr:"))return false;
  var defaults=b.newBlockState.method_26204().method_9564();
  for(var prop:b.newBlockState.method_11656().keySet()){
   String name=prop.method_11899(),value=b.newBlockState.method_11654(prop).toString().toLowerCase(Locale.ROOT);
   if(name.equals("type")&&value.equals("double"))return false;
   if(name.equals("half")&&Set.of("upper","lower").contains(value))return false;
   if(Set.of("facing","axis","horizontal_axis","rotation","shape","face","orientation").contains(name))continue;
   if(name.equals("half")&&Set.of("top","bottom").contains(value))continue;
   if(!b.newBlockState.method_11654(prop).equals(defaults.method_11654(prop)))return false;
  }return true;
 }
}
