package me.bingorufus.chatitemdisplaybungee;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import me.bingorufus.chatitemdisplay.utils.VersionComparer;
import me.bingorufus.chatitemdisplay.utils.VersionComparer.Status;
import me.bingorufus.chatitemdisplay.utils.updater.UpdateChecker;
import me.bingorufus.chatitemdisplay.utils.updater.UpdateDownloader;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class ChatItemDisplayBungee extends Plugin {
	@Override
	public void onEnable() {
		downloadUpdate();
		getProxy().registerChannel("chatitemdisplay:out");
		getProxy().registerChannel("chatitemdisplay:in");

		BungeeCord.getInstance().getPluginManager().registerListener(this, new ItemReceiver());
	}

	@Override
	public void onDisable() {
	}

	public void downloadUpdate() {
		BungeeCord.getInstance().getScheduler().runAsync(this, () -> {
			String error = new UpdateChecker(77177).getLatestVersion(ver -> {
				Status s = new VersionComparer().isRecent(this.getDescription().getVersion(), ver);

				if (s.equals(Status.BEHIND)) {
					try {
						UpdateDownloader downloader = new UpdateDownloader(ver);
						String downloadError = downloader
								.download(new FileOutputStream(this.getDataFolder().getParent()));
						if (downloadError != null) {
							getLogger().warning(
									"Could not download the newest version of ChatItemDisplay (" + downloadError + ")");
							return;
						}
						downloader.deletePlugin(this);
						getLogger().info(
								"The newest version of ChatItemDisplay has been downloaded automatically, it will be loaded upon the next startup");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						getLogger().warning(
								"Could not download the newest version of ChatItemDisplay (" + e.getMessage() + ")");
						return;

					}
				}
				getLogger().info("ChatItemDisplay is up to date");
				return;
			});
			if (error != null) {
				getLogger().warning(error);
			}
		});

	}
}
