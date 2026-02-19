package com.quantumlytangled.gravekeeper.foundation.position;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import com.quantumlytangled.gravekeeper.Config;
import com.quantumlytangled.gravekeeper.GraveKeeper;

public class GravePosition {
	
	@Nonnull
	public static WorldPosition get(@Nonnull final ServerPlayer player, @Nonnull final BlockPos blockPosInitial) {
		WorldPosition worldPositionResult = new WorldPosition(player.level(), blockPosInitial);
		
		// force bed or spawn location as a starting point
		if (worldPositionResult.blockPos.getY() <= Config.USE_BED_OR_SPAWN_LOCATION_BELOW_Y.getAsInt()) {
			worldPositionResult.blockPos = player.getRespawnPosition();
		}
		
		// adjust the starting altitude within the world bounds
		if (worldPositionResult.blockPos.getY() >= worldPositionResult.getLevel().getMaxBuildHeight()) {
			worldPositionResult.blockPos = new BlockPos(
					worldPositionResult.blockPos.getX(),
					worldPositionResult.getLevel().getMaxBuildHeight() - 1,
					worldPositionResult.blockPos.getZ());
		} else if (worldPositionResult.blockPos.getY() < Config.SEARCH_MIN_ALTITUDE.getAsInt()) {
			worldPositionResult.blockPos = new BlockPos(
					worldPositionResult.blockPos.getX(),
					Config.SEARCH_MIN_ALTITUDE.getAsInt(),
					worldPositionResult.blockPos.getZ());
		}
		
		// stop here if we start on a good spot
		if (isFreeSpot(worldPositionResult.getLevel(), worldPositionResult.blockPos, true, false)) {
			return worldPositionResult;
		}
		
		// search for a free spot around the starting point
		if (updateWithNearbyFreeSpot(worldPositionResult)) {
			return worldPositionResult;
		}
		
		// look for the ground if we are in the air, look above us otherwise
		final boolean isFlying = isFreeSpot(worldPositionResult.getLevel(), worldPositionResult.blockPos, false, false);
		if (updateVertically(worldPositionResult, isFlying)) {
			return worldPositionResult;
		}
		
		// look in the opposite direction
		GraveKeeper.LOGGER.info("Player has no safe spot for its grave, releasing direction: {} {}",
		                        player, worldPositionResult.blockPos);
		if (updateVertically(worldPositionResult, !isFlying)) {
			return worldPositionResult;
		}
		
		// flying in the void? just forget the solid block constraints
		if (isFlying) {
			return worldPositionResult;
		}
		
		// we did our best, admins will fix it up from there
		final BlockState blockState = worldPositionResult.getLevel().getBlockState(worldPositionResult.blockPos);
		final BlockEntity blockEntity = worldPositionResult.getLevel().getBlockEntity(worldPositionResult.blockPos);
		GraveKeeper.LOGGER.warn("Can't find free slot for grave, deleting {} with block entity {} at {}",
		                        blockState, blockEntity, worldPositionResult.blockPos);
		if (blockEntity != null) {
			try {
				final CompoundTag tag = blockEntity.saveWithFullMetadata(worldPositionResult.getLevel().registryAccess());
				GraveKeeper.LOGGER.info("NBT is {}", tag);
			} catch (final Exception exception) {
				GraveKeeper.LOGGER.error("Failed to save block entity NBT", exception);
			}
		}
		return worldPositionResult;
	}
	
	
	private static boolean isFreeSpot(@Nonnull final Level level, @Nonnull final BlockPos blockPos,
	                                  final boolean shouldCheckBase, final boolean allowLiquidBase) {
		// ignore bottom and top of world
		if (blockPos.getY() < Config.SEARCH_MIN_ALTITUDE.get()
		    || blockPos.getY() > level.getMaxBuildHeight() - 1) {
			return false;
		}
		
		// require base block
		final BlockState blockStateDown = level.getBlockState(blockPos.below());
		if (shouldCheckBase
		    && !blockStateDown.isSolid()
		    && (!allowLiquidBase || !blockStateDown.liquid())) {
			return false;
		}
		
		// always exclude fluids
		final BlockState blockState = level.getBlockState(blockPos);
		if (blockState.liquid()) {
			return false;
		}
		
		// accept air or replaceable
		final boolean isAirOrReplaceable = blockState.isAir() || blockState.canBeReplaced();
		if (isAirOrReplaceable && Config.DEBUG_LOGS.get()) {
			GraveKeeper.LOGGER.info("Found free spot at ({} {} {})",
			                        blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}
		return isAirOrReplaceable;
	}
	
	private static boolean updateWithNearbyFreeSpot(@Nonnull final WorldPosition worldPositionResult) {
		if (Config.DEBUG_LOGS.get()) {
			GraveKeeper.LOGGER.info("Starting position is {}, searching nearby", worldPositionResult.blockPos);
		}
		
		final int xMin = worldPositionResult.blockPos.getX() - Config.SEARCH_RADIUS_HORIZONTAL_M.get();
		final int xMax = worldPositionResult.blockPos.getX() + Config.SEARCH_RADIUS_HORIZONTAL_M.get();
		final int yMin = Math.max(worldPositionResult.blockPos.getY() - Config.SEARCH_RADIUS_BELOW_M.get(),
		                          Config.SEARCH_MIN_ALTITUDE.get());
		final int yMax = Math.min(worldPositionResult.blockPos.getY() + Config.SEARCH_RADIUS_ABOVE_M.get(),
		                          worldPositionResult.getLevel().getMaxBuildHeight() - 1);
		final int zMin = worldPositionResult.blockPos.getZ() - Config.SEARCH_RADIUS_HORIZONTAL_M.get();
		final int zMax = worldPositionResult.blockPos.getZ() + Config.SEARCH_RADIUS_HORIZONTAL_M.get();
		
		final Vec3 vStarting = new Vec3(
				worldPositionResult.blockPos.getX() + 0.5D,
				worldPositionResult.blockPos.getY() + 0.5D,
				worldPositionResult.blockPos.getZ() + 0.5D);
		
		final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(
				worldPositionResult.blockPos.getX(),
				worldPositionResult.blockPos.getY(),
				worldPositionResult.blockPos.getZ());
		
		BlockPos blockPosVisible = worldPositionResult.blockPos;
		BlockPos blockPosHidden = worldPositionResult.blockPos;
		int distanceClosestVisible = Integer.MAX_VALUE;
		int distanceClosestHidden = Integer.MAX_VALUE;
		
		for (int x = xMin; x <= xMax; x++) {
			if (Math.abs(x) > Math.max(distanceClosestVisible, distanceClosestHidden)) continue;
			for (int z = zMin; z <= zMax; z++) {
				if (Math.abs(z) > Math.max(distanceClosestVisible, distanceClosestHidden)) continue;
				for (int y = yMin; y <= yMax; y++) {
					if (Math.abs(y) > Math.max(distanceClosestVisible, distanceClosestHidden)) continue;
					mutableBlockPos.set(x, y, z);
					if (isFreeSpot(worldPositionResult.getLevel(), mutableBlockPos, true, false)) {
						final int distanceCurrent = (int) Math.round(mutableBlockPos.distToCenterSqr(
								worldPositionResult.blockPos.getX() + 0.5D,
								worldPositionResult.blockPos.getY() + 0.5D,
								worldPositionResult.blockPos.getZ() + 0.5D));
						
						final Vec3 vTarget = new Vec3(
								mutableBlockPos.getX() + 0.5D,
								mutableBlockPos.getY() + 0.5D,
								mutableBlockPos.getZ() + 0.5D);
						
						final HitResult hitResult = worldPositionResult.getLevel().clip(
								new ClipContext(vStarting, vTarget,
								                ClipContext.Block.COLLIDER,
								                ClipContext.Fluid.NONE,
								                CollisionContext.empty()));
						final boolean isHidden = hitResult.getType() == HitResult.Type.MISS
						                         || !(hitResult instanceof BlockHitResult blockHit)
						                         || !blockHit.getBlockPos().equals(mutableBlockPos);
						
						if (isHidden) {
							if (distanceCurrent < distanceClosestHidden) {
								if (Config.DEBUG_LOGS.get()) {
									GraveKeeper.LOGGER.info("New hidden free spot is closer: {} -> {}", distanceClosestHidden, distanceCurrent);
								}
								distanceClosestHidden = distanceCurrent;
								blockPosHidden = mutableBlockPos.immutable();
							}
						} else if (distanceCurrent < distanceClosestVisible) {
							if (Config.DEBUG_LOGS.get()) {
								GraveKeeper.LOGGER.info("New visible free spot is closer: {} -> {}", distanceClosestVisible, distanceCurrent);
							}
							distanceClosestVisible = distanceCurrent;
							blockPosVisible = mutableBlockPos.immutable();
						}
					}
				}
			}
		}
		
		if (distanceClosestVisible != Integer.MAX_VALUE) {
			if (Config.DEBUG_LOGS.get()) {
				GraveKeeper.LOGGER.info("Found closest visible block {} m away at ({} {} {})",
				                        distanceClosestVisible, blockPosVisible.getX(), blockPosVisible.getY(), blockPosVisible.getZ());
			}
			worldPositionResult.blockPos = blockPosVisible;
			return true;
		}
		if (distanceClosestHidden != Integer.MAX_VALUE) {
			if (Config.DEBUG_LOGS.get()) {
				GraveKeeper.LOGGER.info("Found closest hidden block {} m away at ({} {} {})",
				                        distanceClosestHidden, blockPosHidden.getX(), blockPosHidden.getY(), blockPosHidden.getZ());
			}
			worldPositionResult.blockPos = blockPosHidden;
			return true;
		}
		return false;
	}
	
	private static boolean updateVertically(@Nonnull final WorldPosition worldPositionResult, final boolean isFlying) {
		if (Config.DEBUG_LOGS.get()) {
			GraveKeeper.LOGGER.info("Starting position is {}, searching {}",
			                        worldPositionResult.blockPos, isFlying ? "down below" : "up above");
		}
		
		final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(
				worldPositionResult.blockPos.getX(),
				worldPositionResult.blockPos.getY(),
				worldPositionResult.blockPos.getZ());
		
		int y = worldPositionResult.blockPos.getY();
		while (y >= Config.SEARCH_MIN_ALTITUDE.get()
		       && y < worldPositionResult.getLevel().getMaxBuildHeight() - 1) {
			mutableBlockPos.setY(y);
			if (isFreeSpot(worldPositionResult.getLevel(), mutableBlockPos, true, true)) {
				if (Config.DEBUG_LOGS.get()) {
					GraveKeeper.LOGGER.info("Found vertical block at ({} {} {})",
					                        mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ());
				}
				worldPositionResult.blockPos = mutableBlockPos.immutable();
				return true;
			}
			if (isFlying) {
				y--;
			} else {
				y++;
			}
		}
		return false;
	}
}
