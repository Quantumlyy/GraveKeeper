package com.quantumlytangled.deathchests.core;

import com.quantumlytangled.deathchests.DeathChests;
import com.quantumlytangled.deathchests.block.BlockDeathChest;
import com.quantumlytangled.deathchests.compatability.CompatBaubles;
import com.quantumlytangled.deathchests.compatability.CompatGalacticCraftCore;
import com.quantumlytangled.deathchests.compatability.CompatTechguns;
import com.quantumlytangled.deathchests.tile.TileDeathChest;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class Registration {

    public static Logger logger;

    public static Block blockDeathChest;

    public void preInitialize(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        DeathChestsConfig.onFMLpreInitialization(event.getModConfigurationDirectory().getAbsolutePath());

        blockDeathChest = new BlockDeathChest();
    }

    public void initialize(FMLInitializationEvent event) {
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
        ZonedDateTime tof = ZonedDateTime.now(ZoneOffset.UTC);

        EntityPlayerMP playerEntity = (EntityPlayerMP) entity;
        String playerName = playerEntity.getDisplayNameString();
        UUID playerUUID = playerEntity.getUniqueID();
        World world = playerEntity.world;

        double pX = playerEntity.posX;
        double pY = playerEntity.posY;
        double pZ = playerEntity.posZ;
        BlockPos deathPos = new BlockPos(pX, pY, pZ);

        String timestamp = tof.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.n"));
        String identifier = playerName + "." + playerUUID + "." + timestamp;

        if (playerEntity.world.getGameRules().getBoolean("keepInventory") && !DeathChestsConfig.IGNORE_KEEP_INVENTORY) return;

        InventoryDeath invDeath = new InventoryDeath();
        world.setBlockState(deathPos, blockDeathChest.getDefaultState());

        final TileEntity tChest = world.getTileEntity(deathPos);
        if (!(tChest instanceof TileDeathChest)) return;
        final TileDeathChest dChest = (TileDeathChest) tChest;

        invDeath.formInventory(playerEntity);
        dChest.setData(playerEntity, identifier, tof, invDeath);

        playerEntity.inventory.clear();
        if (DeathChestsConfig.isBaublesLoaded) CompatBaubles.clearInventory(playerEntity);
        if (DeathChestsConfig.isGalacticCraftCoreLoaded) CompatGalacticCraftCore.clearInventory(playerEntity);
        if (DeathChestsConfig.isTechgunsLoaded) CompatTechguns.clearInventory(playerEntity);

        playerEntity.sendMessage(new TextComponentString(String.format("Chest placed at x: %s; y: %s; z: %s", (int) pX, (int) pY, (int) pZ)));
        logger.info(String.format("Generated DeathChest for %s(%s) at x: %s; y: %s; z: %s", playerName, playerUUID, (int) pX, (int) pY, (int) pZ));
    }
}
