package com.quantumlytangled.gravekeeper.foundation.inventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.quantumlytangled.gravekeeper.compat.ICompatInventory;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InventoryCollector {
	
	private static final LinkedHashMap<InventoryType, ICompatInventory> compatInventories = new LinkedHashMap<>(10);
	
	public static void addCompatibilityWrapper(final ICompatInventory compatInventory) {
		compatInventories.put(compatInventory.getType(), compatInventory);
	}
	
	public static List<InventorySlot> collectOnDeath(final ServerPlayer player) {
		final List<InventorySlot> inventorySlots = new ArrayList<>();
		
		for (final ICompatInventory compatInventory : compatInventories.values()) {
			collectOnDeath(player, inventorySlots, compatInventory);
		}
		
		return inventorySlots;
	}
	
	private static void collectOnDeath(final ServerPlayer player,
	                                   final List<InventorySlot> inventorySlots,
	                                   final ICompatInventory compatInventory) {
		final NonNullList<ItemStack> items = compatInventory.getAllContents(player);
		for (int i = 0; i < items.size(); i++) {
			final ItemStack stack = items.get(i);
			if (stack.isEmpty()) continue;
			inventorySlots.add(new InventorySlot(stack, i, compatInventory.getType()));
			compatInventory.removeItem(player, i);
		}
	}
}
