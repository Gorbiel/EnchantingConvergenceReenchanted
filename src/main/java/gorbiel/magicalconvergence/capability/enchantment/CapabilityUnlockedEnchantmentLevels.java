package gorbiel.magicalconvergence.capability.enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import gorbiel.magicalconvergence.EnchantingConvergence;
import gorbiel.magicalconvergence.network.packet.server.SPacketSyncUnlockedEnchantmentLevels;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;

public class CapabilityUnlockedEnchantmentLevels {

	private final PlayerEntity player;
	private final Object2IntMap<ResourceLocation> unlockedEnchantmentLevels = new Object2IntOpenHashMap<>();

	public CapabilityUnlockedEnchantmentLevels(PlayerEntity player) {
		this.player = player;
	}

	public boolean isUnlocked(ResourceLocation enchantName, int enchantLevel) {
		return this.unlockedEnchantmentLevels.getInt(enchantName) >= enchantLevel;
	}

	public boolean unlock(ResourceLocation enchantName, int enchantLevel) {
		if (this.unlockedEnchantmentLevels.getInt(enchantName) >= enchantLevel) {
			return false;
		}
		this.unlockedEnchantmentLevels.put(enchantName, enchantLevel);
		if (!this.player.world.isRemote) {
			EnchantingConvergence.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.player), new SPacketSyncUnlockedEnchantmentLevels(this.player));
		}
		return true;
	}

	public void setUnlockedEnchantmentLevels(Object2IntMap<ResourceLocation> map) {
		this.unlockedEnchantmentLevels.clear();
		for (Object2IntMap.Entry<ResourceLocation> entry : map.object2IntEntrySet()) {
			this.unlockedEnchantmentLevels.put(entry.getKey(), entry.getIntValue());
		}
	}

	public Object2IntMap<ResourceLocation> getUnlockedEnchantmentLevels() {
		return this.unlockedEnchantmentLevels;
	}

}
