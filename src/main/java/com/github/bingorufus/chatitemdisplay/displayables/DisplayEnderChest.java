package com.github.bingorufus.chatitemdisplay.displayables;

import com.github.bingorufus.chatitemdisplay.ChatItemDisplay;
import com.github.bingorufus.chatitemdisplay.api.display.DisplayType;
import com.github.bingorufus.chatitemdisplay.api.display.Displayable;
import com.github.bingorufus.chatitemdisplay.util.ChatItemConfig;
import com.github.bingorufus.chatitemdisplay.util.iteminfo.InventoryData;
import com.github.bingorufus.chatitemdisplay.util.iteminfo.InventorySerializer;
import com.github.bingorufus.chatitemdisplay.util.string.StringFormatter;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class DisplayEnderChest extends Displayable {
    protected Inventory inventory;
    protected String inventoryTitle;

    public DisplayEnderChest(Player player) {
        super(player);
        inventoryTitle = new StringFormatter()
                .format(ChatItemConfig.ENDERCHEST_TITLE.replace("%player%",
                        ChatItemDisplay.getInstance().getConfig().getBoolean("use-nicks-in-gui") ? ChatItemDisplay.getInstance().getConfig().getBoolean("strip-nick-colors-gui")
                                ? ChatColor.stripColor(getDisplayer().getDisplayName())
                                : getDisplayer().getDisplayName() : getDisplayer().getRegularName()));
        inventory = Bukkit.createInventory(player, InventoryType.ENDER_CHEST, inventoryTitle);
        inventory.setContents(player.getEnderChest().getContents().clone());
    }

    public DisplayEnderChest(JsonObject data) {
        super(data);
    }

    @Override
    protected Class<? extends DisplayType> getTypeClass() {
        return DisplayEnderChestType.class;
    }

    @Override
    public BaseComponent getDisplayComponent() {
        String format = new StringFormatter()
                .format(ChatItemConfig.CHAT_INVENTORY_FORMAT)
                .replaceAll("%player%", ChatItemDisplay.getInstance().getConfig().getBoolean("use-nicks-in-display-message")
                        ? ChatItemDisplay.getInstance().getConfig().getBoolean("strip-nick-colors-message")
                        ? ChatColor.stripColor(getDisplayer().getDisplayName())
                        : getDisplayer().getDisplayName()
                        : getDisplayer().getRegularName());
        return format(format);
    }

    private TextComponent format(String format) {
        String[] parts = format.split("((?<=%type%)|(?=%type%))");
        TextComponent whole = new TextComponent();
        BaseComponent prev = null;
        for (int i = 0; i < parts.length; i++) {
            if (i > 0)
                prev = new TextComponent(TextComponent.fromLegacyText(whole.getExtra().get(i - 1).toLegacyText()));
            String part = parts[i];
            if (part.equalsIgnoreCase("%type%")) {
                TranslatableComponent type = new TranslatableComponent("container.enderchest");
                if (i > 0) {
                    type.copyFormatting(prev, ComponentBuilder.FormatRetention.FORMATTING, false);
                }
                whole.addExtra(type);
                continue;
            }

            TextComponent tc = new TextComponent(TextComponent.fromLegacyText(part));
            if (i > 0 && !part.startsWith("§r"))
                tc.copyFormatting(prev, ComponentBuilder.FormatRetention.FORMATTING, false);

            whole.addExtra(tc);
        }
        UUID id = ChatItemDisplay.getInstance().getDisplayedManager().getDisplay(this).getId();
        whole.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewitem " + id.toString()));

        return whole;
    }

    @Override
    public Inventory onViewDisplay(Player viewer) {
        return InventorySerializer.cloneInventory(this.inventory, this.inventoryTitle);
    }

    @Override
    public String getLoggerMessage() {

        String format = ChatItemConfig.CHAT_INVENTORY_FORMAT
                .replaceAll("%player%", ChatItemDisplay.getInstance().getConfig().getBoolean("use-nicks-in-display-message")
                        ? ChatItemDisplay.getInstance().getConfig().getBoolean("strip-nick-colors-message")
                        ? ChatColor.stripColor(getDisplayer().getDisplayName())
                        : getDisplayer().getDisplayName()
                        : getDisplayer().getRegularName());
        format = format.replaceAll("%type%", ChatItemDisplay.getInstance().getLang().get("container.enderchest").getAsString());

        return ChatColor.stripColor(new StringFormatter().format(format));
    }

    @Override
    protected JsonObject serializeData() {
        JsonObject jo = new JsonObject();
        InventorySerializer inventorySerializer = new InventorySerializer();
        jo.addProperty("data", inventorySerializer.serialize(inventory, inventoryTitle));
        return jo;
    }

    @Override
    protected void deseralizeData(JsonObject data) {
        InventoryData inventoryData = new InventorySerializer().deserialize(data.get("data").getAsString());
        inventory = inventoryData.getInventory();
        inventoryTitle = inventoryData.getTitle();
    }

    @Override
    public void broadcastDisplayable() {
        String format = new StringFormatter()
                .format(ChatItemConfig.COMMAND_INVENTORY_FORMAT)
                .replaceAll("%player%",
                        ChatItemDisplay.getInstance().getConfig().getBoolean("use-nicks-in-display-message")
                                ? ChatItemDisplay.getInstance().getConfig().getBoolean("strip-nick-colors-message")
                                ? ChatColor.stripColor(getDisplayer().getDisplayName())
                                : getDisplayer().getDisplayName()
                                : getDisplayer().getRegularName());

        Bukkit.spigot().broadcast(format(format));
    }

    @Override
    public boolean hasBlacklistedItem() {
        return containsBlacklistedItem(inventory);
    }
}
