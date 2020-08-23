package me.bingorufus.chatitemdisplay.utils.iteminfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import net.md_5.bungee.api.chat.TranslatableComponent;

public class ItemStackTranslator {

	private Class<?> craftPotionUtil;
	private Class<?> craftItemStack;
	private Class<?> mojangsonParser;
	private Class<?> nmsItemStack;

	
	public ItemStackTranslator() {
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			craftPotionUtil = Class
					.forName("org.bukkit.craftbukkit.{v}.potion.CraftPotionUtil".replace("{v}", version));

			craftItemStack = Class
					.forName("org.bukkit.craftbukkit.{v}.inventory.CraftItemStack".replace("{v}", version));

			mojangsonParser = Class
					.forName("net.minecraft.server.{v}.MojangsonParser".replace("{v}", version));

			nmsItemStack = Class.forName("net.minecraft.server.{v}.ItemStack".replace("{v}", version));


		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public Object nmsItem(ItemStack item) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

			Method asNms = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
			asNms.setAccessible(true);
			Object nmsItem = asNms.invoke(craftItemStack, item);
		return nmsItem;

	}





	public String getNBT(ItemStack item) {
		try {
			Object nmsItem = nmsItem(item);
			if (nmsItem == null) {
				throw new IllegalArgumentException(item.getType().name() + " could not be queried!");
			}
			Method hasTag = nmsItem.getClass().getMethod("hasTag");

			if ((boolean) hasTag.invoke(nmsItem)) {
				Method getTag = nmsItem.getClass().getMethod("getTag");
				Object nbtData = getTag.invoke(nmsItem);
				Method asString = nbtData.getClass().getMethod("asString");
				return (String) asString.invoke(nbtData);
			}

		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException | IllegalAccessException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return "{}";

	}

	public ItemStack fromNBT(ItemStack baseItem, String NBTData) {
		try {

			Object nmsItem = nmsItem(baseItem);

			Method parseNBT = mojangsonParser.getMethod("parse", String.class);
			Object nbtCompound = parseNBT.invoke(mojangsonParser, NBTData);

			Method setTag = nmsItemStack.getMethod("setTag", nbtCompound.getClass());
			setTag.invoke(nmsItem, nbtCompound);

			Method asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsItemStack);
			ItemStack editedItem = (ItemStack) asBukkitCopy.invoke(craftItemStack, nmsItem);
			return editedItem;

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return new ItemStack(Material.AIR);

	}

	public String getId(ItemStack holding) {
		try {
			Object item = nmsItem(holding);
			if (item == null) {
				throw new IllegalArgumentException(holding.getType().name() + " could not be queried!");
			}
			Method getItem = item.getClass().getMethod("getItem");
			getItem.setAccessible(true);
			Object newItem = getItem.invoke(item);
			Method getName = newItem.getClass().getMethod("getName");
			getName.setAccessible(true);
			String key = (String) getName.invoke(newItem);
			if (holding.getItemMeta() instanceof PotionMeta) {
				PotionMeta pm = (PotionMeta) holding.getItemMeta();

				key += ".effect.";
				Method fromBukkit = craftPotionUtil.getMethod("fromBukkit", PotionData.class);
				fromBukkit.setAccessible(true);
				String potionkey = (String) fromBukkit.invoke(craftPotionUtil,
						new PotionData(pm.getBasePotionData().getType()));
				potionkey = potionkey.replaceAll("minecraft:", "");
				key += potionkey;

			}

			return key;

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;

	}


	public TranslatableComponent translateItemStack(ItemStack holding) {
		return new TranslatableComponent(getId(holding));
	}

	public String potionId(PotionType pt) {
		try {
		Method fromBukkit = craftPotionUtil.getMethod("fromBukkit", PotionData.class);
		fromBukkit.setAccessible(true);


			String potionkey = (String) fromBukkit.invoke(craftPotionUtil, new PotionData(pt));
			return potionkey;

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return "";

		}

	}

}
