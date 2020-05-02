package com.quantumlytangled.deathchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockDeathChest extends Block {
    public BlockDeathChest () {
        super(Material.ROCK);

        setBlockUnbreakable();
        setRegistryName("DeathChest");
    }
}
