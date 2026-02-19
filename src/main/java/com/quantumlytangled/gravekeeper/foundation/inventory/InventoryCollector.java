package com.quantumlytangled.gravekeeper.foundation.inventory;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InventoryCollector {
	public static List<InventorySlot> collectOnDeath(final ServerPlayer player) {
		final List<InventorySlot> inventorySlots = new ArrayList<>();
		
		// main inventory (0-35)
		for (int i = 0; i < player.getInventory().items.size(); i++) {
			final ItemStack stack = player.getInventory().items.get(i);
			if (stack.isEmpty()) continue;
			inventorySlots.add(new InventorySlot(stack, i, InventoryType.MAIN));
			player.getInventory().items.set(i, ItemStack.EMPTY);
		}
		
		// armor slots
		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			final ItemStack stack = player.getInventory().armor.get(i);
			if (stack.isEmpty()) continue;
			inventorySlots.add(new InventorySlot(stack, i, InventoryType.ARMOUR));
			player.getInventory().armor.set(i, ItemStack.EMPTY);
		}
		
		// offhand slot
		for (int i = 0; i < player.getInventory().offhand.size(); i++) {
			final ItemStack stack = player.getInventory().offhand.get(i);
			if (stack.isEmpty()) continue;
			inventorySlots.add(new InventorySlot(stack, i, InventoryType.OFFHAND));
			player.getInventory().offhand.set(i, ItemStack.EMPTY);
		}
		
		return inventorySlots;
	}
}
