package com.quantumlytangled.gravekeeper.core;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.util.InventorySlot;
import com.quantumlytangled.gravekeeper.util.WorldPosition;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class GraveData {
  
  // Version id
  private static final int VERSION = 2;
  
  // Instance properties
  public final List<InventorySlot> inventorySlots;
  public final WorldPosition worldPositionPlayer;
  public final WorldPosition worldPositionGrave;
  
  public GraveData(@Nonnull final List<InventorySlot> inventorySlots, @Nonnull final WorldPosition worldPositionPlayer, @Nonnull final WorldPosition worldPositionGrave) {
    this.inventorySlots = inventorySlots;
    this.worldPositionPlayer = worldPositionPlayer;
    this.worldPositionGrave = worldPositionGrave;
  }
  
  @Nonnull
  public NBTTagCompound writeToNBT() {
    final NBTTagCompound tagContainer = new NBTTagCompound();
    
    tagContainer.setString("modid", GraveKeeper.MODID);
    tagContainer.setString("mod_version", GraveKeeper.VERSION);
    tagContainer.setInteger("format_version", VERSION);
    
    tagContainer.setTag("deathPosition", worldPositionPlayer.writeToNBT(new NBTTagCompound()));
    tagContainer.setTag("gravePosition", worldPositionGrave.writeToNBT(new NBTTagCompound()));
    
    final NBTTagList tagSlots = new NBTTagList();
    for (final InventorySlot inventorySlot : inventorySlots) {
      tagSlots.appendTag(inventorySlot.writeToNBT());
    }
    tagContainer.setTag("inventory_slots", tagSlots);
    
    return tagContainer;
  }
  
  public GraveData(@Nonnull final NBTTagCompound tagContainer) {
    final String modid = tagContainer.getString("modid");
    if (!modid.equals(GraveKeeper.MODID)) {
      throw new RuntimeException(String.format("Invalid InventorySlots format: unknown modid %s", modid));
    }
    final int version = tagContainer.getInteger("format_version");
    if (version > VERSION) {
      throw new RuntimeException(String.format("Invalid InventorySlots format: unknown version %d", version));
    }
    
    if (version >= 2) {
      worldPositionPlayer = new WorldPosition(tagContainer.getCompoundTag("deathPosition"));
      worldPositionGrave = new WorldPosition(tagContainer.getCompoundTag("gravePosition"));
    } else {
      worldPositionPlayer = null;
      worldPositionGrave = null;
    }
    
    final NBTTagList tagSlots = tagContainer.getTagList("inventory_slots", NBT.TAG_COMPOUND);
    inventorySlots = new ArrayList<>(tagSlots.tagCount());
    for (final NBTBase tagSlot : tagSlots) {
      assert tagSlot instanceof NBTTagCompound;
      inventorySlots.add(new InventorySlot((NBTTagCompound) tagSlot));
    }
  }
}