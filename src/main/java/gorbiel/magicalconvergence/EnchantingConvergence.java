package gorbiel.magicalconvergence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gorbiel.magicalconvergence.client.ClientEnchantingConvergence;
import gorbiel.magicalconvergence.init.EnchantingConvergenceCapabilities;
import gorbiel.magicalconvergence.init.EnchantingConvergenceContainers;
import gorbiel.magicalconvergence.init.EnchantingConvergencePackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import gorbiel.magicalconvergence.config.EnchantingConvergenceConfig;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(EnchantingConvergence.MOD_ID)
public class EnchantingConvergence {

	public static final String MOD_ID = "enchanting_convergence_reenchanted";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static final String NETWORK_VERSION = "1.0.0";

	public static final SimpleChannel NETWORK =
			NetworkRegistry.newSimpleChannel(
					ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"),
					() -> NETWORK_VERSION,
					NETWORK_VERSION::equals,
					NETWORK_VERSION::equals
			);

	public EnchantingConvergence(FMLJavaModLoadingContext context) {
		var modEventBus = context.getModEventBus();

		modEventBus.addListener(EnchantingConvergence::setup);
		modEventBus.addListener(ClientEnchantingConvergence::setupClient);
		modEventBus.addListener(EnchantingConvergenceCapabilities::registerCapabilities);

		context.registerConfig(ModConfig.Type.SERVER, EnchantingConvergenceConfig.SERVER_SPEC);

		EnchantingConvergenceContainers.registerContainers(context);
	}

	public static void setup(FMLCommonSetupEvent event) {
		EnchantingConvergencePackets.registerPackets();
	}
}