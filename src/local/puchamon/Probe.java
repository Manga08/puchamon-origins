package local.puchamon;
/** Used only by updater tests; it is not Minecraft and does not access game credentials. */
public final class Probe {
 public static void main(String[] args)throws Exception{
  if(args.length>0&&args[0].equals("--memory")){System.out.println("ACTUAL_MAX_MIB="+Runtime.getRuntime().maxMemory()/1048576L);return;}
  System.out.println("PROBE_REACHED=true");
 }
}
