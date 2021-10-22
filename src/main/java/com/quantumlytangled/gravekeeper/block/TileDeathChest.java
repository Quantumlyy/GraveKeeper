package com.quantumlytangled.gravekeeper.block;

import com.quantumlytangled.gravekeeper.GraveKeeperConfig;
import com.quantumlytangled.gravekeeper.core.CreationDate;
import com.quantumlytangled.gravekeeper.util.InventorySlot;
import com.quantumlytangled.gravekeeper.core.InventoryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

import net.minecraftforge.common.util.Constants;

import org.apache.commons.lang3.time.DurationFormatUtils;

// TODO: All of this
public class TileDeathChest extends TileEntity {
  
  private String dataIdentifier = "";
  
  private String ownerName = "";
  private UUID ownerUUID = null;
  
  private long creationDate = 0;
  
  private List<InventorySlot> inventorySlots = new ArrayList<>();
  
  public TileDeathChest() {
    super(TileEntityType.BED);
  }
  
  public void processInteraction(@Nonnull final ServerPlayerEntity player) {
    final boolean isCreative = player.isCreative();
    final boolean isOwner = ownerUUID == null
                            || (ownerUUID.getLeastSignificantBits() == 0L && ownerUUID.getMostSignificantBits() == 0L)
                            || player.getUUID().equals(ownerUUID);
    final long timeRemaining = CreationDate.getRemainingSeconds(creationDate);
    if ( isCreative
         || player.isShiftKeyDown() ) {
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
    ownerName = player.getDisplayName().toString();
    ownerUUID = player.getUUID();
    this.creationDate = creationDate;
    this.inventorySlots = inventorySlots.stream()
                                        .filter(inventorySlot -> !inventorySlot.isCharmed
                                                                 && !inventorySlot.isSoulbound )
                                        .collect(Collectors.toList());
    setChanged();
  }
  
  @Override
  public void load(@Nonnull BlockState state, @Nonnull CompoundNBT tagCompound) {
    super.load(state, tagCompound);
    
    final ListNBT nbtInventorySlots = tagCompound.getList("InventorySlots", Constants.NBT.TAG_COMPOUND);
    for (int index = 0; index < nbtInventorySlots.size(); index++) {
      final CompoundNBT nbtInventorySlot = nbtInventorySlots.getCompound(index);
      inventorySlots.add(new InventorySlot(nbtInventorySlot));
    }
    
    dataIdentifier = tagCompound.getString("DataIdentifier");
    ownerName = tagCompound.getString("OwnerName");
    ownerUUID = tagCompound.getUUID("OwnerUUID");
    creationDate = tagCompound.getLong("CreationDate");
  }
  
  @Nonnull
  @Override
  public CompoundNBT save(@Nonnull final CompoundNBT tagCompound) {
    super.save(tagCompound);
    
    final ListNBT nbtInventorySlots = new ListNBT();
    for (final InventorySlot inventorySlot : inventorySlots) {
      nbtInventorySlots.add(inventorySlot.writeToNBT());
    }
    
    tagCompound.putString("DataIdentifier", dataIdentifier);
    tagCompound.putString("OwnerName", ownerName);
    if (ownerUUID != null) {
      tagCompound.putUUID("OwnerUUID", ownerUUID);
    }
    tagCompound.putLong("CreationDate", creationDate);
    tagCompound.put("InventorySlots", nbtInventorySlots);
    
    return tagCompound;
  }
  
  private void doInspection(@Nonnull final ServerPlayerEntity player, final boolean isCreative, final boolean isOwner, final long timeRemaining) {
    final ITextComponent textOwner = new StringTextComponent(ownerName == null ? "-null-" : ownerName);
    textOwner.getStyle()
             .withHoverEvent(new HoverEvent(Action.SHOW_TEXT, new StringTextComponent(ownerUUID == null ? "-null-" : ownerUUID.toString())))
             .withColor(TextFormatting.AQUA);
    
    final ITextComponent textDuration = new StringTextComponent(DurationFormatUtils.formatDurationWords(Math.abs(timeRemaining * 1000L), true, true));
    textDuration.getStyle()
                .withColor(TextFormatting.RED);
    
    final ITextComponent textSize = new StringTextComponent(String.format("%d", inventorySlots.size()));
    textSize.getStyle()
            .withColor(TextFormatting.AQUA);
    
    ITextComponent textMessageToSend;
    if (isOwner) {
      if (timeRemaining <= 0L) {
        textMessageToSend = new TranslationTextComponent("gravekeeper.chat.inspect.elapsed_yours",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().withColor(TextFormatting.GREEN);
      } else if (!GraveKeeperConfig.OWNER_ONLY_COLLECTION) {
        textMessageToSend = new TranslationTextComponent("gravekeeper.chat.inspect.delayed_yours",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().withColor(TextFormatting.GREEN);
      } else {
        textMessageToSend = new TranslationTextComponent("gravekeeper.chat.inspect.guarded_yours",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().withColor(TextFormatting.GREEN);
      }
    } else {
      if (timeRemaining <= 0L) {
        textMessageToSend = new TranslationTextComponent("gravekeeper.chat.inspect.elapsed_other",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().withColor(TextFormatting.GREEN);
      } else if (!GraveKeeperConfig.OWNER_ONLY_COLLECTION) {
        textMessageToSend = new TranslationTextComponent("gravekeeper.chat.inspect.delayed_other",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().withColor(TextFormatting.GOLD);
      } else {
        textMessageToSend = new TranslationTextComponent("gravekeeper.chat.inspect.guarded_other",
                                                         textOwner, textDuration, textSize );
        textMessageToSend.getStyle().withColor(TextFormatting.GOLD);
      }
    }
    player.sendMessage(textMessageToSend, Util.NIL_UUID);
    
    if ( isCreative
         && ( isOwner
              || timeRemaining <= 0L )) {
      textMessageToSend = new TranslationTextComponent("gravekeeper.chat.inspect.survival_required",
                                                       textOwner, textDuration, textSize );
      textMessageToSend.getStyle().withColor(TextFormatting.RED);
      player.sendMessage(textMessageToSend, Util.NIL_UUID);
    }
  }
  
  protected void doDropContent() {
    for (InventorySlot inventory : inventorySlots) {
      final ItemEntity entityItem = new ItemEntity(world, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, inventory.itemStack);
      world.addEntity(entityItem);
    }
  }
  
  private void doReturnToOwner(@Nonnull final ServerPlayerEntity player) {
    final List<ItemStack> overflow = InventoryHandler.restoreOrOverflow(player, inventorySlots, false);
    
    for (final ItemStack itemStack : overflow) {
      if (player.inventory.add(itemStack.copy())) {
        continue;
      }
      world.addEntity(new ItemEntity(world, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, itemStack));
    }
    
    world.removeTileEntity(worldPosition);
    world.setBlockToAir(worldPosition);
  }
}
