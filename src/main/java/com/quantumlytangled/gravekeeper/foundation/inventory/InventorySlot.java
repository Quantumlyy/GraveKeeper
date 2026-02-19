package com.quantumlytangled.gravekeeper.foundation.inventory;

import javax.annotation.Nonnull;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class InventorySlot {
	
	public final ItemStack itemStack;
	public final int slot;
	public final InventoryType type;
	
	public InventorySlot(@Nonnull ItemStack itemStack, int slot, @Nonnull InventoryType type) {
		this.itemStack = itemStack.copy();
		this.slot = slot;
		this.type = type;
	}
	
	public InventorySlot(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
		this.itemStack = ItemStack.parseOptional(registries, tag.getCompound("item"));
		this.slot = tag.getInt("slot");
		this.type = InventoryType.valueOf(tag.getString("type"));
	}
	
	@Nonnull
	public CompoundTag writeToNBT(@Nonnull HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putString("type", type.name());
		tag.putInt("slot", slot);
		tag.put("item", itemStack.save(registries));
		return tag;
	}
}
