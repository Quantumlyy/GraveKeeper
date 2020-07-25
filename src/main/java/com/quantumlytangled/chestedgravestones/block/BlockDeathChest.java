package com.quantumlytangled.chestedgravestones.block;

import com.quantumlytangled.chestedgravestones.ChestedGravestones;
import com.quantumlytangled.chestedgravestones.core.Registration;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDeathChest extends Block {

  public BlockDeathChest() {
    super(Material.ANVIL);

    setBlockUnbreakable();
    setResistance(6000000.0F);
    setRegistryName("DeathChest");
    setTranslationKey(ChestedGravestones.MODID + ".death_chest");
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
    Registration.logger.warn(String.format("Death chest broken at %s (%d %d %d) in proximity of player %s",
        world.getProviderName(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), player ));

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
}
