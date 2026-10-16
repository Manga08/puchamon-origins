import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
public class FotogramaJfr {
 public static void main(String[] args)throws Exception{
  var start=Instant.parse(args[1]);var end=Instant.parse(args[2]);
  var counts=new HashMap<String,Integer>();var stacks=new HashMap<String,Integer>();
  try(var file=new RecordingFile(Path.of(args[0]))){while(file.hasMoreEvents()){
   var e=file.readEvent();if(e.getEndTime().isBefore(start)||e.getStartTime().isAfter(end))continue;
   var type=e.getEventType().getName();counts.merge(type,1,Integer::sum);
   if(type.equals("jdk.GCPhasePause"))System.out.println("GC\t"+e.getStartTime()+"\t"+e.getDuration().toNanos()/1e6+"ms");
   var thread=type.equals("jdk.ExecutionSample")||type.equals("jdk.NativeMethodSample")?e.getThread("sampledThread"):e.getThread();
   if(thread==null||!"Render thread".equals(thread.getJavaName()))continue;
   var trace=e.getStackTrace();if(trace==null)continue;
   var frames=trace.getFrames().stream().limit(80).map(f->f.getMethod().getType().getName()+"."+f.getMethod().getName()).toList();
   if(frames.stream().anyMatch(f->f.contains("Wmi")||f.contains("Wbem")))System.out.println("WMI_RENDER_THREAD\t"+e.getStartTime()+"\t"+type+"\t"+String.join(" > ",frames));
   stacks.merge(type+" :: "+String.join(" > ",frames),1,Integer::sum);
  }}
  System.out.println("EVENTS\t"+counts);
  stacks.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(12).forEach(e->System.out.println("STACK\t"+e.getValue()+"\t"+e.getKey()));
 }
}
