package com.quantumlytangled.gravekeeper.compat;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.quantumlytangled.gravekeeper.foundation.inventory.InventoryType;

@ParametersAreNonnullByDefault
public interface ICompatInventory {
	
	InventoryType getType();
	
	NonNullList<ItemStack> getAllContents(final ServerPlayer player);
	
	void removeItem(final ServerPlayer player, final int slot);
	
	ItemStack setItemReturnOverflow(final ServerPlayer player, final int slot, final ItemStack item);
}
