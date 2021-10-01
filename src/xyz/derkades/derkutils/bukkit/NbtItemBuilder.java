package xyz.derkades.derkutils.bukkit;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;

public class NbtItemBuilder extends AbstractItemBuilder<NbtItemBuilder> {

	public NbtItemBuilder(@NotNull Material material) {
		super(material);
	}
	
	public NbtItemBuilder(@NotNull ItemStack item) {
		super(item);
	}
	
	@Override
	@NotNull
	public NbtItemBuilder getInstance() {
		return this;
	}
	
	@NotNull
	public NbtItemBuilder canDestroy(@NotNull final String... vanillaNamespacedNames) {
		Objects.requireNonNull(vanillaNamespacedNames, "names varargs is null");
		final NBTItem nbt = new NBTItem(this.item);
		nbt.getStringList("CanDestroy").addAll(List.of(vanillaNamespacedNames));
		this.item = nbt.getItem();
		return this;
	}
	
	@NotNull
	public NbtItemBuilder canPlaceOn(@NotNull final String... vanillaNamespacedNames) {
		Objects.requireNonNull(vanillaNamespacedNames, "names varargs is null");
		final NBTItem nbt = new NBTItem(this.item);
		nbt.getStringList("CanPlaceOn").addAll(List.of(vanillaNamespacedNames));
		this.item = nbt.getItem();
		return this;
	}
	
	@NotNull
	public NbtItemBuilder skullTexture(@NotNull final String texture) {
		Objects.requireNonNull(texture, "Texture string is null");
		final NBTItem nbt = new NBTItem(this.item);
		final NBTCompound skullOwner = nbt.addCompound("SkullOwner");
		skullOwner.setString("Id", UUID.randomUUID().toString());
		skullOwner.addCompound("Properties").getCompoundList("textures").addCompound().setString("Value", texture);
		this.item = nbt.getItem();
		return this;
	}
	
	@NotNull
	public NbtItemBuilder hideFlags(final int hideFlags) {
		final NBTItem nbt = new NBTItem(this.item);
		nbt.setInteger("HideFlags", hideFlags);
		this.item = nbt.getItem();
		return this;
	}

}
