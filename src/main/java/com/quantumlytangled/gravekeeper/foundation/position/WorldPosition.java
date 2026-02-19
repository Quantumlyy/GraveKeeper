package com.quantumlytangled.gravekeeper.foundation.position;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class WorldPosition {
	
	public BlockPos blockPos;
	@Nullable
	private ResourceKey<Level> dimensionKey;
	@Nullable
	private Level level;
	
	public WorldPosition(@Nonnull final Level level, @Nonnull final BlockPos blockPos) {
		this.level = level;
		this.dimensionKey = level.dimension();
		this.blockPos = blockPos;
	}
	
	public WorldPosition(@Nonnull final ResourceKey<Level> dimensionKey, @Nonnull final BlockPos blockPos) {
		this.dimensionKey = dimensionKey;
		this.blockPos = blockPos;
	}
	
	public WorldPosition(@Nonnull final CompoundTag tag) {
		this.dimensionKey = ResourceKey.create(Registries.DIMENSION,
		                                       new ResourceLocation(tag.getString("dimensionKey")));
		this.blockPos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
	}
	
	@Nullable
	private static Level getServerLevel(@Nullable final ResourceKey<Level> key) {
		if (key == null) return null;
		final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) return null;
		return server.getLevel(key);
	}
	
	@Nonnull
	public Level getLevel() {
		if (level == null) {
			level = getServerLevel(dimensionKey);
			if (level == null) {
				GraveKeeper.LOGGER.warn("Failed to load dimension {}, defaulting to overworld", dimensionKey);
				level = getServerLevel(Level.OVERWORLD);
			}
		}
		return level;
	}
	
	public void setLevel(@Nonnull final Level level) {
		this.level = level;
		this.dimensionKey = level.dimension();
	}
	
	public boolean isSameLevel(@Nonnull final Level level) {
		return level.dimension().equals(dimensionKey);
	}
	
	@Nonnull
	public CompoundTag writeToNBT(@Nonnull final CompoundTag tag) {
		tag.putString("dimensionKey", getLevel().dimension().location().toString());
		tag.putInt("x", blockPos.getX());
		tag.putInt("y", blockPos.getY());
		tag.putInt("z", blockPos.getZ());
		return tag;
	}
	
	@Nonnull
	public String format() {
		final String dimName = dimensionKey != null ? dimensionKey.location().toString() : "unknown";
		return String.format("[%s] %d %d %d", dimName, blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}
}
