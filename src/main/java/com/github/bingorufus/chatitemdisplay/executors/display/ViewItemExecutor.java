package com.github.bingorufus.chatitemdisplay.executors.display;

import com.github.bingorufus.chatitemdisplay.ChatItemDisplay;
import com.github.bingorufus.chatitemdisplay.Display;
import com.github.bingorufus.chatitemdisplay.util.ChatItemConfig;
import com.github.bingorufus.chatitemdisplay.util.string.StringFormatter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ViewItemExecutor implements CommandExecutor {
    final ChatItemDisplay m = ChatItemDisplay.getInstance();


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (m.getConfig().getBoolean("disable-gui")) {
            sender.sendMessage(new StringFormatter().format(ChatItemConfig.FEATURE_DISABLED));
            return true;

        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command");
            return true;
        }

        if (args.length == 0)
            return false;

        Player p = (Player) sender;
        String target = args[0];
        UUID id = null;
        boolean usePlayer = false;
        boolean invalidPlayer;
        if (Bukkit.getPlayer(args[0]) != null) {
            target = Bukkit.getPlayer(args[0]).getName();
            usePlayer = true;
        }
        invalidPlayer = m.getDisplayedManager().getMostRecent(target.toUpperCase()) == null;

        if (invalidPlayer && usePlayer) {
            sender.sendMessage(
                    new StringFormatter().format(ChatItemConfig.EMPTY_DISPLAY));
            return true;
        }

        if (invalidPlayer) {
            try {
                id = UUID.fromString(args[0]);

            } catch (IllegalArgumentException e) {
                sender.sendMessage(new StringFormatter()
                        .format(ChatItemConfig.EMPTY_DISPLAY));
                return true;
            }
            if (m.getDisplayedManager().getDisplayed(id) == null) {
                sender.sendMessage(new StringFormatter().format(ChatItemConfig.INVALID_ID));
                return true;
            }

        }


        Display dis = id == null ? m.getDisplayedManager().getMostRecent(target.toUpperCase()) : m.getDisplayedManager().getDisplayed(id);

        if (dis == null) {
            return false;
        }
        Inventory inv = dis.getDisplayable().onViewDisplay(p);
        if (inv != null) {
            ChatItemDisplay.getInstance().getChatItemDisplayInventories().put(inv, null);
            p.openInventory(inv);
        }
        return true;
    }


}
