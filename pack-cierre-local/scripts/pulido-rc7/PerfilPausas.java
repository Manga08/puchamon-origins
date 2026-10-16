import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
/** Outputs aggregates only, never environment, file paths from events, credentials or arguments. */
public class PerfilPausas {
 public static void main(String[] args)throws Exception{
  var pauses=new ArrayList<Double>();var collections=new HashMap<String,Integer>();var render=new HashMap<String,Integer>();
  double fileMs=0;int filesSlow=0,renderSamples=0;var longEvents=new ArrayList<String>();java.time.Instant firstSample=null,lastSample=null;
  try(var input=new RecordingFile(Path.of(args[0]))){while(input.hasMoreEvents()){
   var e=input.readEvent();var type=e.getEventType().getName();double ms=e.getDuration().toNanos()/1e6;
   if(type.equals("jdk.GCPhasePause")){pauses.add(ms);if(ms>=50)longEvents.add("GC_PAUSE\t"+e.getStartTime()+"\t"+ms);}
   if(type.equals("jdk.GarbageCollection"))collections.merge(e.getString("cause"),1,Integer::sum);
   if(type.equals("jdk.FileRead")||type.equals("jdk.FileWrite")){if(ms>=20){filesSlow++;fileMs+=ms;}}
   if(type.equals("jdk.ExecutionSample")){
    if(firstSample==null||e.getStartTime().isBefore(firstSample))firstSample=e.getStartTime();if(lastSample==null||e.getStartTime().isAfter(lastSample))lastSample=e.getStartTime();
    var t=e.getThread("sampledThread");if(t!=null&&t.getJavaName().equals("Render thread")){
     renderSamples++;var trace=e.getStackTrace();if(trace!=null){var unique=new HashSet<String>();for(var f:trace.getFrames()){
      String name=f.getMethod().getType().getName();if(name.startsWith("java.")||name.startsWith("jdk.")||name.startsWith("net.minecraft."))continue;
      unique.add(name.split("\\.")[0]+"."+(name.contains(".")?name.split("\\.")[1]:""));}
      for(var name:unique)render.merge(name,1,Integer::sum);
     }
    }
   }
  }}
  Collections.sort(pauses);System.out.println("FILE\t"+Path.of(args[0]).getFileName());
  System.out.println("EXECUTION_SAMPLE_WINDOW\t"+firstSample+"\t"+lastSample);
  System.out.println("GC_COUNT\t"+pauses.size()+"\tTOTAL_MS\t"+pauses.stream().mapToDouble(x->x).sum());
  if(!pauses.isEmpty())System.out.println("GC_P95_MS\t"+pauses.get((int)(pauses.size()*.95))+"\tMAX_MS\t"+pauses.getLast());
  System.out.println("GC_CAUSES\t"+collections);System.out.println("FILE_IO_20MS_PLUS\t"+filesSlow+"\tTOTAL_MS\t"+fileMs);
  System.out.println("RENDER_SAMPLES\t"+renderSamples+"\tOverlapping inclusive sampled stacks, NOT exclusive CPU percent:");
  render.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(15).forEach(e->System.out.println("STACK_NAMESPACE\t"+e.getKey()+"\t"+e.getValue()));
  longEvents.forEach(System.out::println);
 }
}
