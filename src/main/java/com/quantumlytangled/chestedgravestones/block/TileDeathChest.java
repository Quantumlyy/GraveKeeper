package com.quantumlytangled.chestedgravestones.block;

import com.quantumlytangled.chestedgravestones.core.ChestedGravestonesConfig;
import com.quantumlytangled.chestedgravestones.core.CreationDate;
import com.quantumlytangled.chestedgravestones.core.InventorySlot;
import com.quantumlytangled.chestedgravestones.core.InventoryType;
import com.quantumlytangled.chestedgravestones.util.InventoryHandler;
import org.apache.commons.lang3.time.DurationFormatUtils;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraftforge.common.util.Constants;

public class TileDeathChest extends TileEntity {

  private String dataIdentifier = "";

  private String ownerName = "";
  private UUID ownerUUID = null;

  private long creationDate = 0;

  private List<InventorySlot> inventorySlots = new ArrayList<>();

  public TileDeathChest() {
    super();
  }

  public void processInteraction(@Nonnull final EntityPlayerMP player) {
    final boolean isCreative = player.isCreative();
    final boolean isOwner = player.getUniqueID().equals(ownerUUID);
    final long timeRemaining = CreationDate.getRemainingSeconds(creationDate);
    if ( isCreative
      || player.isSneaking() ) {
      doInspection(player, isCreative, isOwner, timeRemaining);
      
    } else if (isOwner) {
      doReturnToOwner(player);
      
    } else if ( ( ChestedGravestonesConfig.INSTANT_FOREIGN_COLLECTION
               || timeRemaining <= 0L )
             && !ChestedGravestonesConfig.OWNER_ONLY_COLLECTION ) {
      doDropContent();
      
    } else {
      doInspection(player, false, false, timeRemaining);
    }
  }

  public void setData(@Nonnull final EntityPlayer player, @Nonnull final String identifier,
      final long creationDate, @Nonnull final List<InventorySlot> inventorySlots) {
    dataIdentifier = identifier;
    ownerName = player.getDisplayNameString();
    ownerUUID = player.getUniqueID();
    this.creationDate = creationDate;
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
    if (ownerUUID != null) {
      tagCompound.setUniqueId("OwnerUUID", ownerUUID);
    }
    tagCompound.setLong("CreationDate", creationDate);
    tagCompound.setTag("InventorySlots", nbtInventorySlots);

    return tagCompound;
  }

  private void doInspection(@Nonnull final EntityPlayer player, final boolean isCreative, final boolean isOwner, final long timeRemaining) {
    final ITextComponent textOwner = new TextComponentString(ownerName == null ? "-null-" : ownerName);
    textOwner.getStyle()
        .setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponentString(ownerUUID == null ? "-null-" : ownerUUID.toString())))
        .setColor(TextFormatting.AQUA);
    
    final ITextComponent textDuration = new TextComponentString(DurationFormatUtils.formatDurationWords(Math.abs(timeRemaining * 1000L), true, true));
    textDuration.getStyle()
        .setColor(TextFormatting.RED);
    
    final ITextComponent textSize = new TextComponentString(String.format("%d", inventorySlots.size()));
    textSize.getStyle()
        .setColor(TextFormatting.AQUA);
    
    ITextComponent textMessageToSend;
    if (isOwner) {
      if (timeRemaining <= 0L) {
        textMessageToSend = new TextComponentTranslation("chestedgravestones.chat.inspect.elapsed_yours",
            textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      } else if (!ChestedGravestonesConfig.OWNER_ONLY_COLLECTION) {
        textMessageToSend = new TextComponentTranslation("chestedgravestones.chat.inspect.delayed_yours",
            textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      } else {
        textMessageToSend = new TextComponentTranslation("chestedgravestones.chat.inspect.guarded_yours",
            textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      }
    } else {
      if (timeRemaining <= 0L) {
        textMessageToSend = new TextComponentTranslation("chestedgravestones.chat.inspect.elapsed_other",
            textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      } else if (!ChestedGravestonesConfig.OWNER_ONLY_COLLECTION) {
        textMessageToSend = new TextComponentTranslation("chestedgravestones.chat.inspect.delayed_other",
            textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GOLD);
      } else {
        textMessageToSend = new TextComponentTranslation("chestedgravestones.chat.inspect.guarded_other",
            textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GOLD);
      }
    }
    player.sendMessage(textMessageToSend);
    
    if ( isCreative
      && ( isOwner
        || timeRemaining <= 0L )) {
      textMessageToSend = new TextComponentTranslation("chestedgravestones.chat.inspect.survival_required",
          textOwner, textDuration, textSize );
      textMessageToSend.getStyle().setColor(TextFormatting.RED);
      player.sendMessage(textMessageToSend);
    }
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