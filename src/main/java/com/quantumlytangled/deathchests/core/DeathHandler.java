package com.quantumlytangled.deathchests.core;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import java.util.List;

public final class DeathHandler {

    public static void restoreVanillaInventory(NonNullList<ItemStack> contents, NonNullList<ItemStack> inventory, List<ItemStack> overflow) {
        for (int i = 0; i < contents.size(); ++i) {
            final ItemStack item = contents.get(i);
            if (item.isEmpty()) continue;
            if (inventory.get(i).isEmpty()) {
                inventory.set(i, item.copy());
                continue;
            }
            overflow.add(item);
        }
    }

    public static NBTTagList formNBTTagListFromInventoryContents(NBTTagList list, NonNullList<ItemStack> contents) {
        for (int i = 0; i < contents.size(); ++i) {
            NBTTagCompound item = new NBTTagCompound();
            item.setByte("Slot", (byte) i);
            list.appendTag(contents.get(i).writeToNBT(item));
        }
        return list;
    }

    public static void readInventoryContentsFromNBTTagList(NBTTagList list, NonNullList<ItemStack> contents) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            contents.set(tag.getByte("Slot"), new ItemStack(tag));
        }
    }

}
