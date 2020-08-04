package com.quantumlytangled.gravekeeper.util;

import javax.annotation.Nonnull;
import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.compatability.CompatArmor;
import com.quantumlytangled.gravekeeper.compatability.CompatMain;
import com.quantumlytangled.gravekeeper.compatability.CompatOffHand;
import com.quantumlytangled.gravekeeper.compatability.ICompatInventory;
import com.quantumlytangled.gravekeeper.core.InventorySlot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;

public class InventoryHandler {
  
  // Version id
  private static final int VERSION = 1;
  
  // Vanilla wrappers
  public static ICompatInventory compatArmor = CompatArmor.getInstance();
  public static ICompatInventory compatMain = CompatMain.getInstance();
  public static ICompatInventory compatOffHand = CompatOffHand.getInstance();
  
  // Modded wrappers
  public static ICompatInventory compatBaubles = null;
  public static ICompatInventory compatGalacticCraft = null;
  public static ICompatInventory compatTechGuns = null;

  @Nonnull
  public static List<InventorySlot> collectOnDeath(@Nonnull final EntityPlayerMP player) {
    final List<InventorySlot> inventorySlots = new ArrayList<>();

    collectOnDeath(player, inventorySlots, compatArmor);
    collectOnDeath(player, inventorySlots, compatOffHand);
    collectOnDeath(player, inventorySlots, compatMain);

    if (compatBaubles != null) {
      collectOnDeath(player, inventorySlots, compatBaubles);
    }
    if (compatGalacticCraft != null) {
      collectOnDeath(player, inventorySlots, compatGalacticCraft);
    }
    if (compatTechGuns != null) {
      collectOnDeath(player, inventorySlots, compatTechGuns);
    }
    
    return inventorySlots;
  }

  private static void collectOnDeath(@Nonnull final EntityPlayerMP player, @Nonnull final List<InventorySlot> inventorySlots, @Nonnull final ICompatInventory compatInventory) {
    final NonNullList<ItemStack> itemStacks = compatInventory.getAllContents(player);
    for (int index = 0; index < itemStacks.size(); index++) {
      if (itemStacks.get(index).isEmpty()) {
        continue;
      }
      final InventorySlot inventorySlot = new InventorySlot(itemStacks.get(index), index, compatInventory.getType());
      inventorySlots.add(inventorySlot);
      compatInventory.removeItem(player, index);
    }
  }

  @Nonnull
  public static List<ItemStack> restoreOrOverflow(@Nonnull final EntityPlayerMP player, @Nonnull final List<InventorySlot> inventorySlots) {
    final List<ItemStack> overflow = new ArrayList<>();
    for (final InventorySlot inventorySlot : inventorySlots) {
      InventoryHandler.restoreOrOverflow(player, inventorySlot, overflow);
    }
    return overflow;
  }
  
  private static void restoreOrOverflow(@Nonnull final EntityPlayerMP player, @Nonnull final InventorySlot inventorySlot, @Nonnull final List<ItemStack> overflow) {
    // get wrapper, falling back to main inventory in case the related mod was removed
    final ICompatInventory compatInventory;
    switch (inventorySlot.type) {
      case MAIN:
      default:
        compatInventory = compatMain;
        break;

      case ARMOUR:
        compatInventory = compatArmor;
        break;

      case OFFHAND:
        compatInventory = compatOffHand;
        break;

      case BAUBLES:
        compatInventory = compatBaubles != null ? compatBaubles : compatMain;
        break;

      case GALACTICRAFT:
        compatInventory = compatGalacticCraft != null ? compatGalacticCraft : compatMain;
        break;

      case TECHGUNS:
        compatInventory = compatTechGuns != null ? compatTechGuns : compatMain;
        break;
    }

    final ItemStack itemStackLeft = compatInventory.setItemReturnOverflow(player, inventorySlot.slot, inventorySlot.itemStack);
    if (!itemStackLeft.isEmpty()) {
      overflow.add(itemStackLeft);
    }
  }
  
  @Nonnull
  public static NBTTagCompound writeToNBT(@Nonnull final List<InventorySlot> inventorySlots) {
    final NBTTagCompound tagContainer = new NBTTagCompound();
    
    tagContainer.setString("modid", GraveKeeper.MODID);
    tagContainer.setString("mod_version", GraveKeeper.VERSION);
    tagContainer.setInteger("format_version", VERSION);

    final NBTTagList tagSlots = new NBTTagList();
    for (final InventorySlot inventorySlot : inventorySlots) {
      tagSlots.appendTag(inventorySlot.writeToNBT());
    }
    tagContainer.setTag("inventory_slots", tagSlots);
    
    return tagContainer;
  }
  
  @Nonnull
  public static List<InventorySlot> readFromNBT(@Nonnull final NBTTagCompound tagContainer) {
    final String modid = tagContainer.getString("modid");
    if (!modid.equals(GraveKeeper.MODID)) {
      throw new RuntimeException(String.format("Invalid InventorySlots format: unknown modid %s", modid));
    }
    final int version = tagContainer.getInteger("format_version");
    if (version != VERSION) {
      throw new RuntimeException(String.format("Invalid InventorySlots format: unknown version %d", version));
    }
    
    final NBTTagList tagSlots = tagContainer.getTagList("inventory_slots", NBT.TAG_COMPOUND);
    final List<InventorySlot> inventorySlots = new ArrayList<>(tagSlots.tagCount());
    for (final NBTBase tagSlot : tagSlots) {
      assert tagSlot instanceof NBTTagCompound;
      inventorySlots.add(new InventorySlot((NBTTagCompound) tagSlot));
    }

    return inventorySlots;
  }
}