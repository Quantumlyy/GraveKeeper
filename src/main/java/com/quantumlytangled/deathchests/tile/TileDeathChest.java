package com.quantumlytangled.deathchests.tile;

import net.minecraft.tileentity.TileEntity;

public class TileDeathChest extends TileEntity {

    private String dataIdentifier = null;

    public TileDeathChest() {
        super();
    }

    public TileDeathChest setIdentifier(String identifier) {
        this.dataIdentifier = identifier;
        return this;
    }

}
