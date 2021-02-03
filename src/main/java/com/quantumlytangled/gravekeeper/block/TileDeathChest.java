package com.quantumlytangled.gravekeeper.block;

import com.quantumlytangled.gravekeeper.GraveKeeperConfig;
import com.quantumlytangled.gravekeeper.core.CreationDate;
import com.quantumlytangled.gravekeeper.util.InventorySlot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

import org.apache.commons.lang3.time.DurationFormatUtils;

// TODO: All of this
public class TileDeathChest extends TileEntity {
  
  private String dataIdentifier = "";
  
  private String ownerName = "";
  private UUID ownerUUID = null;
  
  private long creationDate = 0;
  
  private List<InventorySlot> inventorySlots = new ArrayList<>();
  
  public TileDeathChest() {
    super(TileEntityType.A);
  }
  
  public void processInteraction(@Nonnull final ServerPlayerEntity player) {
    final boolean isCreative = player.isCreative();
    final boolean isOwner = ownerUUID == null
                            || (ownerUUID.getLeastSignificantBits() == 0L && ownerUUID.getMostSignificantBits() == 0L)
                            || player.getUniqueID().equals(ownerUUID);
    final long timeRemaining = CreationDate.getRemainingSeconds(creationDate);
    if ( isCreative
         || player.isSneaking() ) {
      doInspection(player, isCreative, isOwner, timeRemaining);
      
    } else if (isOwner) {
      doReturnToOwner(player);
      
    } else if ( (GraveKeeperConfig.INSTANT_FOREIGN_COLLECTION
                 || timeRemaining <= 0L )
                && !GraveKeeperConfig.OWNER_ONLY_COLLECTION ) {
      doDropContent();
      
    } else {
      doInspection(player, false, false, timeRemaining);
    }
  }
  
  public void setData(@Nonnull final ServerPlayerEntity player, @Nonnull final String identifier,
                      final long creationDate, @Nonnull final List<InventorySlot> inventorySlots) {
    dataIdentifier = identifier;
    ownerName = player.getDisplayNameString();
    ownerUUID = player.getUniqueID();
    this.creationDate = creationDate;
    this.inventorySlots = inventorySlots.stream()
                                        .filter(inventorySlot -> !inventorySlot.isCharmed
                                                                 && !inventorySlot.isSoulbound )
                                        .collect(Collectors.toList());
    markDirty();
  }
  
  @Override
  public void readFromNBT(@Nonnull CompoundNBT tagCompound) {
    super.readFromNBT(tagCompound);
    
    final NBTTagList nbtInventorySlots = tagCompound.getTagList("InventorySlots", Constants.NBT.TAG_COMPOUND);
    for (int index = 0; index < nbtInventorySlots.tagCount(); index++) {
      final NBTTagCompound nbtInventorySlot = nbtInventorySlots.getCompoundTagAt(index);
      inventorySlots.add(new InventorySlot(nbtInventorySlot));
    }
    
    dataIdentifier = tagCompound.getString("DataIdentifier");
    ownerName = tagCompound.getString("OwnerName");
    ownerUUID = tagCompound.getUniqueId("OwnerUUID");
    creationDate = tagCompound.getLong("CreationDate");
  }
  
  @Nonnull
  @Override
  public CompoundNBT writeToNBT(@Nonnull final CompoundNBT tagCompound) {
    super.writeToNBT(tagCompound);
    
    final ListNBT nbtInventorySlots = new ListNBT();
    for (final InventorySlot inventorySlot : inventorySlots) {
      nbtInventorySlots.add(inventorySlot.writeToNBT());
    }
    
    tagCompound.putString("DataIdentifier", dataIdentifier);
    tagCompound.putString("OwnerName", ownerName);
    if (ownerUUID != null) {
      tagCompound.putUniqueId("OwnerUUID", ownerUUID);
    }
    tagCompound.putLong("CreationDate", creationDate);
    tagCompound.put("InventorySlots", nbtInventorySlots);
    
    return tagCompound;
  }
  
  private void doInspection(@Nonnull final ServerPlayerEntity player, final boolean isCreative, final boolean isOwner, final long timeRemaining) {
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
        textMessageToSend = new TextComponentTranslation("gravekeeper.chat.inspect.elapsed_yours",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      } else if (!GraveKeeperConfig.OWNER_ONLY_COLLECTION) {
        textMessageToSend = new TextComponentTranslation("gravekeeper.chat.inspect.delayed_yours",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      } else {
        textMessageToSend = new TextComponentTranslation("gravekeeper.chat.inspect.guarded_yours",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      }
    } else {
      if (timeRemaining <= 0L) {
        textMessageToSend = new TextComponentTranslation("gravekeeper.chat.inspect.elapsed_other",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GREEN);
      } else if (!GraveKeeperConfig.OWNER_ONLY_COLLECTION) {
        textMessageToSend = new TextComponentTranslation("gravekeeper.chat.inspect.delayed_other",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GOLD);
      } else {
        textMessageToSend = new TextComponentTranslation("gravekeeper.chat.inspect.guarded_other",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().setColor(TextFormatting.GOLD);
      }
    }
    player.sendMessage(textMessageToSend);
    
    if ( isCreative
         && ( isOwner
              || timeRemaining <= 0L )) {
      textMessageToSend = new TextComponentTranslation("gravekeeper.chat.inspect.survival_required",
                                                       textOwner, textDuration, textSize );
      textMessageToSend.getStyle().setColor(TextFormatting.RED);
      player.sendMessage(textMessageToSend);
    }
  }
  
  protected void doDropContent() {
    for (InventorySlot inventory : inventorySlots) {
      final ItemEntity entityItem = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, inventory.itemStack);
      world.addEntity(entityItem);
    }
  }
  
  private void doReturnToOwner(@Nonnull final ServerPlayerEntity player) {
    final List<ItemStack> overflow = InventoryHandler.restoreOrOverflow(player, inventorySlots, false);
    
    for (final ItemStack itemStack : overflow) {
      if (player.inventory.addItemStackToInventory(itemStack.copy())) {
        continue;
      }
      world.addEntity(new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack));
    }
    
    world.removeTileEntity(pos);
    world.setBlockToAir(pos);
  }
}
