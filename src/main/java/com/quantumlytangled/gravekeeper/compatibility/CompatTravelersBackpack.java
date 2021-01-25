package com.quantumlytangled.gravekeeper.compatibility;

import com.quantumlytangled.gravekeeper.util.InventoryType;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.capability.ITravelersBackpack;
import com.tiviacz.travelersbackpack.handlers.ConfigHandler;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompatTravelersBackpack implements ICompatInventory {
  
  private static final CompatTravelersBackpack INSTANCE = new CompatTravelersBackpack();
  
  public static CompatTravelersBackpack getInstance() {
    return INSTANCE;
  }
  
  @Override
  public InventoryType getType() {
    return InventoryType.TRAVELERS_BACKPACK;
  }
  
  @Override
  public NonNullList<ItemStack> getAllContents(@Nonnull final EntityPlayerMP player) {
    final NonNullList<ItemStack> invContents = NonNullList.withSize(1, ItemStack.EMPTY);
    if (!ConfigHandler.server.backpackDeathPlace) {
      final ItemStack itemStack = CapabilityUtils.getWearingBackpack(player);
      invContents.set(0, itemStack);
    }
    
    return invContents;
  }
  
  @Override
  public void removeItem(@Nonnull final EntityPlayerMP player, final int slot) {
    final ITravelersBackpack travelersBackpack = CapabilityUtils.getCapability(player);
    travelersBackpack.removeWearable();
  }
  
  @Override
  public ItemStack setItemReturnOverflow(@Nonnull final EntityPlayerMP player, final int slot, @Nonnull final ItemStack itemStack) {
    final boolean isWearingBackpack = CapabilityUtils.isWearingBackpack(player);
    if ( slot == 0
      && !isWearingBackpack ) {
      CapabilityUtils.equipBackpack(player, itemStack);
      return ItemStack.EMPTY;
    }
    return itemStack;
  }
}