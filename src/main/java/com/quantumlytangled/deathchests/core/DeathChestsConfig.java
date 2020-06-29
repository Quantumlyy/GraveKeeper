package com.quantumlytangled.deathchests.core;

import com.quantumlytangled.deathchests.DeathChests;
import com.quantumlytangled.deathchests.compatability.CompatBaubles;
import com.quantumlytangled.deathchests.compatability.CompatGalacticCraftCore;
import com.quantumlytangled.deathchests.compatability.ICompatInventory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

public class DeathChestsConfig {

    private static String stringConfigDirectory;
    private static File fileConfigDirectory;

    public static ICompatInventory isBaublesLoaded = null;
    public static ICompatInventory isGalacticCraftCoreLoaded = null;

    public static boolean IGNORE_KEEP_INVENTORY = false;

    public static boolean INSTANT_COLLECTION = false;
    public static int EXPIRE_TIME = 7200;

    public static boolean KEEP_SOULBOUND = false;
    public static int KEEP_SOULBOUND_AMOUNT = 5;

    public static boolean getExpiredStatus(long creationDate) {
        return (creationDate + EXPIRE_TIME) < ZonedDateTime.now(ZoneOffset.UTC).getLong(ChronoField.INSTANT_SECONDS);
    }

    public static void onFMLpreInitialization(final String stringConfigDirectory) {
        DeathChestsConfig.stringConfigDirectory = stringConfigDirectory;

        fileConfigDirectory = new File(stringConfigDirectory, DeathChests.MODID);
        //noinspection ResultOfMethodCallIgnored
        fileConfigDirectory.mkdir();
        if (!fileConfigDirectory.isDirectory()) {
            throw new RuntimeException(String.format("Unable to create config directory %s",
                    fileConfigDirectory));
        }

        loadConfig(new File(fileConfigDirectory, "config.yml"));

        if (Loader.isModLoaded("baubles")) isBaublesLoaded = CompatBaubles.INSTANCE();
        if (Loader.isModLoaded("galacticraftcore")) isGalacticCraftCoreLoaded = CompatGalacticCraftCore.INSTANCE();
    }

    public static void loadConfig(final File file) {
        final Configuration config = new Configuration(file);
        config.load();

        IGNORE_KEEP_INVENTORY = config
                .get("general", "ignore_keep_inventory", IGNORE_KEEP_INVENTORY, "Whether the chests should still spawn on keepInventory")
                .getBoolean(false);

        INSTANT_COLLECTION = config
                .get("chest_collection", "instant_collection", INSTANT_COLLECTION, "Whether other players should be able to instantly collect ones Death Chest")
                .getBoolean(true);
        EXPIRE_TIME = config
                .get("chest_collection", "expire_time", EXPIRE_TIME, "Time in seconds after which other players will be able to collect ones chest")
                .getInt(7200);

        KEEP_SOULBOUND = config
                .get("soulbound", "keep", KEEP_SOULBOUND, "Should soulbound items be kept in players inventory")
                .getBoolean(true);
        KEEP_SOULBOUND_AMOUNT = config
                .get("soulbound", "amount", KEEP_SOULBOUND_AMOUNT, "The amount of items soulbound should affect and be saved")
                .getInt(5);

        config.save();
    }

}
