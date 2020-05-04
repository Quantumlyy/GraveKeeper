package com.quantumlytangled.deathchests.tile;

import com.quantumlytangled.deathchests.core.InventoryDeath;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.*;

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
        final List<ItemStack> overflow = new ArrayList<>();

        for (int i = 0; i < contents.mainInventory.size(); ++i) {
            final ItemStack item = contents.mainInventory.get(i);
            if (player.inventory.mainInventory.get(i) == ItemStack.EMPTY) {
                player.inventory.mainInventory.set(i, item);
                return;
            }
            overflow.add(item);
        }
        for (int i = 0; i < contents.armorInventory.size(); ++i) {
            final ItemStack item = contents.armorInventory.get(i);
            if (player.inventory.armorInventory.get(i) == ItemStack.EMPTY) {
                player.inventory.armorInventory.set(i, item);
                return;
            }
            overflow.add(item);
        }
        for (int i = 0; i < contents.offHandInventory.size(); ++i) {
            final ItemStack item = contents.offHandInventory.get(i);
            if (player.inventory.offHandInventory.get(i) == ItemStack.EMPTY) {
                player.inventory.offHandInventory.set(i, item);
                return;
            }
            overflow.add(item);
        }

        for (int i = 0; i < overflow.size(); ++i) {
            final ItemStack item = overflow.get(i);
            if (player.inventory.mainInventory.get(i) == ItemStack.EMPTY) {
                player.inventory.mainInventory.set(i, item);
                return;
            }
            world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), item.copy()));
        }

        world.removeTileEntity(pos);
        world.setBlockToAir(pos);
    }
}
