package com.quantumlytangled.gravekeeper.content.grave;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.foundation.inventory.InventorySlot;
import org.jetbrains.annotations.NotNull;

public class GraveBlockEntity extends BlockEntity {
	
	private List<InventorySlot> inventorySlots = new ArrayList<>();
	
	public GraveBlockEntity(BlockPos pos, BlockState state) {
		super(GraveKeeper.GRAVE_BLOCK_ENTITY.get(), pos, state);
	}
	
	public List<InventorySlot> getInventorySlots() {
		return inventorySlots;
	}
	
	public void setInventorySlots(@Nonnull List<InventorySlot> slots) {
		this.inventorySlots = new ArrayList<>(slots);
		setChanged();
	}
	
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		super.saveAdditional(tag, registries);
		ListTag slotsTag = new ListTag();
		for (InventorySlot slot : inventorySlots) {
			slotsTag.add(slot.writeToNBT(registries));
		}
		tag.put("InventorySlots", slotsTag);
	}
	
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(tag, registries);
		inventorySlots.clear();
		ListTag slotsTag = tag.getList("InventorySlots", Tag.TAG_COMPOUND);
		for (int i = 0; i < slotsTag.size(); i++) {
			inventorySlots.add(new InventorySlot(slotsTag.getCompound(i), registries));
		}
	}
}
