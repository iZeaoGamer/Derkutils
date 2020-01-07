package xyz.derkades.derkutils.bukkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class IconMenu implements Listener {

	private String name;
	private final int size;
	protected final Player player;

	private final Inventory inventory;
	private final InventoryView view;

	/**
	 * Creates a new menu instance.
	 * <ul>
	 * <li>To add items, use the {@link IconMenu#addItem(int, ItemStack)} method</li>
	 * <li>The menu should be opened in the same tick as it's created. Do this using {@link #open()}.
	 * @param plugin Bukkit plugin instance
	 * @param name Name of the menu
	 * @param size Number of slots. Must be a multiple of 9 that is greater than 0 and less than 10.
	 * @param player Player that this menu will be opened for when {@link #open()} is called
	 */
	public IconMenu(final Plugin plugin, final String name, final int rows, final Player player){
		this.size = rows * 9;
		this.name = name;
		this.player = player;

		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.inventory = Bukkit.createInventory(this.player, this.size, this.name);
		this.view = this.player.openInventory(this.inventory);

		new BukkitRunnable() {

			@Override
			public void run() {
				// Some people were getting NPEs, not sure why.
				// now that should happen anymore, hopefully.
				if (IconMenu.this.view == null ||
						IconMenu.this.view.getPlayer() == null ||
						IconMenu.this.view.getPlayer().getOpenInventory() == null ||
						player == null) {
					HandlerList.unregisterAll(IconMenu.this);
					this.cancel();
					return;
				}

				// Unregister listeners for the menu if the player has opened a different
				// menu, which means that this menu must be closed.
				if (!IconMenu.this.view.getPlayer().getOpenInventory().equals(IconMenu.this.view)) {
					IconMenu.this.view.close(); // maybe not necessary
					HandlerList.unregisterAll(IconMenu.this);
					IconMenu.this.onClose(new MenuCloseEvent(player, CloseReason.PLAYER_CLOSED));
					this.cancel();
					return;
				}
			}

		}.runTaskTimer(plugin, 1, 1);
	}

	/**
	 * Called when a player clicks on an item in the menu
	 * @param event
	 * @return Whether the menu should be closed
	 */
	public abstract boolean onOptionClick(OptionClickEvent event);

	/**
	 * Called when the menu closes
	 * @param event
	 */
	public void onClose(final MenuCloseEvent event) {}

	/**
	 * Sets the name of the menu. Has no effect after the menu has been opened.
	 * @param name
	 */
	public void setName(final String name){
		this.name = name;
	}

	public int getSize() {
		return this.size;
	}

	/**
	 * Calls {@link #onClose(MenuCloseEvent)} with {@link CloseReason#FORCE_CLOSE} and closes the inventory.
	 */
	public void close() {
		this.onClose(new MenuCloseEvent(this.player, CloseReason.FORCE_CLOSE));
		this.view.close();
	}

	public void addItem(final int slot, final ItemStack item) {
		this.inventory.setItem(slot, item);
	}

	public boolean hasItem(final int slot) {
		return this.inventory.getItem(slot) != null &&
				this.inventory.getItem(slot).getType() != Material.AIR;
	}

	public void clear() {
		this.inventory.clear();
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public InventoryView getInventoryView() {
		return this.view;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(final InventoryClickEvent event) {
		if (event.getView().equals(this.view)) {
			event.setCancelled(true);

			if (event.getClick() != ClickType.LEFT) {
				return;
			}

			final int slot = event.getRawSlot();

			final Player clicker = (Player) event.getWhoClicked();

			if (slot >= 0 && slot < this.size && this.hasItem(slot)) {
				final ItemStack item = this.inventory.getItem(slot);

				final boolean close = this.onOptionClick(new OptionClickEvent(clicker, slot, item));
				if (close) {
					IconMenu.this.onClose(new MenuCloseEvent(IconMenu.this.player, CloseReason.ITEM_CLICK));
					this.view.close();
				}
			}
		}
	}

}