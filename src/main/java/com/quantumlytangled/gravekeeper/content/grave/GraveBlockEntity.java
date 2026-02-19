package com.quantumlytangled.gravekeeper.content.grave;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.quantumlytangled.gravekeeper.Config;
import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.foundation.CreationDate;
import com.quantumlytangled.gravekeeper.foundation.inventory.InventoryRestorer;
import com.quantumlytangled.gravekeeper.foundation.inventory.InventorySlot;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

public class GraveBlockEntity extends BlockEntity {
	
	private final String ownerName = "";
	private final UUID ownerUUID = null;
	
	private final long creationDate = 0;
	
	private List<InventorySlot> inventorySlots = new ArrayList<>();
	
	public GraveBlockEntity(BlockPos pos, BlockState state) {
		super(GraveKeeper.GRAVE_BLOCK_ENTITY.get(), pos, state);
	}
	
	public void processInteraction(@Nonnull final ServerPlayer player) {
		final boolean isCreative = player.isCreative();
		final boolean isOwner = ownerUUID == null
		                        || (ownerUUID.getLeastSignificantBits() == 0L && ownerUUID.getMostSignificantBits() == 0L)
		                        || player.getUUID().equals(ownerUUID);
		final long timeRemaining = CreationDate.getRemainingSeconds(creationDate);
		
		if (isCreative || player.isCrouching()) {
			doInspection(player, isCreative, isOwner, timeRemaining);
			
		} else if (isOwner) {
			doReturnToOwner(player);
			
		} else if ((Config.isInstantForeignCollection() || timeRemaining <= 0L)
		           && !Config.isOwnerOnlyCollection()) {
			GraveKeeper.LOGGER.warn("Death chest dropping content at {} {} {} {} by player {}",
			                        level != null ? level.dimension().location() : "null",
			                        getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), player);
			doDropContent();
			
		} else {
			doInspection(player, false, false, timeRemaining);
		}
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
	
	private void doInspection(@Nonnull final ServerPlayer player, final boolean isCreative,
	                          final boolean isOwner, final long timeRemaining) {
		
		final Component textOwner = Component.literal(ownerName == null ? "-null-" : ownerName)
		                                     .withStyle(style -> style
				                                                         .withColor(ChatFormatting.AQUA)
				                                                         .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				                                                                                        Component.literal(ownerUUID == null ? "-null-" : ownerUUID.toString()))));
		
		final Component textDuration = Component.literal(
				                                        DurationFormatUtils.formatDurationWords(Math.abs(timeRemaining * 1000L), true, true))
		                                        .withStyle(ChatFormatting.RED);
		
		final Component textSize = Component.literal(String.valueOf(inventorySlots.size()))
		                                    .withStyle(ChatFormatting.AQUA);
		
		final Component textMessageToSend;
		if (isOwner) {
			if (timeRemaining <= 0L) {
				textMessageToSend = Component.translatable("gravekeeper.chat.inspect.elapsed_yours",
				                                           textOwner, textSize).withStyle(ChatFormatting.GREEN);
			} else if (!Config.isOwnerOnlyCollection()) {
				textMessageToSend = Component.translatable("gravekeeper.chat.inspect.delayed_yours",
				                                           textSize, textDuration).withStyle(ChatFormatting.GREEN);
			} else {
				textMessageToSend = Component.translatable("gravekeeper.chat.inspect.guarded_yours",
				                                           textSize).withStyle(ChatFormatting.GREEN);
			}
		} else {
			if (timeRemaining <= 0L) {
				textMessageToSend = Component.translatable("gravekeeper.chat.inspect.elapsed_other",
				                                           textOwner, textSize).withStyle(ChatFormatting.GREEN);
			} else if (!Config.isOwnerOnlyCollection()) {
				textMessageToSend = Component.translatable("gravekeeper.chat.inspect.delayed_other",
				                                           textOwner, textSize, textDuration).withStyle(ChatFormatting.GOLD);
			} else {
				textMessageToSend = Component.translatable("gravekeeper.chat.inspect.guarded_other",
				                                           textOwner, textSize).withStyle(ChatFormatting.GOLD);
			}
		}
		player.sendSystemMessage(textMessageToSend);
		
		if (isCreative && (isOwner || timeRemaining <= 0L)) {
			player.sendSystemMessage(Component.translatable("gravekeeper.chat.inspect.survival_required")
			                                  .withStyle(ChatFormatting.RED));
		}
	}
	
	protected void dropItem(final ItemStack itemStack) {
		if (level == null) return;
		
		try {
			ItemEntity entityItem = new ItemEntity(level, getBlockPos().getX() + 0.5D,
			                                       getBlockPos().getY() + 0.5D, getBlockPos().getZ() + 0.5D, itemStack);
			level.addFreshEntity(entityItem);
		} catch (final Exception exception) {
			GraveKeeper.LOGGER.error("Failed to drop item {}", itemStack, exception);
		}
	}
	
	protected void doDropContent() {
		if (level == null) return;
		
		for (InventorySlot slot : inventorySlots) {
			dropItem(slot.itemStack);
		}
		inventorySlots.clear();
		
		level.removeBlockEntity(getBlockPos());
		level.setBlockAndUpdate(getBlockPos(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
	}
	
	private void doReturnToOwner(@Nonnull final ServerPlayer player) {
		if (level == null) return;
		
		final List<ItemStack> overflow = InventoryRestorer.restoreOrOverflow(player, inventorySlots);
		
		for (final ItemStack itemStack : overflow) {
			if (player.getInventory().add(itemStack)) {
				continue;
			}
			dropItem(itemStack);
		}
		inventorySlots.clear();
		
		level.removeBlockEntity(getBlockPos());
		level.setBlockAndUpdate(getBlockPos(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
	}
}
