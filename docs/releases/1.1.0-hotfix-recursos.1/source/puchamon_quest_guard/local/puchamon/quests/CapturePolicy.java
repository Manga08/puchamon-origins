package local.puchamon.quests;

import java.util.Set;

/** Pure allowlist. No inventory scans, team sharing, gifts, trades or periodic granting. */
public final class CapturePolicy {
 public static final Set<String> SPECIES=Set.of(
  "mew","mewtwo","articuno","zapdos","moltres","hooh","lugia","celebi",
  "kyogre","groudon","rayquaza","latias","latios","jirachi","deoxys",
  "regirock","regice","registeel","dialga","palkia","giratina","cresselia",
  "heatran","darkrai","arceus");
 private CapturePolicy(){}
 public static String advancementFor(String speciesIdentifier){
  if(speciesIdentifier==null||!speciesIdentifier.startsWith("cobblemon:"))return null;
  String species=speciesIdentifier.substring("cobblemon:".length());
  return SPECIES.contains(species)?"migaja_guia:legendario_"+species:null;
 }
}
