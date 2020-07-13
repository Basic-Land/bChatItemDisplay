package me.BingoRufus.ChatDisplay.ListenersAndExecutors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import me.BingoRufus.ChatDisplay.Main;

public class ChatDisplayListener implements Listener {

	char bell = '\u0007';

	public static Map<String, Inventory> DisplayedItem = new HashMap<String, Inventory>();
	public static Map<Player, Inventory> DisplayedShulkerBox = new HashMap<Player, Inventory>();
	public static Map<UUID, Long> DisplayItemCooldowns = new HashMap<UUID, Long>();
	String MsgName;
	String GUIName;
	Main main;
	boolean debug;
	String Version;

	public ChatDisplayListener(Main m) {
		m.reloadConfig();
		main = m;
		debug = main.getConfig().getBoolean("debug-mode");
		Version = Bukkit.getServer().getVersion().substring(Bukkit.getServer().getVersion().indexOf("(MC: ") + 5,
				Bukkit.getServer().getVersion().indexOf(")"));
		Bukkit.getPluginManager().registerEvents(new InventoryClick(main, Version), main);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {

		if (debug)
			Bukkit.getLogger().info(e.getPlayer().getName() + " sent a message");

		if (e.getPlayer().getInventory().getItemInMainHand() == null) {
			if (debug)
				Bukkit.getLogger().info(e.getPlayer().getName() + " is not holding anything");
			return;
		}

		for (String Trigger : main.getConfig().getStringList("triggers")) {
			if (e.getMessage().toUpperCase().contains(Trigger.toUpperCase())) {

				if (debug)
					Bukkit.getLogger().info(e.getPlayer().getName() + "'s message contains an item display trigger");

				if (new DisplayPermissionChecker(main, e.getPlayer()).hasPermission()) {
					String newmsg = e.getMessage().replaceFirst("(?i)" + Pattern.quote(Trigger),
							bell + "cid" + e.getPlayer().getName() + bell);
					e.setMessage(newmsg);
					return;

				}
				e.setCancelled(true);
				break;
			}
		}

	}

}
