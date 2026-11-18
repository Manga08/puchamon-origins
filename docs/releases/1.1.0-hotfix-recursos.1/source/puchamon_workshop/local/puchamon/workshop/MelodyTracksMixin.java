package local.puchamon.workshop.mixin;
import com.bpmelodies.client.playback.BackpackPlaybackSession;import immersive_melodies.resources.*;import net.minecraft.class_2960;import com.google.gson.*;import java.nio.charset.StandardCharsets;import java.util.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=BackpackPlaybackSession.class,remap=false)
public abstract class MelodyTracksMixin {
 @Shadow @Final public class_2960 melodyId;@Shadow @Final public class_2960 instrumentItemId;@Shadow private Melody resolvedMelody;
 @Unique private static final JsonObject puchamon$presets=loadPresets();
 @Unique private static JsonObject loadPresets(){try(var in=BackpackPlaybackSession.class.getResourceAsStream("/puchamon-instrument-presets.json")){return JsonParser.parseString(new String(in.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("songs");}catch(Exception e){throw new IllegalStateException("Missing Puchamon instrument arrangement",e);}}
 @Redirect(method="tick",at=@At(value="INVOKE",target="Limmersive_melodies/resources/Track;getNotes()Ljava/util/List;"))
 private List<Note> arrangedNotes(Track track){
  if(!puchamon$presets.has(melodyId.toString()))return track.getNotes();
  var song=puchamon$presets.getAsJsonObject(melodyId.toString());if(!song.has(instrumentItemId.toString()))return track.getNotes();
  int index=resolvedMelody.getTracks().indexOf(track);for(var selected:song.getAsJsonArray(instrumentItemId.toString()))if(selected.getAsInt()==index)return track.getNotes();return List.of();
 }
}
