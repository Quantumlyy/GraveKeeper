package com.quantumlytangled.chestedgravestones.compatability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import techguns.capabilities.TGExtendedPlayer;

public class CompatTechguns implements ICompatInventory {

    private static final CompatTechguns INSTANCE = new CompatTechguns();

    public static CompatTechguns getInstance() {
        return INSTANCE;
    }

    public NonNullList<ItemStack> getAllContents(EntityPlayerMP player) {
        IInventory inventory = getTGInventory(player);

        NonNullList<ItemStack> invContents = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSizeInventory(); i++)
            invContents.set(i, inventory.getStackInSlot(i));

        return invContents;
    }

    public void setItem(int slot, ItemStack item, EntityPlayerMP player) {
        IInventory inventory = getTGInventory(player);
        inventory.setInventorySlotContents(slot, item);
    }

    public boolean isSlotEmpty(int slot, EntityPlayerMP player) {
        IInventory inventory = getTGInventory(player);
        return inventory.getStackInSlot(slot).isEmpty();
    }

    public void clearInventory(EntityPlayerMP player) {
        IInventory inventory = getTGInventory(player);

        for (int i = 0; i < inventory.getSizeInventory(); ++i)
            inventory.setInventorySlotContents(i, ItemStack.EMPTY);
    }

    private static IInventory getTGInventory(EntityPlayer player) {
        return TGExtendedPlayer.get(player).tg_inventory;
    }

}
