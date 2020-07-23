package com.quantumlytangled.chestedgravestones.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryDeath {

  public static int MAX_SOULBOUND = ChestedGravestonesConfig.KEEP_SOULBOUND_AMOUNT;

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

    if (ChestedGravestonesConfig.compatBaubles != null) {
      extractContents(ChestedGravestonesConfig.compatBaubles.getAllContents(player),
          InventoryType.BAUBLES);
    }
    if (ChestedGravestonesConfig.compatGalacticCraft != null) {
      extractContents(ChestedGravestonesConfig.compatGalacticCraft.getAllContents(player),
          InventoryType.GCC);
    }
    if (ChestedGravestonesConfig.compatTechGuns != null) {
      extractContents(ChestedGravestonesConfig.compatTechGuns.getAllContents(player),
                      InventoryType.TECHGUNS);
    }
  }

  private void extractContents(NonNullList<ItemStack> contents, InventoryType type) {
    for (int i = 0; i < contents.size(); i++) {
      if (contents.get(i).isEmpty()) {
        continue;
      }
      InventoryDeathSlot entry = new InventoryDeathSlot(contents.get(i), i, type);
      inventory.add(entry);
      switch (type) {
        case MAIN:
          this.player.inventory.mainInventory.set(i, ItemStack.EMPTY);
        case ARMOUR:
          this.player.inventory.armorInventory.set(i, ItemStack.EMPTY);
        case OFFHAND:
          this.player.inventory.offHandInventory.set(i, ItemStack.EMPTY);
        case BAUBLES:
          ChestedGravestonesConfig.compatBaubles.setItem(i, ItemStack.EMPTY, this.player);
        case GCC:
          ChestedGravestonesConfig.compatGalacticCraft
              .setItem(i, ItemStack.EMPTY, this.player);
      }
    }
  }

}
