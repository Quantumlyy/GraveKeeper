package com.quantumlytangled.gravekeeper.core;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.GraveKeeperConfig;
import com.quantumlytangled.gravekeeper.compatibility.CompatMain;
import com.quantumlytangled.gravekeeper.compatibility.ICompatInventory;

import com.quantumlytangled.gravekeeper.util.InventorySlot;
import com.quantumlytangled.gravekeeper.util.InventoryType;

import com.quantumlytangled.gravekeeper.util.SoulboundHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryHandler {
  
  private static final LinkedHashMap<InventoryType, ICompatInventory> compatInventories = new LinkedHashMap<>(10);
  
  public static void addCompatibilityWrapper(@Nonnull final ICompatInventory compatInventory) {
    InventoryHandler.compatInventories.put(compatInventory.getType(), compatInventory);
  }
  
  @Nonnull
  public static List<InventorySlot> collectOnDeath(@Nonnull final ServerPlayerEntity player) {
    final List<InventorySlot> inventorySlots = new ArrayList<>();
    
    // collect all items
    for (final ICompatInventory compatInventory : compatInventories.values()) {
      collectOnDeath(player, inventorySlots, compatInventory);
    }
    
    // restore soulbound items
    if (GraveKeeperConfig.MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY.get()) {
      for (final InventorySlot inventorySlot : inventorySlots) {
        if ( inventorySlot.isSoulbound
             && inventorySlot.type != InventoryType.ARMOUR
             && inventorySlot.type != InventoryType.MAIN ) {
          if (player.inventory.addItemStackToInventory(inventorySlot.itemStack.copy())) {
            continue;
          }
          // main inventory is overflowing => cancel soulbound so we keep that item in the grave
          GraveKeeper.logger.warn(String.format("Failed to move soulbinded item to main inventory (is it full?): %s",
                                                inventorySlot.itemStack));
          inventorySlot.isSoulbound = false;
        }
      }
    }
    
    return inventorySlots;
  }
  
  private static void collectOnDeath(@Nonnull final ServerPlayerEntity player,
                                     @Nonnull final List<InventorySlot> inventorySlots,
                                     @Nonnull final ICompatInventory compatInventory) {
    // compute how many further items are allowed for soulbound
    int countSoulboundRemaining = GraveKeeperConfig.KEEP_SOULBOUND_AMOUNT.get();
    for (final InventorySlot inventorySlot : inventorySlots) {
      if (inventorySlot.isSoulbound) {
        countSoulboundRemaining--;
      }
    }
    
    // scan inventory slots
    final NonNullList<ItemStack> itemStacks = compatInventory.getAllContents(player);
    for (int index = 0; index < itemStacks.size(); index++) {
      final ItemStack itemStack = itemStacks.get(index);
      if (itemStack.isEmpty()) {
        continue;
      }
      final boolean isSoulbound = countSoulboundRemaining > 0
                                  && SoulboundHandler.isSoulbound(itemStack);
      if (isSoulbound) {
        countSoulboundRemaining--;
        if (GraveKeeperConfig.DEBUG_LOGS.get()) {
          GraveKeeper.logger.info(String.format("Keeping soulbound item %s with NBT %s",
                                                itemStack, itemStack.getTag() ));
        }
      }
      final InventorySlot inventorySlot = new InventorySlot(itemStack, index, compatInventory.getType(), isSoulbound);
      inventorySlots.add(inventorySlot);
      if ( !isSoulbound
        || ( GraveKeeperConfig.MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY.get()
          && inventorySlot.type != InventoryType.ARMOUR
          && inventorySlot.type != InventoryType.MAIN ) ) {
        compatInventory.removeItem(player, index);
      }
    }
  }
  
  @Nonnull
  public static List<ItemStack> restoreOrOverflow(@Nonnull final ServerPlayerEntity player, @Nonnull final List<InventorySlot> inventorySlots,
                                                  final boolean doRestoreSoulbound) {
    final List<ItemStack> overflow = new ArrayList<>();
    for (final InventorySlot inventorySlot : inventorySlots) {
      if ( !doRestoreSoulbound
        &&  inventorySlot.isSoulbound ) {
        continue;
      }
      InventoryHandler.restoreOrOverflow(player, inventorySlot, overflow);
    }
    return overflow;
  }
  
  private static void restoreOrOverflow(@Nonnull final ServerPlayerEntity player, @Nonnull final InventorySlot inventorySlot, @Nonnull final List<ItemStack> overflow) {
    // get wrapper, falling back to main inventory in case the related mod was removed
    final ICompatInventory compatInventory = compatInventories.getOrDefault(inventorySlot.type, CompatMain.getInstance());
    
    final ItemStack itemStackLeft = compatInventory.setItemReturnOverflow(player, inventorySlot.slot, inventorySlot.itemStack);
    if (!itemStackLeft.isEmpty()) {
      overflow.add(itemStackLeft);
    }
  }
}
