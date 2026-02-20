package com.quantumlytangled.gravekeeper.compat;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.quantumlytangled.gravekeeper.foundation.inventory.InventoryType;

@ParametersAreNonnullByDefault
public class CompatMain implements ICompatInventory {
	
	private static final CompatMain INSTANCE = new CompatMain();
	
	public static CompatMain getInstance() {
		return INSTANCE;
	}
	
	@Override
	public InventoryType getType() {
		return InventoryType.MAIN;
	}
	
	@Override
	public NonNullList<ItemStack> getAllContents(final ServerPlayer player) {
		return player.getInventory().items;
	}
	
	@Override
	public void removeItem(final ServerPlayer player, final int slot) {
		player.getInventory().items.set(slot, ItemStack.EMPTY);
	}
	
	@Override
	public ItemStack setItemReturnOverflow(final ServerPlayer player, final int slot, final ItemStack itemStack) {
		if (slot >= 0
		    && slot < player.getInventory().items.size()
		    && player.getInventory().items.get(slot).isEmpty()) {
			player.getInventory().items.set(slot, itemStack);
			return ItemStack.EMPTY;
		}
		return itemStack;
	}
}
