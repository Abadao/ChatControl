package kangarko.chatcontrol;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import kangarko.chatcontrol.hooks.MultiverseHook;
import kangarko.chatcontrol.hooks.TownyHook;
import kangarko.chatcontrol.model.Settings;
import kangarko.chatcontrol.utils.Common;
import kangarko.chatcontrol.utils.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatter implements Listener {

	private final Pattern COLOR_REGEX = Pattern.compile("(?i)&([0-9A-F])");
	private final Pattern MAGIC_REGEN = Pattern.compile("(?i)&([K])");
	private final Pattern BOLD_REGEX = Pattern.compile("(?i)&([L])");
	private final Pattern STRIKETHROUGH_REGEX = Pattern.compile("(?i)&([M])");
	private final Pattern UNDERLINE_REGEX = Pattern.compile("(?i)&([N])");
	private final Pattern ITALIC_REGEX = Pattern.compile("(?i)&([O])");
	private final Pattern RESET_REGEX = Pattern.compile("(?i)&([R])");

	private MultiverseHook mvHook;
	private TownyHook townyHook;

	public ChatFormatter() {
		if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
			mvHook = new MultiverseHook();
			Common.Log("&fHooked with Multiverse 2 (World Alias)!");
		}

		if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
			townyHook = new TownyHook();
			Common.Log("&fTowny 0.8x integration enabled.");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onChatFormat(AsyncPlayerChatEvent e) {
		Player pl = e.getPlayer();
		String world = pl.getWorld().getName();

		String msgFormat = Settings.Chat.Formatter.FORMAT;
		boolean rangedMode = Settings.Chat.Formatter.RANGED_MODE;
		String msg = e.getMessage();

		if (rangedMode && msg.startsWith("!") && Common.hasPerm(pl, Permissions.Formatter.GLOBAL_CHAT)) {
			rangedMode = false;
			msg = msg.substring(1);

			msgFormat = Settings.Chat.Formatter.GLOBAL_FORMAT;
		}

		msgFormat = formatColor(msgFormat);

		msg = formatColor(msg, pl, world);

		msgFormat = msgFormat.replace("%message", "%2$s").replace("%displayname", "%1$s");
		msgFormat = replacePlayerVariables(pl, msgFormat);
		msgFormat = replaceTime(msgFormat);

		e.setFormat(msgFormat);
		e.setMessage(msg);

		if (rangedMode) {
			double range = Settings.Chat.Formatter.RANGE;

			e.getRecipients().clear();
			e.getRecipients().addAll(getLocalRecipients(pl, msgFormat, range));
		}
	}

	public String replacePlayerVariables(Player pl, String format) {
		String world = pl.getWorld().getName();
		
		if (ChatControl.instance().authMe != null)
			format = format.replace("%countrycode", ChatControl.instance().authMe.getCountryCode(pl)).replace("%countryname", ChatControl.instance().authMe.getCountryName(pl));

		return format
				.replace("%pl_prefix", formatColor(ChatControl.instance().vault.getPlayerPrefix(pl)))
				.replace("%pl_suffix", formatColor(ChatControl.instance().vault.getPlayerSuffix(pl)))
				.replace("%world", getWorldAlias(world)).replace("%health", formatHealth(pl) + ChatColor.RESET)
				.replace("%player", pl.getName())
				.replace("%town", getTown(pl)).replace("%nation", getNation(pl));
	}

	private List<Player> getLocalRecipients(Player pl, String message, double range) {
		List<Player> recipients = new LinkedList<Player>();
		try {
			Location playerLocation = pl.getLocation();
			double squaredDistance = Math.pow(range, 2.0D);

			for (Player recipient : Bukkit.getOnlinePlayers())
				if (recipient.getWorld().equals(pl.getWorld()))
					if (playerLocation.distanceSquared(recipient.getLocation()) <= squaredDistance || Common.hasPerm(pl, Permissions.Formatter.OVERRIDE_RANGED))
						recipients.add(recipient);

			return recipients;
		} catch (ArrayIndexOutOfBoundsException ex) {
			Common.Debug("(Range Chat) Got " + ex.getMessage() + ", trying backup.");

			if (Common.hasPerm(pl, Permissions.Formatter.OVERRIDE_RANGED)) {
				for (Player recipient : Bukkit.getOnlinePlayers())
					if (recipient.getWorld().equals(pl.getWorld()))
						recipients.add(recipient);
				return recipients;
			}

			for (Entity en : pl.getNearbyEntities(range, range, range))
				if (en.getType() == EntityType.PLAYER)
					recipients.add((Player) en);

			return recipients;
		}
	}

	private String replaceTime(String msg) {
		Calendar c = Calendar.getInstance();

		if (msg.contains("%h"))
			msg = msg.replace("%h", String.format("%02d", c.get(10)));

		if (msg.contains("%H"))
			msg = msg.replace("%H", String.format("%02d", c.get(11)));

		if (msg.contains("%g"))
			msg = msg.replace("%g", Integer.toString(c.get(10)));

		if (msg.contains("%G"))
			msg = msg.replace("%G", Integer.toString(c.get(11)));

		if (msg.contains("%i"))
			msg = msg.replace("%i", String.format("%02d", c.get(12)));

		if (msg.contains("%s"))
			msg = msg.replace("%s", String.format("%02d", c.get(13)));

		if (msg.contains("%a"))
			msg = msg.replace("%a", c.get(9) == 0 ? "am" : "pm");

		if (msg.contains("%A"))
			msg = msg.replace("%A", c.get(9) == 0 ? "AM" : "PM");

		return msg;
	}

	private String formatColor(String string) {
		if (string == null)
			return "";

		String str = string;
		str = COLOR_REGEX.matcher(str).replaceAll("\u00A7$1");
		str = MAGIC_REGEN.matcher(str).replaceAll("\u00A7$1");
		str = BOLD_REGEX.matcher(str).replaceAll("\u00A7$1");
		str = STRIKETHROUGH_REGEX.matcher(str).replaceAll("\u00A7$1");
		str = UNDERLINE_REGEX.matcher(str).replaceAll("\u00A7$1");
		str = ITALIC_REGEX.matcher(str).replaceAll("\u00A7$1");
		str = RESET_REGEX.matcher(str).replaceAll("\u00A7$1");
		return str;
	}

	private String formatColor(String string, Player pl, String worldName) {
		if (string == null)
			return "";

		String str = string;
		if (Common.hasPerm(pl, Permissions.Formatter.COLOR))
			str = COLOR_REGEX.matcher(str).replaceAll("\u00A7$1");

		if (Common.hasPerm(pl, Permissions.Formatter.MAGIC))
			str = MAGIC_REGEN.matcher(str).replaceAll("\u00A7$1");

		if (Common.hasPerm(pl, Permissions.Formatter.BOLD))
			str = BOLD_REGEX.matcher(str).replaceAll("\u00A7$1");

		if (Common.hasPerm(pl, Permissions.Formatter.STRIKETHROUGH))
			str = STRIKETHROUGH_REGEX.matcher(str).replaceAll("\u00A7$1");

		if (Common.hasPerm(pl, Permissions.Formatter.UNDERLINE))
			str = UNDERLINE_REGEX.matcher(str).replaceAll("\u00A7$1");

		if (Common.hasPerm(pl, Permissions.Formatter.ITALIC))
			str = ITALIC_REGEX.matcher(str).replaceAll("\u00A7$1");

		str = RESET_REGEX.matcher(str).replaceAll("\u00A7$1");
		return str;
	}

	private String formatHealth(Player pl) {
		int health = (int) pl.getHealth();

		if (health > 10)
			return ChatColor.DARK_GREEN + "" + health;
		if (health > 5)
			return ChatColor.GOLD + "" + health;
		return ChatColor.RED + "" + health;
	}

	private String getWorldAlias(String world) {
		if (mvHook == null)
			return world;

		return mvHook.getColoredAlias(world);
	}

	private String getNation(Player pl) {
		if (townyHook == null)
			return "";

		return townyHook.getNation(pl);
	}

	private String getTown(Player pl) {
		if (townyHook == null)
			return "";

		return townyHook.getTownName(pl);
	}
}