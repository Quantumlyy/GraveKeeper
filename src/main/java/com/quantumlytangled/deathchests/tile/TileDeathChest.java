package com.quantumlytangled.deathchests.tile;

import com.quantumlytangled.deathchests.core.DeathHandler;
import com.quantumlytangled.deathchests.core.InventoryDeath;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
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

    public void processRight(EntityPlayer player, World world, BlockPos pos) {
        if (checkInvalidity(player)) return;
        if (player.isCreative()) processCreativeInspect(player, world, pos);
        else processItemReturn(player, world, pos);
    }

    public void processLeft(EntityPlayer player, World world, BlockPos pos) {
        if (checkInvalidity(player)) return;
        if (player.isCreative()) processCreativeItemReturn(player, world, pos);
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

        NBTTagCompound contents = (NBTTagCompound) nbt.getTag("Contents");
        NBTTagList mainInv = contents.getTagList("MainInventory", Constants.NBT.TAG_COMPOUND);
        NBTTagList offhandInv = contents.getTagList("OffHandInventory", Constants.NBT.TAG_COMPOUND);
        NBTTagList armorInv = contents.getTagList("ArmorInventory", Constants.NBT.TAG_COMPOUND);

        this.dataIdentifier = nbt.getString("DataIdentifier");
        this.ownerName = nbt.getString("OwnerName");
        this.ownerUUID = nbt.getUniqueId("OwnerUUID");
        this.creationDate = nbt.getLong("CreationDate");

        DeathHandler.readInventoryContentsFromNBTTagList(mainInv, this.contents.mainInventory);
        DeathHandler.readInventoryContentsFromNBTTagList(offhandInv, this.contents.offHandInventory);
        DeathHandler.readInventoryContentsFromNBTTagList(armorInv, this.contents.armorInventory);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        NBTTagCompound contents = new NBTTagCompound();

        NBTTagList mainInv = new NBTTagList();
        NBTTagList offhandInv = new NBTTagList();
        NBTTagList armorInv = new NBTTagList();

        contents.setTag("MainInventory", DeathHandler.formNBTTagListFromInventoryContents(mainInv, this.contents.mainInventory));
        contents.setTag("OffHandInventory", DeathHandler.formNBTTagListFromInventoryContents(offhandInv, this.contents.offHandInventory));
        contents.setTag("ArmorInventory", DeathHandler.formNBTTagListFromInventoryContents(armorInv, this.contents.armorInventory));

        nbt.setString("DataIdentifier", this.dataIdentifier);
        nbt.setString("OwnerName", this.ownerName);
        nbt.setUniqueId("OwnerUUID", this.ownerUUID);
        nbt.setLong("CreationDate", this.creationDate);
        nbt.setTag("Contents", contents);

        return nbt;
    }

    private boolean checkInvalidity(EntityPlayer player) {
        return !((ownerUUID == player.getUniqueID()) || player.isCreative());
    }

    private void processCreativeInspect(EntityPlayer player, World world, BlockPos pos) {
        player.sendMessage(new TextComponentString(String.join("\n",
                "Name.UUID - " + ownerName + "." + ownerUUID,
                "DeathData file name - " + dataIdentifier
        )));
    }

    private void processCreativeItemReturn(EntityPlayer player, World world, BlockPos pos) {
        for (NonNullList<ItemStack> inventory : contents.allInventories) {
            for (ItemStack item : inventory) {
                world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), item.copy()));
            }
        }
    }

    private void processItemReturn(EntityPlayer player, World world, BlockPos pos) {
        List<ItemStack> overflow = new ArrayList<>();

        DeathHandler.restoreVanillaInventory(contents.mainInventory, player.inventory.mainInventory, overflow);
        DeathHandler.restoreVanillaInventory(contents.armorInventory, player.inventory.armorInventory, overflow);
        DeathHandler.restoreVanillaInventory(contents.offHandInventory, player.inventory.offHandInventory, overflow);

        for (final ItemStack item : overflow) {
            if (player.inventory.addItemStackToInventory(item.copy())) continue;
            world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), item.copy()));
        }

        world.removeTileEntity(pos);
        world.setBlockToAir(pos);
    }
}
