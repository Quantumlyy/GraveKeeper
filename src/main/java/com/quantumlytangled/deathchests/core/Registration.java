package com.quantumlytangled.deathchests.core;

import com.quantumlytangled.deathchests.DeathChests;
import com.quantumlytangled.deathchests.block.BlockDeathChest;
import com.quantumlytangled.deathchests.tile.TileDeathChest;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public final class Registration {

    public static Logger logger;

    public static Block blockDeathChest;

    public void preInitialize(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        blockDeathChest = new BlockDeathChest();
    }

    public void initialize(FMLInitializationEvent event)
    {
    }

    @SubscribeEvent
    public void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().register(blockDeathChest);
        GameRegistry.registerTileEntity(TileDeathChest.class, new ResourceLocation(DeathChests.MODID, "death_chest"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(@Nonnull LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof EntityPlayer && entity.isServerWorld())) return;
        Date tof = new Date();

        EntityPlayerMP playerEntity = (EntityPlayerMP) entity;
        String playerName = playerEntity.getDisplayNameString();
        UUID playerUUID = playerEntity.getUniqueID();
        NBTTagCompound nbtTag = playerEntity.writeToNBT(new NBTTagCompound());
        World world = playerEntity.world;

        double pX = playerEntity.posX;
        double pY = playerEntity.posY;
        double pZ = playerEntity.posZ;
        BlockPos deathPos = new BlockPos(pX, pY, pZ);

        String worldTimeHex = Long.toHexString(playerEntity.getServerWorld().getWorldTime());
        String timestamp = new SimpleDateFormat("dd-MM-yyyy-mm-HH-ss").format(tof);
        String identifier = playerName + "." + playerUUID + "." + worldTimeHex + "." + timestamp;
        String fileName = identifier + ".nbt";

        File dir = new File(playerEntity.server.getWorld(DimensionType.OVERWORLD.getId()).getSaveHandler().getWorldDirectory(), "deathchests_deathdata");
        if ((!dir.exists()) && (!dir.mkdir())) logger.error("Unable to create `deathchests_deathdata` folder.");

        File file = new File(dir, fileName);
        try {
            if (!file.exists()) {
                try (FileOutputStream output = new FileOutputStream(file)) {
                    CompressedStreamTools.writeCompressed(nbtTag, output);
                }
                try {
                    InventoryDeath invDeath = new InventoryDeath(playerEntity);
                    world.setBlockState(deathPos, blockDeathChest.getDefaultState());
                    TileDeathChest dChest = (TileDeathChest) world.getTileEntity(deathPos);
                    dChest.setData(identifier, playerName, playerUUID, tof, invDeath);
                    playerEntity.inventory.clear();
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to save player death data for " + playerName + "." + playerUUID, e);
        }
    }
}
