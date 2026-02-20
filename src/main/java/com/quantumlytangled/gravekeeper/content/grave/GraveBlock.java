package com.quantumlytangled.gravekeeper.content.grave;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

public class GraveBlock extends Block implements EntityBlock {
	
	private static final VoxelShape SHAPE = Shapes.or(
			// base slab
			Block.box(0, 0, 0, 16, 3, 16),
			// grave slab
			Block.box(4, 3, 2, 12, 4, 16),
			// cross pole (approximate)
			Block.box(5, 4, 13, 7, 16, 15)
	                                                 );
	
	public GraveBlock(Properties properties) {
		super(properties);
	}
	
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new GraveBlockEntity(pos, state);
	}
	
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
	                                                 @NotNull Player player, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		
		if (!(level.getBlockEntity(pos) instanceof GraveBlockEntity grave)) {
			player.sendSystemMessage(Component.literal("Invalid block entity at " + pos));
			return InteractionResult.SUCCESS;
		}
		
		grave.processInteraction((ServerPlayer) player);
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public @NotNull VoxelShape getShape(
			@NotNull BlockState state,
			@NotNull BlockGetter level,
			@NotNull BlockPos pos,
			@NotNull CollisionContext context
	                                   ) {
		return SHAPE;
	}
	
	@Override
	public boolean useShapeForLightOcclusion(@NotNull BlockState state) {
		return false;
	}
}