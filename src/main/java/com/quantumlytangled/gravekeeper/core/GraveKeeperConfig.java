package com.quantumlytangled.gravekeeper.core;

import com.quantumlytangled.gravekeeper.GraveKeeper;
import com.quantumlytangled.gravekeeper.compatability.CompatBaubles;
import com.quantumlytangled.gravekeeper.compatability.CompatGalacticCraftCore;
import com.quantumlytangled.gravekeeper.compatability.CompatTechGuns;
import com.quantumlytangled.gravekeeper.util.InventoryHandler;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

public class GraveKeeperConfig {

  public static boolean IGNORE_KEEP_INVENTORY = false;
  public static boolean DEBUG_LOGS = false;

  public static int EXPIRE_TIME_SECONDS = 7200;
  public static boolean INSTANT_FOREIGN_COLLECTION = false;
  public static boolean OWNER_ONLY_COLLECTION = false;

  public static boolean KEEP_SOULBOUND = false;
  public static int KEEP_SOULBOUND_AMOUNT = 5;

  public static int SEARCH_MIN_ALTITUDE = 0;
  public static int SEARCH_RADIUS_ABOVE_M = 10;
  public static int SEARCH_RADIUS_BELOW_M = -10;
  public static int SEARCH_RADIUS_HORIZONTAL_M = 5;
  public static int SPAWN_DIMENSION_ID = 0;
  public static int USE_BED_OR_SPAWN_LOCATION_BELOW_Y = 0;

  public static void onFMLpreInitialization(final File fileConfigDirectory) {

    loadConfig(new File(fileConfigDirectory, GraveKeeper.MODID + ".yml"));

    if (Loader.isModLoaded("baubles")) {
      InventoryHandler.compatBaubles = CompatBaubles.getInstance();
    }
    if (Loader.isModLoaded("galacticraftcore")) {
      InventoryHandler.compatGalacticCraft = CompatGalacticCraftCore.getInstance();
    }
    if (Loader.isModLoaded("techguns")) {
      InventoryHandler.compatTechGuns = CompatTechGuns.getInstance();
    }
  }

  public static void loadConfig(final File file) {
    final Configuration config = new Configuration(file);
    config.load();

    IGNORE_KEEP_INVENTORY = config
        .get("general", "ignore_keep_inventory", IGNORE_KEEP_INVENTORY,
            "Whether the chests should still spawn when keepInventory is enabled")
        .getBoolean(false);
    DEBUG_LOGS = config
        .get("general", "enable_debug_logs", DEBUG_LOGS,
            "Enable console logs for debugging purpose")
        .getBoolean(false);

    EXPIRE_TIME_SECONDS = config
        .get("general", "expire_time", EXPIRE_TIME_SECONDS, String.join("\n", new String[] {
            "Time in seconds after which other players will be able to collect ones grave",
            "Use 0 to have an instant expiration and anyone is able to pick up the grave instantly",
            "Use -1 or lower to remove expiration and only the owner will ever be able to pick up the chest"
        }))
        .getInt(7200);
    EXPIRE_TIME_SECONDS = Math.max(-1, EXPIRE_TIME_SECONDS);
    INSTANT_FOREIGN_COLLECTION = EXPIRE_TIME_SECONDS == 0;
    OWNER_ONLY_COLLECTION = EXPIRE_TIME_SECONDS == -1;

    KEEP_SOULBOUND = config
        .get("soulbound", "keep", KEEP_SOULBOUND,
            "Should soulbound items be kept in players inventory")
        .getBoolean(true);
    KEEP_SOULBOUND_AMOUNT = config
        .get("soulbound", "amount", KEEP_SOULBOUND_AMOUNT,
            "The amount of soulbound items should be kept in player inventory, remaining will go into the chest")
        .getInt(5);

    SEARCH_MIN_ALTITUDE = Math.abs(config
        .get("grave_location", "search_min_altitude", SEARCH_MIN_ALTITUDE,
            "Force a minimum altitude before looking for a free spot (this also applies to home/spawn location).")
        .getInt(0));
    SEARCH_RADIUS_ABOVE_M = Math.abs(config
        .get("grave_location", "search_radius_above_m", SEARCH_RADIUS_ABOVE_M,
            "How far to search around vertically above for a free spot to place the grave.")
        .getInt(10));
    SEARCH_RADIUS_BELOW_M = Math.abs(config
        .get("grave_location", "search_radius_below_m", SEARCH_RADIUS_BELOW_M,
            "How far to search around vertically below for a free spot to place the grave.")
        .getInt(-10));
    SEARCH_RADIUS_HORIZONTAL_M = config
        .get("grave_location", "search_radius_horizontal_m", SEARCH_RADIUS_HORIZONTAL_M,
            "How far to search around horizontally for a free spot to place the grave.")
        .getInt(5);
    SPAWN_DIMENSION_ID = config
        .get("grave_location", "spawn_dimension", SPAWN_DIMENSION_ID,
            "Defines which spawn dimension to use when player has no bed set.")
        .getInt(0);
    USE_BED_OR_SPAWN_LOCATION_BELOW_Y = config
        .get("grave_location", "use_bed_or_spawn_location_below_y", USE_BED_OR_SPAWN_LOCATION_BELOW_Y, String.join("\n", new String[] {
            "Use Bed or spawn location when death happens with Y below this value.",
            "Use -1000 or lower to disable it, use 1000 or higher to force permanently.",
            "We first check bed in current dimension, then bed in spawn dimension, then we use the actual spawn.",
            "Note: default spawn is center of the world at Y = 0." }))
        .getInt(0);
    if (USE_BED_OR_SPAWN_LOCATION_BELOW_Y <= -1000) {
      USE_BED_OR_SPAWN_LOCATION_BELOW_Y = Integer.MIN_VALUE;
    } else if (USE_BED_OR_SPAWN_LOCATION_BELOW_Y >= 1000) {
      USE_BED_OR_SPAWN_LOCATION_BELOW_Y = Integer.MAX_VALUE;
    }
    
    config.save();
  }

}
