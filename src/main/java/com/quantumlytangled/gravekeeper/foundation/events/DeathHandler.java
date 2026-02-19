package com.quantumlytangled.gravekeeper.foundation.events;

import javax.annotation.Nonnull;
import java.util.List;

import com.quantumlytangled.gravekeeper.Config;

import com.quantumlytangled.gravekeeper.content.grave.GraveBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import com.quantumlytangled.gravekeeper.GraveKeeper;
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
		grave.setInventorySlots(inventorySlots);
	}
}
