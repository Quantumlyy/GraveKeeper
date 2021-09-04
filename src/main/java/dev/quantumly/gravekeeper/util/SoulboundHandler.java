package dev.quantumly.gravekeeper.util;

import dev.quantumly.gravekeeper.GraveKeeperConfig;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class SoulboundHandler {
  public static boolean isSoulbound(@Nonnull final ItemStack itemStack) {
    // check for enchantments
    final Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
    for (final Enchantment enchantment : enchantments.keySet()) {
      if ( enchantment != null
           && (GraveKeeperConfig.ANY_ENCHANT_IS_SOULBOUND.get()
               || GraveKeeperConfig.SOULBOUND_ENCHANTMENTS.contains(enchantment) ) ) {
        return true;
      }
    }
    
    // check for NBT tags
    final CompoundNBT tagCompound = itemStack.getTag();
    if (tagCompound != null) {
      for (final CompoundNBT tagElement : GraveKeeperConfig.SOULBOUND_TAGS) {
        boolean isSoulbound = true;
        for (final String tagKey : tagElement.keySet()) {
          if ( tagCompound.get(tagKey) == null
               || !Objects.equals(tagCompound.get(tagKey), tagElement.get(tagKey))) {
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
