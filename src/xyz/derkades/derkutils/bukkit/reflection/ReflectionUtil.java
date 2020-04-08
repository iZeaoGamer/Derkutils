package xyz.derkades.derkutils.bukkit.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReflectionUtil {

	/**
	 *
	 * @param pathToClass Path to a Minecraft class, with %s where the version string would usually be. For example: <i>org.bukkit.craftbukkit.%s.entity.CraftPlayer</i>
	 * @return Class from formatted string
	 * @throws ClassNotFoundException
	 */
	public static Class<?> getMinecraftClass(final String pathToClass) throws ClassNotFoundException {
		final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		return Class.forName(String.format(pathToClass, version));
	}

	/**
	 *
	 * @param player
	 * @return Player ping or -1 if an error occurred
	 */
	public static int getPing(final Player player) {
		try {
			final Object entityPlayer = getMinecraftClass("org.bukkit.craftbukkit.%s.entity.CraftPlayer").getMethod("getHandle").invoke(player);
			final Object ping = getMinecraftClass("net.minecraft.server.%s.EntityPlayer").getField("ping").get(entityPlayer);
			return (int) ping;
		} catch (final ClassNotFoundException | NoSuchMethodException | SecurityException |
				IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Deprecated // broken in 1.15+
	public static ItemStack addCanPlaceOn(final ItemStack item, final String... minecraftItemNames) {
		try {
			final Class<?> craftItemStackClass = getMinecraftClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
			final Object nmsItemStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
			final Class<?> nbtClass = getMinecraftClass("net.minecraft.server.%s.NBTTagCompound");
			Object nbt = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
			if (nbt == null) {
				nbt = nbtClass.getConstructor().newInstance();
			}

			final Object nbtList = getMinecraftClass("net.minecraft.server.%s.NBTTagList").getConstructor().newInstance();
			for (String minecraftItemName : minecraftItemNames) {
				if (!minecraftItemName.contains("minecraft")) {
					minecraftItemName = "minecraft:" + minecraftItemName;
				}

				final Constructor<?> constr = getMinecraftClass("net.minecraft.server.%s.NBTTagString").getConstructor(String.class);
				constr.setAccessible(true);
				final Object nbtString = constr.newInstance(minecraftItemName);
				nbtList.getClass().getMethod("add", Object.class).invoke(nbtList, nbtString);
			}

			nbtClass.getMethod("set", String.class, getMinecraftClass("net.minecraft.server.%s.NBTBase")).invoke(nbt, "CanPlaceOn", nbtList);
			nmsItemStack.getClass().getMethod("setTag", nbtClass).invoke(nmsItemStack, nbt);
			final Object bukkitItemStack = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStack.getClass()).invoke(null, nmsItemStack);
			return (ItemStack) bukkitItemStack;
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			e.printStackTrace();
			return item;
		}
	}

	@Deprecated // broken in 1.15+
	public static ItemStack addCanDestroy(final ItemStack item, final String... minecraftItemNames) {
		try {
			final Class<?> craftItemStackClass = getMinecraftClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
			final Object nmsItemStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
			final Class<?> nbtClass = getMinecraftClass("net.minecraft.server.%s.NBTTagCompound");
			Object nbt = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
			if (nbt == null) {
				nbt = nbtClass.getConstructor().newInstance();
			}
			final Object nbtList = getMinecraftClass("net.minecraft.server.%s.NBTTagList").getConstructor().newInstance();
			for (String minecraftItemName : minecraftItemNames) {
				if (!minecraftItemName.contains("minecraft")) {
					minecraftItemName = "minecraft:" + minecraftItemName;
				}

				final Constructor<?> constr = getMinecraftClass("net.minecraft.server.%s.NBTTagString").getConstructor(String.class);
				constr.setAccessible(true);
				final Object nbtString = constr.newInstance(minecraftItemName);
				nbtList.getClass().getMethod("add", Object.class).invoke(nbtList, nbtString);
			}
			nbtClass.getMethod("set", String.class, getMinecraftClass("net.minecraft.server.%s.NBTBase")).invoke(nbt, "CanDestroy", nbtList);
			nmsItemStack.getClass().getMethod("setTag", nbtClass).invoke(nmsItemStack, nbt);
			final Object bukkitItemStack = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStack.getClass()).invoke(null, nmsItemStack);
			return (ItemStack) bukkitItemStack;
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			e.printStackTrace();
			return item;
		}
	}

	public static CommandMap getCommandMap() {
		try {
			final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			return (CommandMap) field.get(Bukkit.getServer());
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void registerCommand(final String name, final Command command) {
		getCommandMap().register(name, command);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Command> getKnownCommands() {
		try {
			final CommandMap map = getCommandMap();
			return (Map<String, Command>) map.getClass().getMethod("getKnownCommands").invoke(map);
		} catch (final InvocationTargetException | IllegalAccessException | SecurityException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static Command unregisterCommand(final String name) {
		return getKnownCommands().remove(name);
	}

}
