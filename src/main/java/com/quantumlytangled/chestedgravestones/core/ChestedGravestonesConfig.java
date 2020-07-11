package com.quantumlytangled.chestedgravestones.core;

import com.quantumlytangled.chestedgravestones.ChestedGravestones;
import com.quantumlytangled.chestedgravestones.compatability.CompatBaubles;
import com.quantumlytangled.chestedgravestones.compatability.CompatGalacticCraftCore;
import com.quantumlytangled.chestedgravestones.compatability.ICompatInventory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

public class ChestedGravestonesConfig {

    private static String stringConfigDirectory;
    private static File fileConfigDirectory;

    public static ICompatInventory isBaublesLoaded = null;
    public static ICompatInventory isGalacticCraftCoreLoaded = null;

    public static boolean IGNORE_KEEP_INVENTORY = false;

    public static int EXPIRE_TIME = 7200;
    public static boolean INSTANT_COLLECTION = false;

    public static boolean KEEP_SOULBOUND = false;
    public static int KEEP_SOULBOUND_AMOUNT = 5;

    public static boolean getExpiredStatus(long creationDate) {
        return (creationDate + EXPIRE_TIME) < ZonedDateTime.now(ZoneOffset.UTC).getLong(ChronoField.INSTANT_SECONDS);
    }

    public static void onFMLpreInitialization(final String stringConfigDirectory) {
        ChestedGravestonesConfig.stringConfigDirectory = stringConfigDirectory;

        fileConfigDirectory = new File(stringConfigDirectory, ChestedGravestones.MODID);
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

        EXPIRE_TIME = config
                .get("chest_collection", "expire_time", EXPIRE_TIME, String.join("\n", new String[]{
                        "Time in seconds after which other players will be able to collect ones chest",
                        "If -1 is passed EXPIRE_TIME will be disable any anyone will be able to pick up the chest instantly"
                }))
                .getInt(7200);
        INSTANT_COLLECTION = EXPIRE_TIME == -1;

        KEEP_SOULBOUND = config
                .get("soulbound", "keep", KEEP_SOULBOUND, "Should soulbound items be kept in players inventory")
                .getBoolean(true);
        KEEP_SOULBOUND_AMOUNT = config
                .get("soulbound", "amount", KEEP_SOULBOUND_AMOUNT, "The amount of items soulbound should affect and be saved")
                .getInt(5);

        config.save();
    }

}
