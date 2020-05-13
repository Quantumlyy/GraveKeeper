package com.quantumlytangled.deathchests.block;

import com.quantumlytangled.deathchests.DeathChests;
import com.quantumlytangled.deathchests.core.CustomEntitySelectors;
import com.quantumlytangled.deathchests.tile.TileDeathChest;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockDeathChest extends Block {
    public BlockDeathChest() {
        super(Material.ANVIL);

        setBlockUnbreakable();
        setResistance(6000000.0F);
        setRegistryName("DeathChest");
        setTranslationKey(DeathChests.MODID + ".death_chest");
    }

    @Override
    public boolean onBlockActivated(
            @Nonnull World worldIn,
            @Nonnull BlockPos pos,
            @Nonnull IBlockState state,
            @Nonnull EntityPlayer playerIn,
            @Nonnull EnumHand hand,
            @Nonnull EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ) {
        if (worldIn.isRemote) return true;

        final TileEntity tChest = worldIn.getTileEntity(pos);
        if (!(tChest instanceof TileDeathChest)) return true;
        final TileDeathChest chest = (TileDeathChest) tChest;

        chest.processRight(playerIn, worldIn, pos);
        return true;
    }

    @Override
    public void breakBlock(
            @Nonnull World worldIn,
            @Nonnull BlockPos pos,
            @Nonnull IBlockState state) {
        if (worldIn.isRemote) {
            super.breakBlock(worldIn, pos, state);
            return;
        }

        final TileEntity tChest = worldIn.getTileEntity(pos);
        if (!(tChest instanceof TileDeathChest)) {
            super.breakBlock(worldIn, pos, state);
            return;
        }
        final TileDeathChest chest = (TileDeathChest) tChest;
        EntityPlayer player = worldIn.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 6, CustomEntitySelectors.IN_CREATIVE);

        chest.processLeft(player, worldIn, pos);
    }

    @Override
    public boolean hasTileEntity(@Nonnull IBlockState state) {
        return true;
    }

    @Nonnull
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileDeathChest();
    }
}
