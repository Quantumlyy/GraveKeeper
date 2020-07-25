package com.quantumlytangled.chestedgravestones.block;

import com.quantumlytangled.chestedgravestones.core.ChestedGravestonesConfig;
import com.quantumlytangled.chestedgravestones.core.InventorySlot;
import com.quantumlytangled.chestedgravestones.core.InventoryType;
import com.quantumlytangled.chestedgravestones.util.InventoryHandler;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

public class TileDeathChest extends TileEntity {

  private String dataIdentifier = null;

  private String ownerName = null;
  private UUID ownerUUID = null;

  private long creationDate = 0;

  private List<InventorySlot> inventorySlots = new ArrayList<>();

  public TileDeathChest() {
    super();
  }

  public void processInteraction(@Nonnull final EntityPlayerMP player) {
    if ( player.isCreative()
      || player.isSneaking() ) {
      doInspection(player);
      
    } else if (player.getUniqueID().equals(ownerUUID)) {
      doReturnToOwner(player);
      
    } else if ( ( ChestedGravestonesConfig.INSTANT_FOREIGN_COLLECTION
               || ChestedGravestonesConfig.getExpiredStatus(creationDate) )
            && !ChestedGravestonesConfig.OWNER_ONLY_COLLECTION ) {
      doDropContent();
    }
  }

  public void setData(@Nonnull final EntityPlayer player, @Nonnull final String identifier,
      @Nonnull final ZonedDateTime creation, @Nonnull final List<InventorySlot> inventorySlots) {
    dataIdentifier = identifier;
    ownerName = player.getDisplayNameString();
    ownerUUID = player.getUniqueID();
    creationDate = creation.getLong(ChronoField.INSTANT_SECONDS);
    this.inventorySlots = inventorySlots;
    markDirty();
  }

  @Override
  public void readFromNBT(@Nonnull NBTTagCompound tagCompound) {
    super.readFromNBT(tagCompound);

    final NBTTagList nbtInventorySlots = tagCompound.getTagList("InventorySlots", Constants.NBT.TAG_COMPOUND);
    for (int index = 0; index < nbtInventorySlots.tagCount(); index++) {
      final NBTTagCompound nbtInventorySlot = nbtInventorySlots.getCompoundTagAt(index);
      inventorySlots.add(new InventorySlot(
          new ItemStack(nbtInventorySlot),
          nbtInventorySlot.getInteger("Slot"),
          InventoryType.valueOf(nbtInventorySlot.getString("Type")) ));
    }

    dataIdentifier = tagCompound.getString("DataIdentifier");
    ownerName = tagCompound.getString("OwnerName");
    ownerUUID = tagCompound.getUniqueId("OwnerUUID");
    creationDate = tagCompound.getLong("CreationDate");
  }

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound tagCompound) {
    super.writeToNBT(tagCompound);

    final NBTTagList nbtInventorySlots = new NBTTagList();
    for (final InventorySlot inventorySlot : inventorySlots) {
      NBTTagCompound nbtEntry = new NBTTagCompound();
      nbtEntry.setInteger("Slot", (byte) inventorySlot.slot);
      nbtEntry.setString("Type", inventorySlot.type.toString());
      nbtInventorySlots.appendTag(inventorySlot.itemStack.writeToNBT(nbtEntry));
    }

    tagCompound.setString("DataIdentifier", dataIdentifier);
    tagCompound.setString("OwnerName", ownerName);
    tagCompound.setUniqueId("OwnerUUID", ownerUUID);
    tagCompound.setLong("CreationDate", creationDate);
    tagCompound.setTag("InventorySlots", nbtInventorySlots);

    return tagCompound;
  }

  private void doInspection(@Nonnull final EntityPlayer player) {
    player.sendMessage(new TextComponentString(String.format("Here lies %s (%s)\nDeathData file name - %s",
        ownerName, ownerUUID, dataIdentifier )));
  }

  protected void doDropContent() {
    for (InventorySlot inventory : inventorySlots) {
      final EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, inventory.itemStack);
      world.spawnEntity(entityItem);
    }
  }

  private void doReturnToOwner(@Nonnull final EntityPlayerMP player) {
    final List<ItemStack> overflow = InventoryHandler.restoreOrOverflow(player, inventorySlots);
    
    for (final ItemStack itemStack : overflow) {
      if (player.inventory.addItemStackToInventory(itemStack.copy())) {
        continue;
      }
      world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack));
    }

    world.removeTileEntity(pos);
    world.setBlockToAir(pos);
  }
}