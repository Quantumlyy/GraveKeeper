package com.quantumlytangled.deathchests.block;

import com.quantumlytangled.deathchests.DeathChests;
import com.quantumlytangled.deathchests.tile.TileDeathChest;
import net.minecraft.block.Block;
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
    public BlockDeathChest () {
        super(Material.ROCK);

        setBlockUnbreakable();
        setResistance(6000000.0F);
        setRegistryName("DeathChest");
        setTranslationKey(DeathChests.MODID + ".death_chest");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileDeathChest chest = (TileDeathChest) worldIn.getTileEntity(pos);
        if (!worldIn.isRemote) chest.processRight(playerIn, worldIn, pos);
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nonnull
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileDeathChest();
    }
}
