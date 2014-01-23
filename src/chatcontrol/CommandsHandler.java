package chatcontrol;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import chatcontrol.Utils.Common;
import chatcontrol.Utils.Permissions;

public class CommandsHandler implements CommandExecutor {

	List<String> validParameters = Arrays.asList("-silent", "-s", "-anonymous", "-a"); // TODO Keep updated with the parameters below.

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(Common.colorize("&3ChatControl &8// &fRunning &7v" + ChatControl.plugin.getDescription().getVersion()));
			sender.sendMessage(Common.colorize("&3ChatControl &8// &fBy &7kangarko &f� 2013 - 2014"));
			if(!Bukkit.getIp().equalsIgnoreCase("93.91.250.138") && Bukkit.getPort() != 27975) {
				sender.sendMessage(Common.colorize("&3ChatControl &8// &fWebsite: &7http://ultracraft.6f.sk" + (new Random().nextInt(7) == 1 ? " &b< Pr�� si zahra�!" : "") ));	
			}
			return false;
		}

		String argument = args[0];
		String volba = args.length >= 2 ? args[1] : "";
		String dovod = "";

		if(sender.hasPermission(Permissions.Commands.global_perm) && volba.startsWith("-") && !validParameters.contains(volba)) {
			Common.sendRawMsg(sender, ChatControl.Config.getString("Localization.Wrong_Parameters").replace("%params", validParameters.toString().replace("[", "").replace("]", "")));
			return false;
		}

		if(args.length > 1) {
			for(int i = 1; i < args.length; i++) {
				dovod += " " + args[i];
			}
		}

		/** 
		 * MUTE COMMAND
		 */
		if (argument.equalsIgnoreCase("mute") || argument.equalsIgnoreCase("m")) {

			if(!sender.isOp() && !sender.hasPermission(Permissions.Commands.mute)) {
				Common.sendMsg(sender, "Localization.No_Permission");
				return false;
			}

			if(ChatControl.muted){
				ChatControl.muted = false;

				if(volba.equalsIgnoreCase("-silent") || volba.equalsIgnoreCase("-s")) {
					// do nothing
				} else if (volba.equalsIgnoreCase("-anonymous") || volba.equalsIgnoreCase("-a")) {
					Common.broadcastMsg(sender, "Mute.Broadcast", "Localization.Broadcast_Silent_Unmute", "");
				} else {
					Common.broadcastMsg(sender, "Mute.Broadcast", "Localization.Broadcast_Unmute", "");
				}

				Common.sendMsg(sender, "Localization.Successful_Unmute");

			} else {
				ChatControl.muted = true;					

				if(volba.equalsIgnoreCase("-silent") || volba.equalsIgnoreCase("-s")) {
					// do nothing
				} else if (volba.equalsIgnoreCase("-anonymous") || volba.equalsIgnoreCase("-a")) {
					Common.broadcastMsg(sender, "Mute.Broadcast", "Localization.Broadcast_Silent_Mute", "");
				} else {
					Common.broadcastMsg(sender, "Mute.Broadcast", "Localization.Broadcast_Mute", dovod);
				}

				Common.sendMsg(sender, "Localization.Successful_Mute");
			}
			return false;
		}

		/** 
		 * CLEAR COMMAND 
		 */
		if (argument.equalsIgnoreCase("clear") || argument.equalsIgnoreCase("c")) {

			if(!sender.isOp() && !sender.hasPermission(Permissions.Commands.clear)) {
				Common.sendMsg(sender, "Localization.No_Permission");
				return false;
			}

			if(volba.equalsIgnoreCase("console") ||volba.equalsIgnoreCase("konzole") || volba.equalsIgnoreCase("konzola")){
				for(int i = 0; i < ChatControl.Config.getInt("Clear.Amount_Of_Lines_To_Clear_In_Console", 300); i++){
					System.out.println("           ");
				}
				Common.sendMsg(sender, "Localization.Successful_Console_Clear");
				return false;
			}

			for(Player pl : Bukkit.getOnlinePlayers()){
				if(ChatControl.Config.getBoolean("Clear.Do_Not_Clear_For_Staff") && (pl.isOp() || pl.hasPermission(Permissions.Bypasses.chat_clear))){
					Common.sendMsg(pl, "Localization.Staff_Chat_Clear_Message");
					continue;
				}
				for(int i = 0; i < 120; i++){
					pl.sendMessage("�r      ");
				}
			}

			if(volba.equalsIgnoreCase("-silent") || volba.equalsIgnoreCase("-s")) {
				// do nothing
			} else if (volba.equalsIgnoreCase("-anonymous") || volba.equalsIgnoreCase("-a")) {
				Common.broadcastMsg(sender, "Clear.Broadcast", "Localization.Broadcast_Silent_Clear", "");
			} else {
				Common.broadcastMsg(sender, "Clear.Broadcast", "Localization.Broadcast_Clear", dovod);
			}
			return false;
		}

		/**
		 * RELOAD COMMAND
		 */

		if (argument.equalsIgnoreCase("reload") || argument.equalsIgnoreCase("znovunacitat") || argument.equalsIgnoreCase("r")) {

			if(!sender.isOp() && !sender.hasPermission(Permissions.Commands.reload)){
				Common.sendMsg(sender, "Localization.No_Permission");
				return false;
			}

			ChatControl.plugin.reloadConfig();
			Bukkit.getPluginManager().disablePlugin(ChatControl.plugin);
			Bukkit.getPluginManager().enablePlugin(ChatControl.plugin);

			Common.sendMsg(sender, "Localization.Reload_Complete");
			return false;
		}

		if (argument.equalsIgnoreCase("commands") || argument.equalsIgnoreCase("?") || argument.equalsIgnoreCase("list")) {

			if(!sender.isOp() && !sender.hasPermission(Permissions.Commands.command_list)){
				Common.sendMsg(sender, "Localization.No_Permission");
				return false;
			}
			Common.sendRawMsg(sender, 
					" ",
					"&3  ChatControl &f(v" + ChatControl.plugin.getDescription().getVersion() + ")",
					"&2  [] &f= optional arguments (use only 1 at once)",
					" ",
					"  &7/chc mute &2[&7-silent&2] [&7-anonymous&2] [&7reason&2] &e- Chat clear.",
					"  &7/chc clear &2[&7-silent&2] [&7-anonymous&2] [&7reason&2] &e- Chat (un)mute.",
					"  &7/chc reload &e- Reload configuration.",
					"  &7/chc list &e- Command list."
					);
			return false;
		}

		// AK BOL UVEDENY NEPLATNY ARGUMENT
		if(sender.hasPermission(Permissions.Commands.global_perm)) {
			Common.sendMsg(sender, "Localization.Wrong_Args");
		} else {
			Common.sendMsg(sender, "Localization.No_Permission");
		}
		
		return false;
	}

}