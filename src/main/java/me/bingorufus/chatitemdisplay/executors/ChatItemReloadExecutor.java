package me.bingorufus.chatitemdisplay.executors;

import me.bingorufus.chatitemdisplay.util.ChatItemConfig;
import me.bingorufus.chatitemdisplay.util.display.ConfigReloader;
import me.bingorufus.chatitemdisplay.util.string.StringFormatter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class ChatItemReloadExecutor implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("chatitemreload")) {
            if (sender.hasPermission("ChatItemDisplay.command.reload") || sender instanceof ConsoleCommandSender) {
                new ConfigReloader().reload();
                sender.sendMessage(ChatColor.GREEN + "ChatItemDisplay Reloaded");
                return true;
            }


            sender.sendMessage(new StringFormatter().format(
                    ChatItemConfig.MISSING_PERMISSION_GENERIC));
            return true;
        }
        return false;
    }
}
