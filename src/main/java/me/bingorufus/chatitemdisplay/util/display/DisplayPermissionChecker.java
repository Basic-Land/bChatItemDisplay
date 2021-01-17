package me.bingorufus.chatitemdisplay.util.display;

import me.bingorufus.chatitemdisplay.ChatItemDisplay;
import me.bingorufus.chatitemdisplay.util.ChatItemConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DisplayPermissionChecker {
    final ChatItemDisplay m;
    final Player p;

    public DisplayPermissionChecker(ChatItemDisplay chatItemDisplay, Player p) {
        this.m = chatItemDisplay;
        this.p = p;


    }

    public DisplayReason displayItem() {
        boolean debug = ChatItemConfig.DEBUG_MODE;
        ItemStack i = p.getInventory().getItemInMainHand().clone();
        if (i.getItemMeta() == null) {
            if (debug)
                Bukkit.getLogger().info(p.getName() + "'s item has no meta data");
            return DisplayReason.NULL_ITEM;
        }
        if (!p.hasPermission("chatitemdisplay.command.display.item")) {
            if (debug)
                Bukkit.getLogger().info(p.getName() + " does not have permission to display items");

            return DisplayReason.NO_PERMISSON;
        }
        if (m.getConfig().getStringList("blacklisted-items").contains(i.getType().getKey().toString())) {
            if (!p.hasPermission("Chatitemdisplay.blacklistbypass")) {
                if (debug)
                    Bukkit.getLogger().info(p.getName() + "'s displayed item was blacklisted");
                return DisplayReason.BLACKLISTED;
            }
        }
        if (isOnCooldown()) {
            if (debug)
                Bukkit.getLogger().info(p.getName() + " is on a chat display cooldown");
            return DisplayReason.COOLDOWN;
        }
        return DisplayReason.DISPLAY;
    }


    public boolean isOnCooldown() {
        return ChatItemDisplay.getInstance().getDisplayCooldown().isOnCooldown(p) && !p.hasPermission("chatitemdisplay.cooldownbypass");

    }

    public enum DisplayReason {
        BLACKLISTED, COOLDOWN, NULL_ITEM, NO_PERMISSON, DISPLAY
    }

}
