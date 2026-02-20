package com.quantumlytangled.gravekeeper.foundation.inventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.quantumlytangled.gravekeeper.compat.CompatMain;
import com.quantumlytangled.gravekeeper.compat.ICompatInventory;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InventoryRestorer {
	
	private static final LinkedHashMap<InventoryType, ICompatInventory> compatInventories = new LinkedHashMap<>(10);
	
	public static void addCompatibilityWrapper(final ICompatInventory compatInventory) {
		compatInventories.put(compatInventory.getType(), compatInventory);
	}
	
	public static List<ItemStack> restoreOrOverflow(final ServerPlayer player,
	                                                final List<InventorySlot> inventorySlots) {
		final List<ItemStack> overflow = new ArrayList<>();
		
		for (final InventorySlot slot : inventorySlots) {
			restoreOrOverflow(player, slot, overflow);
		}
		
		return overflow;
	}
	
	private static void restoreOrOverflow(final ServerPlayer player,
	                                      final InventorySlot slot, final List<ItemStack> overflow) {
		final ICompatInventory compat = compatInventories.getOrDefault(slot.type, CompatMain.getInstance());
		final ItemStack left = compat.setItemReturnOverflow(player, slot.slot, slot.itemStack);
		if (!left.isEmpty()) {
			overflow.add(left);
		}
	}
}
