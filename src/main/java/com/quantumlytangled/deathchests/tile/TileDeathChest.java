package com.quantumlytangled.deathchests.tile;

import net.minecraft.tileentity.TileEntity;

public class TileDeathChest extends TileEntity {
    private String owner = "none";
    private long creationDate = 0;
    public boolean isSecure = true;
}
