package local.puchamon.friends;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.commands.HomeSetCommand;
import com.fibermc.essentialcommands.commands.HomeCommand;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.teleportation.QueuedPlayerTeleport;
import xyz.nikitacartes.easyauth.interfaces.PlayerAuth;

public final class Friends implements ModInitializer {
 public void onInitialize(){
  CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->{
   dispatcher.register(class_2170.method_9247("sethome")
    .requires(s->s.method_9228() instanceof class_3222 && ECPerms.check(s,"essentialcommands.home.set",0))
    .executes(c->{checkReady(c.getSource().method_9207());return new HomeSetCommand().runDefault(c);})
    .then(class_2170.method_9244("home_name",StringArgumentType.word())
     .executes(c->{checkReady(c.getSource().method_9207());return new HomeSetCommand().run(c);})));
   dispatcher.register(class_2170.method_9247("home")
    .executes(c->{checkHome(c);return new HomeCommand().runDefault(c);})
    .then(class_2170.method_9244("home_name",StringArgumentType.word())
     .requires(ECPerms.require("essentialcommands.home.tp",0))
     .suggests(HomeCommand.Suggestion.LIST_SUGGESTION_PROVIDER)
     .executes(c->{checkHome(c);return new HomeCommand().run(c);})));
  });
 }
 public static int directTpa(CommandContext<class_2168> context)throws CommandSyntaxException{
  var caller=context.getSource().method_9207();
  var target=class_2186.method_9315(context,"target_player");
  return directTpaPlayers(caller,target);
 }
 public static int directTpaPlayers(class_3222 caller,class_3222 target)throws CommandSyntaxException{
  checkReady(caller);
  if(!authenticated(target))throw error("El destinatario debe iniciar sesión antes del teletransporte.");
  if(!target.method_5805())throw error("El destinatario debe estar vivo. Reaparece antes de solicitar un teletransporte.");
  if(caller==target)throw error("No puedes teletransportarte a ti mismo.");
  PlayerTeleporter.requestTeleport(new QueuedPlayerTeleport(caller,target));
  return 1;
 }
 private static void checkHome(CommandContext<class_2168> context)throws CommandSyntaxException{
  if(!ECPerms.check(context.getSource(),"essentialcommands.home.tp",0))throw error("No tienes permiso para usar home.");
  checkReady(context.getSource().method_9207());
 }
 public static void checkReady(class_3222 player)throws CommandSyntaxException{
  if(!authenticated(player))throw error("Ambos jugadores deben iniciar sesión antes de usar el teletransporte.");
  if(!player.method_5805())throw error("El jugador debe estar vivo.");
 }
 public static boolean authenticated(class_3222 player){
  return ((PlayerAuth)player).easyAuth$isAuthenticated();
 }
 private static CommandSyntaxException error(String message){return new SimpleCommandExceptionType(class_2561.method_43470(message)).create();}
}
