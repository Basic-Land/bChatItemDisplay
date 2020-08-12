package me.bingorufus.chatitemdisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.bingorufus.chatitemdisplay.Utils.loaders.Metrics;
import me.bingorufus.chatitemdisplay.Utils.loaders.ProtocolLibRegister;
import me.bingorufus.chatitemdisplay.Utils.updater.UpdateChecker;
import me.bingorufus.chatitemdisplay.Utils.updater.UpdateDownloader;
import me.bingorufus.chatitemdisplay.executors.ChatItemReloadExecutor;
import me.bingorufus.chatitemdisplay.executors.DisplayCommandExecutor;
import me.bingorufus.chatitemdisplay.executors.ViewItemExecutor;
import me.bingorufus.chatitemdisplay.listeners.ChatDisplayListener;
import me.bingorufus.chatitemdisplay.listeners.MapViewerListener;
import me.bingorufus.chatitemdisplay.listeners.NewVersionDisplayer;

public class ChatItemDisplay extends JavaPlugin {
	ChatDisplayListener DisplayListener;
	NewVersionDisplayer NewVer;

	ProtocolLibRegister pl;

	public HashMap<String, Inventory> displaying = new HashMap<String, Inventory>();
	public HashMap<String, Display> displays = new HashMap<String, Display>();
	public HashMap<Player, ItemStack> viewingMap = new HashMap<Player, ItemStack>();

	public List<Inventory> invs = new ArrayList<Inventory>();

	public boolean hasProtocollib = false;
	public Boolean useOldFormat = false;

	/*
	 * TODO: a /version command that shows java version server cversion etc auto
	 * update config
	 */
	@Override
	public void onEnable() {


		this.saveDefaultConfig();
		reloadConfigVars();
		this.getCommand("viewitem").setExecutor(new ViewItemExecutor(this));
		this.getCommand("chatitemreload").setExecutor(new ChatItemReloadExecutor(this));
		Metrics metrics = new Metrics(this, 7229);
		this.getCommand("displayitem").setExecutor(new DisplayCommandExecutor(this));
		metrics.addCustomChart(new Metrics.SimplePie("old_display_messages", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return getConfig().getString("use-old-format");
			}
		}));

	}

	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (viewingMap.containsKey(p)) {
				p.getInventory().setItemInMainHand(viewingMap.get(p));
			}
			if (invs.contains(p.getOpenInventory().getTopInventory())) {
				p.closeInventory();
			}

		}
	}

	public void reloadConfigVars() {


		this.saveDefaultConfig();
		this.reloadConfig();
		Bukkit.getPluginManager().registerEvents(new MapViewerListener(this), this);

		useOldFormat = this.getConfig().getBoolean("use-old-format")
				|| Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") == null;
		if (!useOldFormat) {
			pl = new ProtocolLibRegister(this);
			pl.registerPacketListener();
			hasProtocollib = true;
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "" + ChatColor.BOLD
					+ "[ChatItemDisplay] In Chat Item Displaying has Been Disabled Because This Server Does Not Have ProtocolLib");
			hasProtocollib = false;
		}
		if (DisplayListener != null)
			HandlerList.unregisterAll(DisplayListener);
		if (NewVer != null)
			HandlerList.unregisterAll(NewVer);
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (invs.contains(p.getOpenInventory().getTopInventory())) {
				p.closeInventory();
			}

		}
		if (!getConfig().getBoolean("disable-update-checking")) {
			new UpdateChecker(this, 77177).getLatestVersion(version -> {

				if (UpToDate(this.getDescription().getVersion().split("[.]"), version.split("[.]"))) {
					this.getLogger().info("ChatItemDisplay is up to date");
				} else {

					this.getLogger().warning("ChatItemDisplay is currently running version "
							+ getDescription().getVersion() + " and can be updated to " + version);
					if (getConfig().getBoolean("auto-update")) {
						new UpdateDownloader(this, version).download();
						this.getLogger().info("The download process has begun automatically");
						return;

					}
					this.getLogger().warning(
							"Download the newest version at: //https://www.spigotmc.org/resources/chat-item-display.77177/");
					this.getLogger().warning("or enable auto-update in your config.yml");

					NewVer = new NewVersionDisplayer(this, this.getDescription().getVersion(), version);
					Bukkit.getPluginManager().registerEvents(NewVer, this);
				}
			});
		}

		DisplayListener = new ChatDisplayListener(this);
		Bukkit.getPluginManager().registerEvents(DisplayListener, this);
	}

	public Boolean UpToDate(String cur[], String upd[]) {
		Integer[] CurrentVer = new Integer[3];
		Integer[] UpdateVer = new Integer[3];
		int lengthtouse = 0;
		if (cur.length < upd.length)
			lengthtouse = cur.length;
		if (cur.length > upd.length)
			lengthtouse = cur.length;
		if (cur.length == upd.length)
			lengthtouse = cur.length;
		for (int i = 0; i < lengthtouse; i++) {
			CurrentVer[i] = Integer.parseInt(cur[i]);
			UpdateVer[i] = Integer.parseInt(upd[i]);
		}
		if (CurrentVer.equals(UpdateVer)) {
			if (CurrentVer.length < UpdateVer.length)
				return false;
			return true;
		}

		if (CurrentVer[0] < UpdateVer[0])
			return false;
		if (CurrentVer[0] > UpdateVer[0])
			return true;
		// CurrentVer[0] has to be equal to UpdateVer[0]
		if (CurrentVer[1] < UpdateVer[1])
			return false;
		if (CurrentVer[1] > UpdateVer[1])
			return true;
		// Second number is now equal
		if (CurrentVer[2] >= UpdateVer[2])
			return true;

		return false;
	}

}
