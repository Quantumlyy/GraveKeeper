package com.quantumlytangled.deathchests.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventoryDeath {

    public final NonNullList<ItemStack> mainInventory = NonNullList.withSize(36, ItemStack.EMPTY);
    public final NonNullList<ItemStack> armorInventory = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> offHandInventory = NonNullList.withSize(1, ItemStack.EMPTY);

    // TODO: Adjust size
    public final NonNullList<ItemStack> baublesInventory = NonNullList.withSize(10, ItemStack.EMPTY);
    // TODO: Adjust size
    public final NonNullList<ItemStack> galacticCraftInventory = NonNullList.withSize(100, ItemStack.EMPTY);
    // TODO: Adjust size
    public final NonNullList<ItemStack> techGunsInventory = NonNullList.withSize(100, ItemStack.EMPTY);

    public final List<NonNullList<ItemStack>> allInventories;

    private EntityPlayer player;

    public InventoryDeath() {
        this.allInventories = Arrays.asList(
                this.mainInventory,
                this.armorInventory,
                this.offHandInventory,
                this.baublesInventory,
                this.galacticCraftInventory,
                this.techGunsInventory);
    }

    public void formInventory(EntityPlayer player) {
        this.player = player;

        this.populateInventories();
    }

    private void populateInventories() {
        final InventoryPlayer inventory = player.inventory;

        Collections.copy(mainInventory, inventory.mainInventory);
        Collections.copy(armorInventory, inventory.armorInventory);
        Collections.copy(offHandInventory, inventory.offHandInventory);
    }

}
