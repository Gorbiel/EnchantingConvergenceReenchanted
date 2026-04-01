package gorbiel.magicalconvergence.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class EnchantingConvergenceConfig {

	public static final ServerConfig SERVER_CONFIG;
	public static final ForgeConfigSpec SERVER_SPEC;
	static {
		final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_CONFIG = specPair.getLeft();
		SERVER_SPEC = specPair.getRight();
	}

	public static class ServerConfig {

		public final ForgeConfigSpec.BooleanValue checkIfItemHasEnchantability;
		public final ForgeConfigSpec.BooleanValue checkIfEnchantmentIsTreasureEnchantment;
		public final ForgeConfigSpec.BooleanValue checkIfEnchantmentCanGenerateInLoot;

		public final ForgeConfigSpec.DoubleValue expTotalModifier;
		public final ForgeConfigSpec.DoubleValue expBaseCost;
		public final ForgeConfigSpec.DoubleValue expEnchantmentRarityModifier;
		public final ForgeConfigSpec.DoubleValue expItemEnchantabilityModifier;

		public final ForgeConfigSpec.DoubleValue lapisTotalModifier;
		public final ForgeConfigSpec.DoubleValue lapisBaseCost;
		public final ForgeConfigSpec.DoubleValue lapisEnchantmentRarityModifier;
		public final ForgeConfigSpec.DoubleValue lapisItemEnchantabilityModifier;

		public final ForgeConfigSpec.BooleanValue unlockEnchantmentModeEnabled;
		public final ForgeConfigSpec.BooleanValue unlockEnchantmentModeHardcore;

		public final ForgeConfigSpec.IntValue unlockEnchantmentModeCommonWeight;
		public final ForgeConfigSpec.IntValue unlockEnchantmentModeUncommonWeight;
		public final ForgeConfigSpec.IntValue unlockEnchantmentModeRareWeight;
		public final ForgeConfigSpec.IntValue unlockEnchantmentModeVeryRareWeight;

		public ServerConfig(ForgeConfigSpec.Builder builder) {
			this.checkIfItemHasEnchantability = builder.define("checkIfItemHasEnchantability", false);
			this.checkIfEnchantmentIsTreasureEnchantment = builder.define("checkIfEnchantmentIsTreasureEnchantment", true);
			this.checkIfEnchantmentCanGenerateInLoot = builder.define("checkIfEnchantmentCanGenerateInLoot", true);

			this.expTotalModifier = builder.defineInRange("expTotalModifier", 2.0D, 0.0D, 256.0D);
			this.expBaseCost = builder.defineInRange("expBaseCost", 25.0D, 0.0D, 256.0D);
			this.expEnchantmentRarityModifier = builder.defineInRange("expEnchantmentRarityModifier", 8.0D, 0.0D, 256.0D);
			this.expItemEnchantabilityModifier = builder.defineInRange("expItemEnchantabilityModifier", 50.0D, 0.0D, 256.0D);

			this.lapisTotalModifier = builder.defineInRange("lapisTotalModifier", 0.05D, 0.0D, 256.0D);
			this.lapisBaseCost = builder.defineInRange("lapisBaseCost", 25.0D, 0.0D, 256.0D);
			this.lapisEnchantmentRarityModifier = builder.defineInRange("lapisEnchantmentRarityModifier", 8.0D, 0.0D, 256.0D);
			this.lapisItemEnchantabilityModifier = builder.defineInRange("lapisItemEnchantabilityModifier", 50.0D, 0.0D, 256.0D);

			this.unlockEnchantmentModeEnabled = builder.comment("When enabled enchantments first have to be unlocked.").define("unlockEnchantmentModeEnabled", true);
			this.unlockEnchantmentModeHardcore = builder.comment("When enabled when unlocking a new random enchantment it may unlock an enchantment of a lower tier.").define("unlockEnchantmentModeHardcore", false);

			this.unlockEnchantmentModeCommonWeight = builder.defineInRange("unlockEnchantmentModeCommonWeight", 4, 1, 100);
			this.unlockEnchantmentModeUncommonWeight = builder.defineInRange("unlockEnchantmentModeUncommonWeight", 3, 1, 100);
			this.unlockEnchantmentModeRareWeight = builder.defineInRange("unlockEnchantmentModeRareWeight", 2, 1, 100);
			this.unlockEnchantmentModeVeryRareWeight = builder.defineInRange("unlockEnchantmentModeVeryRareWeight", 1, 1, 100);
		}

	}

}
