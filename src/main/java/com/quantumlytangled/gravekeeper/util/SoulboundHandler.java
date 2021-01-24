package com.quantumlytangled.gravekeeper.util;

import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.quantumlytangled.gravekeeper.GraveKeeperConfig;

public class SoulboundHandler {

  public static boolean isSoulbound(@Nonnull final ItemStack itemStack) {
    // check for enchantments
    final Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
    for (final Enchantment enchantment : enchantments.keySet()) {
      if ( enchantment != null
        && ( GraveKeeperConfig.ANY_ENCHANT_IS_SOULBOUND
          || GraveKeeperConfig.SOULBOUND_ENCHANTMENTS.contains(enchantment) ) ) {
        return true;
      }
    }

    // check for NBT tags
    final NBTTagCompound tagCompound = itemStack.getTagCompound();
    if (tagCompound != null) {
      for (final NBTTagCompound tagElement : GraveKeeperConfig.SOULBOUND_TAGS) {
        boolean isSoulbound = true;
        for (final String tagKey : tagElement.getKeySet()) {
          if ( !tagCompound.hasKey(tagKey)
            || !tagCompound.getTag(tagKey).equals(tagElement.getTag(tagKey)) ) {
            isSoulbound = false;
            break;
          }
        }

        if (isSoulbound) {
          return true;
        }
      }
    }

    return false;
  }

}