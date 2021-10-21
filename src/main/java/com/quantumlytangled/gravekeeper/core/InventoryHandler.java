package com.quantumlytangled.gravekeeper.core;

import com.quantumlytangled.gravekeeper.util.InventoryType;
import com.quantumlytangled.gravekeeper.util.SoulboundHandler;

import javax.annotation.Nonnull;
import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.GraveKeeperConfig;
import com.quantumlytangled.gravekeeper.compatibility.CompatMain;
import com.quantumlytangled.gravekeeper.compatibility.ICompatInventory;
import com.quantumlytangled.gravekeeper.core.CharmHandler.Mode;
import com.quantumlytangled.gravekeeper.util.InventorySlot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryHandler {
  
  private static final LinkedHashMap<InventoryType, ICompatInventory> compatInventories = new LinkedHashMap<>(10);
  
  public static void addCompatibilityWrapper(@Nonnull final ICompatInventory compatInventory) {
    InventoryHandler.compatInventories.put(compatInventory.getType(), compatInventory);
  }
  
  @Nonnull
  public static List<InventorySlot> collectOnDeath(@Nonnull final EntityPlayerMP player) {
    final List<InventorySlot> inventorySlots = new ArrayList<>();
    
    // check for charms
    // note: we directly access explicitly the main inventory wrapper in case it's not enabled for saving in the grave.
    final CharmHandler.Mode charmMode = computeCharmMode(player);
    if (GraveKeeperConfig.DEBUG_LOGS) {
      GraveKeeper.logger.info(String.format("Charm mode is %s",
          charmMode));
    }
    
    // collect all items
    for (final ICompatInventory compatInventory : compatInventories.values()) {
      collectOnDeath(player, charmMode, inventorySlots, compatInventory);
    }
    
    // restore soulbound items
    if (GraveKeeperConfig.MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY) {
      for (final InventorySlot inventorySlot : inventorySlots) {
        if ( inventorySlot.isSoulbound
          && inventorySlot.type != InventoryType.ARMOUR
          && inventorySlot.type != InventoryType.MAIN ) {
          if (player.inventory.addItemStackToInventory(inventorySlot.itemStack.copy())) {
            continue;
          }
          // main inventory is overflowing => cancel soulbound so we keep that item in the grave
          GraveKeeper.logger.warn(String.format("Failed to move soulbinded item to main inventory (is it full?): %s",
                  inventorySlot.itemStack ));
          inventorySlot.isSoulbound = false;
        }
      }
    }
    
    return inventorySlots;
  }
  
  @Nonnull
  private static CharmHandler.Mode computeCharmMode(@Nonnull final EntityPlayerMP player) {
    CharmHandler.Mode charmMode = Mode.NONE;
    for (ICompatInventory compatInventory : compatInventories.values()) {
      if ( compatInventory.getType() != InventoryType.BAUBLES
        && compatInventory.getType() != InventoryType.MAIN ) {
        continue;
      }
      for (final ItemStack itemStack : compatInventory.getAllContents(player)) {
        if (itemStack.isEmpty()) {
          continue;
        }
        charmMode = CharmHandler.updateMode(charmMode, itemStack);
      }
    }
    return charmMode;
  }
  
  private static void collectOnDeath(@Nonnull final EntityPlayerMP player,
      @Nonnull final CharmHandler.Mode charmMode,
      @Nonnull final List<InventorySlot> inventorySlots,
      @Nonnull final ICompatInventory compatInventory) {
    // compute how many further items are allowed for soulbound
    int countSoulboundRemaining = GraveKeeperConfig.KEEP_SOULBOUND_AMOUNT;
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
      final boolean isCharmed = CharmHandler.isCharmed(charmMode, player.inventory.currentItem, compatInventory.getType(), index, itemStack);
      if (isCharmed) {
        if (GraveKeeperConfig.DEBUG_LOGS) {
          GraveKeeper.logger.info(String.format("Keeping charmed item %s with NBT %s",
              itemStack, itemStack.getTagCompound() ));
        }
      }
      final boolean isSoulbound = !isCharmed
                                  && countSoulboundRemaining > 0
                                  && SoulboundHandler.isSoulbound(itemStack);
      if (isSoulbound) {
        countSoulboundRemaining--;
        if (GraveKeeperConfig.DEBUG_LOGS) {
          GraveKeeper.logger.info(String.format("Keeping soulbound item %s with NBT %s",
              itemStack, itemStack.getTagCompound() ));
        }
      }
      final InventorySlot inventorySlot = new InventorySlot(itemStack, index, compatInventory.getType(), isCharmed, isSoulbound);
      inventorySlots.add(inventorySlot);
      if ( !isCharmed
        && ( !isSoulbound
          || ( GraveKeeperConfig.MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY
            && inventorySlot.type != InventoryType.ARMOUR
            && inventorySlot.type != InventoryType.MAIN ) ) ) {
        compatInventory.removeItem(player, index);
      }
    }
  }
  
  @Nonnull
  public static List<ItemStack> restoreOrOverflow(@Nonnull final EntityPlayerMP player, @Nonnull final List<InventorySlot> inventorySlots,
      final boolean doRestoreSoulbound) {
    final List<ItemStack> overflow = new ArrayList<>();
    for (final InventorySlot inventorySlot : inventorySlots) {
      if ( !doRestoreSoulbound
        && ( inventorySlot.isCharmed
          || inventorySlot.isSoulbound ) ) {
        continue;
      }
      InventoryHandler.restoreOrOverflow(player, inventorySlot, overflow);
    }
    return overflow;
  }
  
  private static void restoreOrOverflow(@Nonnull final EntityPlayerMP player, @Nonnull final InventorySlot inventorySlot, @Nonnull final List<ItemStack> overflow) {
    // get wrapper, falling back to main inventory in case the related mod was removed
    final ICompatInventory compatInventory = compatInventories.getOrDefault(inventorySlot.type, CompatMain.getInstance());
    
    final ItemStack itemStackLeft = compatInventory.setItemReturnOverflow(player, inventorySlot.slot, inventorySlot.itemStack);
    if (!itemStackLeft.isEmpty()) {
      overflow.add(itemStackLeft);
    }
  }
}