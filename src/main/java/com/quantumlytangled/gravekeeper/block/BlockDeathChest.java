package com.quantumlytangled.gravekeeper.block;

import com.quantumlytangled.gravekeeper.util.WorldPosition;
import com.quantumlytangled.gravekeeper.GraveKeeper;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDeathChest extends Block {
  
  private final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(
      0.00D, 0.00D, 0.00D,
      1.00D, 0.25D, 1.00D );
  
  public BlockDeathChest() {
    super(Material.ANVIL);
    
    setBlockUnbreakable();
    setResistance(6000000.0F);
    setRegistryName(GraveKeeper.MODID + ":death_chest");
    setTranslationKey(GraveKeeper.MODID + ".death_chest");
  }
  
  @Override
  public boolean onBlockActivated(@Nonnull final World world, @Nonnull final BlockPos blockPos, @Nonnull final IBlockState blockState,
      @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand, @Nonnull final EnumFacing facing,
      final float hitX, final float hitY, final float hitZ) {
    if (world.isRemote) {
      return true;
    }
    
    final TileEntity tileEntity = world.getTileEntity(blockPos);
    if (!(tileEntity instanceof TileDeathChest)) {
      player.sendMessage(new TextComponentString(String.format("Invalid tile entity %s",
          tileEntity )));
      return true;
    }
    final TileDeathChest tileDeathChest = (TileDeathChest) tileEntity;
    
    tileDeathChest.processInteraction((EntityPlayerMP) player);
    return true;
  }
  
  @Override
  public void onBlockExploded(@Nonnull final World world, @Nonnull final BlockPos blockPos, @Nonnull final Explosion explosion) {
    // do NOT call super, do NOT break the block
    
    final EntityPlayer player = world.getClosestPlayer(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 6, false);
    
    GraveKeeper.logger.warn(String.format("Death chest exploded at %s (%d %d %d) due to %s in proximity of player %s",
        WorldPosition.format(world), blockPos.getX(), blockPos.getY(), blockPos.getZ(), explosion, player ));
  }
  
  @Override
  public void breakBlock(@Nonnull final World world, @Nonnull final BlockPos blockPos, @Nonnull final IBlockState blockState) {
    if (world.isRemote) {
      super.breakBlock(world, blockPos, blockState);
      return;
    }
    
    final TileEntity tileEntity = world.getTileEntity(blockPos);
    if (!(tileEntity instanceof TileDeathChest)) {
      super.breakBlock(world, blockPos, blockState);
      return;
    }
    final TileDeathChest tileDeathChest = (TileDeathChest) tileEntity;
    
    final EntityPlayer player = world.getClosestPlayer(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 6, false);
    GraveKeeper.logger.warn(String.format("Death chest broken at %s (%d %d %d) in proximity of player %s",
        WorldPosition.format(world), blockPos.getX(), blockPos.getY(), blockPos.getZ(), player ));

    tileDeathChest.doDropContent();
  }
  
  @Override
  public boolean hasTileEntity(@Nonnull final IBlockState blockState) {
    return true;
  }
  
  @Nonnull
  @Override
  public TileEntity createTileEntity(@Nonnull final World world, @Nonnull final IBlockState blockState) {
    return new TileDeathChest();
  }
  
  @SuppressWarnings("deprecation")
  @Nonnull
  @Override
  public EnumPushReaction getPushReaction(@Nonnull final IBlockState blockState) {
    return EnumPushReaction.BLOCK;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isOpaqueCube(@Nonnull final IBlockState blockState) {
    return false;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isFullCube(@Nonnull final IBlockState blockState) {
    return false;
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @Override
  public AxisAlignedBB getBoundingBox(@Nonnull final IBlockState blockState, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos) {
    return BOUNDING_BOX;
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @Override
  public BlockFaceShape getBlockFaceShape(@Nonnull final IBlockAccess blockAccess, @Nonnull final IBlockState blockState, @Nonnull final BlockPos blockPos, @Nonnull final EnumFacing enumFacing) {
    return enumFacing == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
  }

  @Nonnull
  @SideOnly(Side.CLIENT)
  @Override
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }
}
