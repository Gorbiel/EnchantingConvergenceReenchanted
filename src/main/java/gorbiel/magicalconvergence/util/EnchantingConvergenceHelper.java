package gorbiel.magicalconvergence.util;

import gorbiel.magicalconvergence.config.EnchantingConvergenceConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantingConvergenceHelper {

    public static List<Enchantment> getValidEnchantments(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }
        if (EnchantingConvergenceConfig.SERVER_CONFIG.checkIfItemHasEnchantability.get()
                && stack.getEnchantmentValue() <= 0
                && stack.getItem() != Items.ENCHANTED_BOOK) {
            return Collections.emptyList();
        }
        List<Enchantment> list = new ArrayList<>();
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            if (EnchantingConvergenceConfig.SERVER_CONFIG.checkIfEnchantmentIsTreasureEnchantment.get()
                    && enchantment.isTreasureOnly()) {
                continue;
            }
            if (EnchantingConvergenceConfig.SERVER_CONFIG.checkIfEnchantmentCanGenerateInLoot.get()
                    && !enchantment.isDiscoverable()) {
                continue;
            }
            if (!enchantment.canApplyAtEnchantingTable(stack)
                    && ((stack.getItem() != Items.BOOK && stack.getItem() != Items.ENCHANTED_BOOK)
                            || !enchantment.isAllowedOnBooks())) {
                continue;
            }
            list.add(enchantment);
        }
        return list;
    }

    public static int getLevelCost(ItemStack stack, Enchantment enchantment, int level) {
        level = Mth.clamp(level, 0, enchantment.getMaxLevel());
        double d1 = calcRequiredEnchantabilityModifier(enchantment, level);
        double d2 = calcRarityModifier(enchantment);
        double d3 = calcItemEnchantabilityModifier(stack);
        double d4 = (d1 + d2) * d3;
        return (int) Math.max(Math.round(d4), 1);
    }

    public static int getLapisCost(ItemStack stack, Enchantment enchantment, int level) {
        level = Mth.clamp(level, 0, enchantment.getMaxLevel());
        double d1 = calcRequiredEnchantabilityModifier(enchantment, level);
        double d2 = calcRarityModifier(enchantment);
        double d3 = calcItemEnchantabilityModifier(stack);
        double d4 = (d1 + d2) * d3;
        return (int) Math.max(Math.round(d4), 1);
    }

    public static int getPowerCost(Enchantment enchantment, int level) {
        level = Mth.clamp(level, 0, enchantment.getMaxLevel());
        int min = enchantment.getMinCost(level);
        int i = Math.min(enchantment.getMinCost(1), 5);
        double d1 = (min - i * 0.5D) / 35.0D;
        double d2 = ((double) level - 1.0D) / (double) enchantment.getMaxLevel();
        return Mth.clamp((int) Math.round((d1 + d2) / 2.0D * 15.0D), 0, 15);
    }

    public static double calcRequiredEnchantabilityModifier(Enchantment enchantment, int level) {
        level = Mth.clamp(level, 0, enchantment.getMaxLevel());
        double c = 16.0D;
        int i = Mth.ceil(1.0D + c - (c * c) / (level + c - 1.0D));
        double f = 0.12D;
        return f * (double) enchantment.getMinCost(i);
    }

    public static double calcRarityModifier(Enchantment enchantment) {
        int i = enchantment.getRarity().getWeight();
        double w = 8.0D;
        return (10.0D + w) / ((double) i + w);
    }

    public static double calcItemEnchantabilityModifier(ItemStack stack) {
        int i = stack.getEnchantmentValue();
        double e = 25.0D;
        return 1.0D / (1.0D + ((double) i / e));
    }
}
