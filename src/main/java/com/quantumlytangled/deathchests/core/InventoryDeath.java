package com.quantumlytangled.deathchests.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.List;

public class InventoryDeath {

    public static int MAX_SOULBOUND = DeathChestsConfig.KEEP_SOULBOUND_AMOUNT;

    public final List<InventoryDeathSlot> inventory = new ArrayList<>();

    private EntityPlayerMP player;

    public void formInventory(EntityPlayerMP player) {
        this.player = player;

        this.populateInventories();
    }

    private void populateInventories() {
        final InventoryPlayer inventory = player.inventory;

        extractContents(inventory.armorInventory, InventoryType.ARMOUR);
        extractContents(inventory.offHandInventory, InventoryType.OFFHAND);
        extractContents(inventory.mainInventory, InventoryType.MAIN);

        if (DeathChestsConfig.isBaublesLoaded != null) extractContents(DeathChestsConfig.isBaublesLoaded.getAllContents(player), InventoryType.BAUBLES);
        if (DeathChestsConfig.isGalacticCraftCoreLoaded != null) extractContents(DeathChestsConfig.isGalacticCraftCoreLoaded.getAllContents(player), InventoryType.GCC);
    }

    private void extractContents(NonNullList<ItemStack> contents, InventoryType type) {
        for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i).isEmpty()) continue;
            InventoryDeathSlot entry = new InventoryDeathSlot(contents.get(i), i, type);
            inventory.add(entry);
            switch (type) {
                case MAIN:
                    this.player.inventory.mainInventory.set(i, ItemStack.EMPTY);
                    break;
                case ARMOUR:
                    this.player.inventory.armorInventory.set(i, ItemStack.EMPTY);
                    break;
                case OFFHAND:
                    this.player.inventory.offHandInventory.set(i, ItemStack.EMPTY);
                    break;
                case BAUBLES:
                    DeathChestsConfig.isBaublesLoaded.setItem(i, ItemStack.EMPTY, this.player);
                    break;
                case GCC:
                    DeathChestsConfig.isGalacticCraftCoreLoaded.setItem(i, ItemStack.EMPTY, this.player);
                    break;
            }
        }
    }

}
