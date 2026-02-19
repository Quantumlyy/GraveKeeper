package com.quantumlytangled.gravekeeper.foundation.inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class InventoryRestorer {
	
	@Nonnull
	public static List<ItemStack> restoreOrOverflow(@Nonnull final ServerPlayer player,
	                                                @Nonnull final List<InventorySlot> inventorySlots) {
		final List<ItemStack> overflow = new ArrayList<>();
		
		for (final InventorySlot inventorySlot : inventorySlots) {
			restoreOrOverflow(player, inventorySlot, overflow);
		}
		
		return overflow;
	}
	
	private static void restoreOrOverflow(@Nonnull final ServerPlayer player,
	                                      @Nonnull final InventorySlot inventorySlot, @Nonnull final List<ItemStack> overflow) {
		switch (inventorySlot.type) {
		case MAIN -> {
			if (player.getInventory().items.get(inventorySlot.slot).isEmpty()) {
				player.getInventory().items.set(inventorySlot.slot, inventorySlot.itemStack.copy());
			} else {
				overflow.add(inventorySlot.itemStack.copy());
			}
		}
		case ARMOUR -> {
			if (player.getInventory().armor.get(inventorySlot.slot).isEmpty()) {
				player.getInventory().armor.set(inventorySlot.slot, inventorySlot.itemStack.copy());
			} else {
				overflow.add(inventorySlot.itemStack.copy());
			}
		}
		case OFFHAND -> {
			if (player.getInventory().offhand.get(inventorySlot.slot).isEmpty()) {
				player.getInventory().offhand.set(inventorySlot.slot, inventorySlot.itemStack.copy());
			} else {
				overflow.add(inventorySlot.itemStack.copy());
			}
		}
		default -> overflow.add(inventorySlot.itemStack.copy());
		}
	}
}
