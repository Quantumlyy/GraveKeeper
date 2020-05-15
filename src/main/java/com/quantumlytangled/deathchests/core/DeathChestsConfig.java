package com.quantumlytangled.deathchests.core;

import com.quantumlytangled.deathchests.DeathChests;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

public class DeathChestsConfig {

    private static String stringConfigDirectory;
    private static File fileConfigDirectory;

    public static boolean isBaublesLoaded = false;

    public static boolean INSTANT_COLLECTION = false;
    public static int EXPIRE_TIME = 7200;

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

        isBaublesLoaded = Loader.isModLoaded("baubles");
    }

    public static void loadConfig(final File file) {
        final Configuration config = new Configuration(file);
        config.load();

        INSTANT_COLLECTION = config.get("chest_collection", "instant_collection", INSTANT_COLLECTION, "Whether other players should be able to instantly collect ones Death Chest").getBoolean(true);
        EXPIRE_TIME = config.get("chest_collection", "expire_time", EXPIRE_TIME, "Time in seconds after which other players will be able to collect ones chest").getInt(7200);

        config.save();
    }

}
