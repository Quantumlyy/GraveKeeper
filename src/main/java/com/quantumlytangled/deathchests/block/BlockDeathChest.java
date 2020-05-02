package com.quantumlytangled.deathchests.block;

import com.quantumlytangled.deathchests.DeathChests;
import com.quantumlytangled.deathchests.tile.TileDeathChest;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDeathChest extends Block {
    public BlockDeathChest () {
        super(Material.ROCK);

        setBlockUnbreakable();
        setRegistryName("DeathChest");
        setTranslationKey(DeathChests.MODID + ".death_chest");
    }

    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileDeathChest();
    }
}
