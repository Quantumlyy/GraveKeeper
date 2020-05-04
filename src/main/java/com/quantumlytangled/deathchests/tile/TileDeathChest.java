package com.quantumlytangled.deathchests.tile;

import com.quantumlytangled.deathchests.core.InventoryDeath;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.Date;
import java.util.UUID;

public class TileDeathChest extends TileEntity {

    private String dataIdentifier = null;

    private String ownerName = null;
    private UUID ownerUUID = null;

    private long creationDate = 0;

    private InventoryDeath contents;

    public TileDeathChest() {
        super();
    }

    public void processRight(EntityPlayer player, World world, BlockPos pos) {
        if (checkPlayerInvalidity(player)) return;
        if (player.isCreative()) processCreativeInspect(player, world, pos);
            else processItemReturn(player, world, pos);
    }

    public void processLeft(EntityPlayer player, World world, BlockPos pos) {
        if (checkPlayerInvalidity(player)) return;
        if (player.isCreative()) processCreativeItemReturn(player, world, pos);
    }

    public void setData(String identifier, String name, UUID uniqueID, Date creation, InventoryDeath inventory) {
        dataIdentifier = identifier;
        ownerName = name;
        ownerUUID = uniqueID;
        creationDate = creation.getTime();
        contents = inventory;
    }

    private boolean checkPlayerInvalidity(EntityPlayer player) {
        return !((ownerUUID == player.getUniqueID()) || player.isCreative());
    }

    private void processCreativeInspect(EntityPlayer player, World world, BlockPos pos) {
        player.sendMessage(new TextComponentString(ownerName));
    }

    private void processCreativeItemReturn(EntityPlayer player, World world, BlockPos pos) {
        for (NonNullList<ItemStack> inventory : contents.allInventories) {
            for (ItemStack item : inventory) {
                world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), item.copy()));
            }
        }
    }

    private void processItemReturn(EntityPlayer player, World world, BlockPos pos) {
        world.setBlockToAir(pos);
    }

}
