package com.quantumlytangled.deathchests.tile;

import com.quantumlytangled.deathchests.core.DeathChestsConfig;
import com.quantumlytangled.deathchests.core.InventoryDeath;
import com.quantumlytangled.deathchests.core.InventoryDeathSlot;
import com.quantumlytangled.deathchests.core.InventoryType;
import com.quantumlytangled.deathchests.util.DeathHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileDeathChest extends TileEntity {

    private String dataIdentifier = null;

    private String ownerName = null;
    private UUID ownerUUID = null;

    private long creationDate = 0;

    private InventoryDeath contents = new InventoryDeath();

    public TileDeathChest() {
        super();
    }

    public void processInteraction(EntityPlayer player, World world, BlockPos pos) {
        if (player.isCreative()) processCreativeInspect(player, world, pos);
        else if (ownerUUID.equals(player.getUniqueID())) processItemReturn(player, world, pos);
        else if ((DeathChestsConfig.getExpiredStatus(creationDate) || DeathChestsConfig.INSTANT_COLLECTION) && !ownerUUID.equals(player.getUniqueID())) processDropItemReturn(player, world, pos);
    }

    public void processBreak(EntityPlayer player, World world, BlockPos pos) {
        if (player.isCreative()) processDropItemReturn(player, world, pos);
    }

    public void setData(
            @Nonnull EntityPlayer player,
            @Nonnull String identifier,
            @Nonnull ZonedDateTime creation,
            @Nonnull InventoryDeath inventory) {
        dataIdentifier = identifier;
        ownerName = player.getDisplayNameString();
        ownerUUID = player.getUniqueID();
        creationDate = creation.getLong(ChronoField.INSTANT_SECONDS);
        contents = inventory;
        this.markDirty();
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (!nbt.hasKey("Contents", Constants.NBT.TAG_LIST)) return;

        NBTTagList contents = (NBTTagList) nbt.getTag("Contents");

        for (int i = 0; i < contents.tagCount(); i++) {
            NBTTagCompound tag = contents.getCompoundTagAt(i);
            this.contents.inventory.add(new InventoryDeathSlot(
                    new ItemStack(tag),
                    tag.getByte("Slot"),
                    InventoryType.valueOf(tag.getString("Type"))));
        }

        this.dataIdentifier = nbt.getString("DataIdentifier");
        this.ownerName = nbt.getString("OwnerName");
        this.ownerUUID = nbt.getUniqueId("OwnerUUID");
        this.creationDate = nbt.getLong("CreationDate");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        NBTTagList contents = new NBTTagList();

        for (InventoryDeathSlot slot : this.contents.inventory) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("Slot", (byte) slot.slot);
            entry.setString("Type", slot.type.toString());
            contents.appendTag(slot.content.writeToNBT(entry));
        }

        nbt.setString("DataIdentifier", this.dataIdentifier);
        nbt.setString("OwnerName", this.ownerName);
        nbt.setUniqueId("OwnerUUID", this.ownerUUID);
        nbt.setLong("CreationDate", this.creationDate);
        nbt.setTag("Contents", contents);

        return nbt;
    }

    private void processCreativeInspect(EntityPlayer player, World world, BlockPos pos) {
        player.sendMessage(new TextComponentString(String.join("\n",
                "Name.UUID - " + ownerName + "." + ownerUUID,
                "DeathData file name - " + dataIdentifier
        )));
    }

    private void processDropItemReturn(EntityPlayer player, World world, BlockPos pos) {
        for (InventoryDeathSlot inventory : contents.inventory)
            world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), inventory.content.copy()));
    }

    private void processItemReturn(EntityPlayer _player, World world, BlockPos pos) {
        List<ItemStack> overflow = new ArrayList<>();
        EntityPlayerMP player = (EntityPlayerMP) _player;

        for (InventoryDeathSlot slot : this.contents.inventory) {
            switch(slot.type) {
                case MAIN:
                    DeathHandler.restoreInvSlot(slot, player.inventory.mainInventory, overflow);
                    break;
                case ARMOUR:
                    DeathHandler.restoreInvSlot(slot, player.inventory.armorInventory, overflow);
                    break;
                case OFFHAND:
                    DeathHandler.restoreInvSlot(slot, player.inventory.offHandInventory, overflow);
                    break;
                case BAUBLES:
                    if (DeathChestsConfig.isBaublesLoaded == null) break;
                    if (DeathChestsConfig.isBaublesLoaded.isSlotEmpty(slot.slot, player)) DeathChestsConfig.isBaublesLoaded.setItem(slot.slot, slot.content, player);
                    else overflow.add(slot.content);
                    break;
                case GCC:
                    if (DeathChestsConfig.isGalacticCraftCoreLoaded == null) break;
                    if (DeathChestsConfig.isGalacticCraftCoreLoaded.isSlotEmpty(slot.slot, player)) DeathChestsConfig.isGalacticCraftCoreLoaded.setItem(slot.slot, slot.content, player);
                    else overflow.add(slot.content);
                    break;
                case TECHGUNS:
                    if (DeathChestsConfig.isTechgunsLoaded == null) break;
                    if (DeathChestsConfig.isTechgunsLoaded.isSlotEmpty(slot.slot, player)) DeathChestsConfig.isTechgunsLoaded.setItem(slot.slot, slot.content, player);
                    else overflow.add(slot.content);
                    break;
            }
        }

        for (final ItemStack item : overflow) {
            if (player.inventory.addItemStackToInventory(item.copy())) continue;
            world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), item.copy()));
        }

        world.removeTileEntity(pos);
        world.setBlockToAir(pos);
    }
}
