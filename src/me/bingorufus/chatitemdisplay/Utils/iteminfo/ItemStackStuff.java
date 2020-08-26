package me.bingorufus.chatitemdisplay.utils.iteminfo;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class ItemStackStuff {
	public ItemStack deserialize(String serialized) {

		return new ItemStack(Material.AIR);
	}

	public String serialize(ItemStack item) {
		return "";
	}

	public String makeStringPretty(String s) {
		switch (s) {
		default:
			String out = null;
			String[] Nameparts = s.toLowerCase().split("_");
			for (String part : Nameparts) {
				part = part.substring(0, 1).toUpperCase() + part.substring(1);
				if (out == null) {
					out = part;
					continue;
				}

				out += " ";
				out += part;
			}

			return out;
		}
	}

	public String ItemName(ItemStack item) {

		String out = "§r";
		if (item.getType().equals(Material.DRAGON_EGG) || item.getType().equals(Material.ENCHANTED_GOLDEN_APPLE))
			out = "§d";

		if (item.getItemMeta().hasEnchants())
			out = ChatColor.AQUA + "";

		if (item.getItemMeta().hasDisplayName()) {
			out += item.getItemMeta().getDisplayName();
			return out;

		}
		return out + makeStringPretty(item.getType().name()) + ChatColor.RESET;

	}

	public TextComponent NameFromItem(ItemStack item) {

		String out = "§r";
		if (item.getType().equals(Material.DRAGON_EGG) || item.getType().equals(Material.ENCHANTED_GOLDEN_APPLE)
				|| item.getType().equals(Material.COMMAND_BLOCK))
			out = "§d";

		if (item.getItemMeta().hasEnchants() || item.getType().isRecord())
			out = ChatColor.AQUA + "";

		if (item.getItemMeta().hasDisplayName()) {
			out += ChatColor.ITALIC;
			out += item.getItemMeta().getDisplayName();
			return new TextComponent(out);

		}
		if (item.getItemMeta() instanceof SkullMeta) {
			SkullMeta sm = (SkullMeta) item.getItemMeta();
			if (sm.hasOwner()) {
			TranslatableComponent translatable = new TranslatableComponent("block.minecraft.player_head.named");
				translatable.addWith(new TextComponent(sm.getOwningPlayer().getName()));

			return new TextComponent(new TextComponent("§e"), translatable);
			}
		}
		if (item.getType().equals(Material.WRITTEN_BOOK)) {

			BookMeta book = (BookMeta) item.getItemMeta();
			if (book.hasTitle()) {
				return new TextComponent(book.getTitle());
			}

		}
		return new TextComponent(new TextComponent(out), new ItemStackTranslator().translateItemStack(item));

	}




}
