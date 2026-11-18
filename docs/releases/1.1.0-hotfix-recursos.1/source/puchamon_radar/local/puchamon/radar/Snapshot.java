package local.puchamon.radar;
import java.util.*;
/** Cosmetic information only. No IVs, moves, inventories, owner, hidden stats or offline locations. */
public record Snapshot(String dimension,List<Entry> entries){
 public record Entry(UUID uuid,int entityId,String species,String form,boolean shiny,List<String> aspects,double x,double y,double z){}
}
