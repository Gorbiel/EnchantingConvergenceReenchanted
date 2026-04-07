package gorbiel.magicalconvergence.util;

import gorbiel.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevels;
import gorbiel.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevelsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.util.LazyOptional;

// TODO diabled for now
public class UnlockedEnchantmentHelper {

    public static boolean isUnlocked(Player player, Enchantment enchantment, int level) {
        if (true) {
            return true;
        }
        return isUnlocked(player, enchantment, level);
    }

    public static boolean isUnlocked(Player player, ResourceLocation enchantment, int level) {
        if (true) {
            return true;
        }
        LazyOptional<CapabilityUnlockedEnchantmentLevels> cap =
                player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY);
        if (!cap.isPresent()) {
            return false;
        }
        return cap.orElse(null).isUnlocked(enchantment, level);
    }

    public static boolean unlock(Player player, Enchantment enchantment, int level) {
        if (true) {
            return true;
        }
        return unlock(player, enchantment, level);
    }

    public static boolean unlock(Player player, ResourceLocation enchantment, int level) {
        if (true) {
            return true;
        }
        LazyOptional<CapabilityUnlockedEnchantmentLevels> cap =
                player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY);
        if (!cap.isPresent()) {
            return false;
        }
        return cap.orElse(null).unlock(enchantment, level);
    }
}
