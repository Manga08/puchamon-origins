package local.puchamon.radar;
import net.minecraft.*;
public record RadarPayload(String json) implements class_8710 {
 public static final class_8710.class_9154<RadarPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("puchamon","pokemon_radar_v1"));
 public static final class_9139<class_9129,RadarPayload> CODEC=class_9139.method_56438((value,buf)->buf.method_10788(value.json(),196608),buf->new RadarPayload(buf.method_10800(196608)));
 public class_8710.class_9154<? extends class_8710> method_56479(){return ID;}
}
