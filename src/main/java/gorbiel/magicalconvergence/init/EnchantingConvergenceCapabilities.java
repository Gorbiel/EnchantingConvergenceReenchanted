package gorbiel.magicalconvergence.init;

import gorbiel.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevels;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class EnchantingConvergenceCapabilities {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(CapabilityUnlockedEnchantmentLevels.class);
    }
}
