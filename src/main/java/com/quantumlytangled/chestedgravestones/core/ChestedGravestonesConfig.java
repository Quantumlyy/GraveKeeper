package com.quantumlytangled.chestedgravestones.core;

import com.quantumlytangled.chestedgravestones.ChestedGravestones;
import com.quantumlytangled.chestedgravestones.compatability.CompatBaubles;
import com.quantumlytangled.chestedgravestones.compatability.CompatGalacticCraftCore;
import com.quantumlytangled.chestedgravestones.compatability.CompatTechGuns;
import com.quantumlytangled.chestedgravestones.util.InventoryHandler;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

public class ChestedGravestonesConfig {

  public static boolean IGNORE_KEEP_INVENTORY = false;

  public static int EXPIRE_TIME_SECONDS = 7200;
  public static boolean INSTANT_FOREIGN_COLLECTION = false;
  public static boolean OWNER_ONLY_COLLECTION = false;

  public static boolean KEEP_SOULBOUND = false;
  public static int KEEP_SOULBOUND_AMOUNT = 5;


  public static void onFMLpreInitialization(final File fileConfigDirectory) {

    loadConfig(new File(fileConfigDirectory, ChestedGravestones.MODID + ".yml"));

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

    EXPIRE_TIME_SECONDS = config
        .get("chest_collection", "expire_time", EXPIRE_TIME_SECONDS, String.join("\n", new String[] {
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

    config.save();
  }

}
