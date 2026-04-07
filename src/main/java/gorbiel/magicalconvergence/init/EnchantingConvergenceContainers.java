package gorbiel.magicalconvergence.init;

import gorbiel.magicalconvergence.EnchantingConvergence;
import gorbiel.magicalconvergence.inventory.container.ContainerEnchantingConvergence;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EnchantingConvergenceContainers {

    private static final DeferredRegister<MenuType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EnchantingConvergence.MOD_ID);

    public static final RegistryObject<MenuType<ContainerEnchantingConvergence>> ENCHANTING_TABLE = CONTAINERS.register(
            "enchanting_table", () -> new MenuType<>(ContainerEnchantingConvergence::new, FeatureFlags.DEFAULT_FLAGS));

    public static void registerContainers(FMLJavaModLoadingContext context) {
        CONTAINERS.register(context.getModEventBus());
    }
}
