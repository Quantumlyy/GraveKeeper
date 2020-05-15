package com.quantumlytangled.deathchests.core;

import com.quantumlytangled.deathchests.compatability.CompatBaubles;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.List;

public class InventoryDeath {

    public final List<InventoryDeathSlot> inventory = new ArrayList<>();

    private EntityPlayer player;

    public void formInventory(EntityPlayer player) {
        this.player = player;

        this.populateInventories();
    }

    private void populateInventories() {
        final InventoryPlayer inventory = player.inventory;

        readContents(inventory.mainInventory, InventoryType.MAIN);
        readContents(inventory.armorInventory, InventoryType.ARMOUR);
        readContents(inventory.offHandInventory, InventoryType.OFFHAND);

        if (DeathChestsConfig.isBaublesLoaded) readContents(CompatBaubles.getAllBaubles(player), InventoryType.BAUBLES);
    }

    private void readContents(NonNullList<ItemStack> contents, InventoryType type) {
        for (int i = 0; i<contents.size(); i++) {
            if (contents.get(i).isEmpty()) continue;
            InventoryDeathSlot entry = new InventoryDeathSlot(contents.get(i), i, type);
            inventory.add(entry);
        }
    }

}
