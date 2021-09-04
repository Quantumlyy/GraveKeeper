package dev.quantumly.gravekeeper.compatibility;

import dev.quantumly.gravekeeper.util.InventoryType;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface ICompatInventory {
	
	InventoryType getType();
	
	NonNullList<ItemStack> getAllContents(@Nonnull final ServerPlayerEntity player);
	
	void removeItem(@Nonnull final ServerPlayerEntity player, final int slot);
	
	ItemStack setItemReturnOverflow(@Nonnull final ServerPlayerEntity player, final int slot, @Nonnull final ItemStack item);
}
