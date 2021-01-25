package com.quantumlytangled.gravekeeper.compatibility;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import com.quantumlytangled.gravekeeper.util.InventoryType;

public interface ICompatInventory {
	
	InventoryType getType();
	
	NonNullList<ItemStack> getAllContents(@Nonnull final ServerPlayerEntity player);
	
	void removeItem(@Nonnull final ServerPlayerEntity player, final int slot);
	
	ItemStack setItemReturnOverflow(@Nonnull final ServerPlayerEntity player, final int slot, @Nonnull final ItemStack item);
}
