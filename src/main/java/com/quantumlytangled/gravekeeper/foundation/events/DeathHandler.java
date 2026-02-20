package com.quantumlytangled.gravekeeper.foundation.events;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.quantumlytangled.gravekeeper.Config;
import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.content.grave.GraveBlockEntity;
import com.quantumlytangled.gravekeeper.foundation.CreationDate;
import com.quantumlytangled.gravekeeper.foundation.inventory.InventoryCollector;
import com.quantumlytangled.gravekeeper.foundation.inventory.InventorySlot;
import com.quantumlytangled.gravekeeper.foundation.position.GravePosition;
import com.quantumlytangled.gravekeeper.foundation.position.WorldPosition;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class DeathHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(@Nonnull final LivingDeathEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		// only handle server-side player deaths
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		final Level level = player.level();
		if (level.isClientSide()) {
			return;
		}
		
		// respect keepInventory gamerule
		if (level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY) && !Config.IGNORE_KEEP_INVENTORY.getAsBoolean()) {
			return;
		}
		
		final CreationDate creationDate = new CreationDate();
		final String stringTimestamp = creationDate.string;
		final String playerName = player.getDisplayName().getString();
		final UUID playerUUID = player.getUUID();
		final String identifier = playerUUID + "_" + playerName + "_" + stringTimestamp;
		
		final List<InventorySlot> inventorySlots = InventoryCollector.collectOnDeath(player);
		if (inventorySlots.isEmpty()) {
			GraveKeeper.LOGGER.warn("No items to save, ignoring death of player {}", player);
			return;
		}
		
		// find a position for the
		final WorldPosition worldPositionPlayer = new WorldPosition(player.level(),
		                                                            new BlockPos(
				                                                            (int) Math.floor(player.getX()),
				                                                            (int) Math.floor(player.getY()),
				                                                            (int) Math.floor(player.getZ())
		                                                            ));
		final WorldPosition gravePos = GravePosition.get(player, worldPositionPlayer.blockPos);
		
		// place the grave block
		level.setBlockAndUpdate(gravePos.blockPos, GraveKeeper.GRAVE_BLOCK.get().defaultBlockState());
		
		if (!(level.getBlockEntity(gravePos.blockPos) instanceof GraveBlockEntity grave)) {
			GraveKeeper.LOGGER.error("Missing block entity at {}, unable to save inventory for player {}", gravePos, player);
			return;
		}
		
		grave.setData(player, identifier, creationDate.seconds, inventorySlots);
		
		level.sendBlockUpdated(gravePos.blockPos,
		                       level.getBlockState(gravePos.blockPos),
		                       level.getBlockState(gravePos.blockPos),
		                       Block.UPDATE_ALL);
		
		GraveKeeper.LOGGER.info("Generated DeathChest for {} ({}) in {} at ({} {} {}).",
		                        playerName,
		                        playerUUID,
		                        gravePos.getLevel().dimension(),
		                        gravePos.blockPos.getX(),
		                        gravePos.blockPos.getY(),
		                        gravePos.blockPos.getZ()
		                       );
		
		final Component textLocation = Component.literal(gravePos.format())
		                                        .withStyle(style -> style
				                                                            .withColor(ChatFormatting.AQUA)
				                                                            .withBold(true));
		
		player.sendSystemMessage(Component.translatable("gravekeeper.chat.grave_placed", textLocation)
		                                  .withStyle(ChatFormatting.GOLD));
	}
}
