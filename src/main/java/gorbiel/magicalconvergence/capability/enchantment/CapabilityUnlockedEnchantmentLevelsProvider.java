package gorbiel.magicalconvergence.capability.enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import gorbiel.magicalconvergence.EnchantingConvergence;
import gorbiel.magicalconvergence.capability.BasicCapabilityProviderSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityUnlockedEnchantmentLevelsProvider extends BasicCapabilityProviderSerializable<CapabilityUnlockedEnchantmentLevels> {

	public static final ResourceLocation REGISTRY_NAME =
			ResourceLocation.fromNamespaceAndPath(EnchantingConvergence.MOD_ID, "unlocked_enchantment_levels");

	public static final Capability<CapabilityUnlockedEnchantmentLevels> CAPABILITY =
			CapabilityManager.get(new CapabilityToken<>() {});

	public CapabilityUnlockedEnchantmentLevelsProvider(Player player) {
		super(CAPABILITY, () -> new CapabilityUnlockedEnchantmentLevels(player));
	}

	@Override
	public Tag serializeNBT() {
		CompoundTag compound = new CompoundTag();
		for (Object2IntMap.Entry<ResourceLocation> entry : this.getInstance().getUnlockedEnchantmentLevels().object2IntEntrySet()) {
			compound.putInt(entry.getKey().toString(), entry.getIntValue());
		}
		return compound;
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		if (nbt instanceof CompoundTag compound) {
			Object2IntMap<ResourceLocation> map = this.getInstance().getUnlockedEnchantmentLevels();
			map.clear();
			for (String key : compound.getAllKeys()) {
				map.put(ResourceLocation.parse(key), compound.getInt(key));
			}
		}
	}

}