package com.quantumlytangled.deathchests.compatability;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatBaubles {

    public static NonNullList<ItemStack> getAllBaubles(EntityPlayer player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);

        NonNullList<ItemStack> invContents = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i<handler.getSlots(); i++) {
            invContents.set(i, handler.getStackInSlot(i));
        }

        return invContents;
    }

    public static void setItem(int slot, ItemStack item, EntityPlayer player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        handler.setStackInSlot(slot, item);
    }

    public static void clearInventory(EntityPlayer player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);

        for (int i = 0; i < handler.getSlots(); ++i)
        {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

}
