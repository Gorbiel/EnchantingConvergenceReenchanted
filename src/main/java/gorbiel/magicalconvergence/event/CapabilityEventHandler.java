package gorbiel.magicalconvergence.event;

import gorbiel.magicalconvergence.EnchantingConvergence;
import gorbiel.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevelsProvider;
import gorbiel.magicalconvergence.network.packet.server.SPacketSyncUnlockedEnchantmentLevels;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = EnchantingConvergence.MOD_ID)
public class CapabilityEventHandler {

	@SubscribeEvent
	public static void onAttachEntityCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player player) {
			event.addCapability(
					CapabilityUnlockedEnchantmentLevelsProvider.REGISTRY_NAME,
					new CapabilityUnlockedEnchantmentLevelsProvider(player)
			);
		}
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) {
			return;
		}
		EnchantingConvergence.NETWORK.send(
				PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
				new SPacketSyncUnlockedEnchantmentLevels(player)
		);
	}

	@SubscribeEvent
	public static void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) {
			return;
		}
		EnchantingConvergence.NETWORK.send(
				PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
				new SPacketSyncUnlockedEnchantmentLevels(player)
		);
	}

	@SubscribeEvent
	public static void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) {
			return;
		}
		EnchantingConvergence.NETWORK.send(
				PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
				new SPacketSyncUnlockedEnchantmentLevels(player)
		);
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		Player newPlayer = event.getEntity();
		Player oldPlayer = event.getOriginal();

		oldPlayer.reviveCaps();
		oldPlayer.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY).ifPresent(oldCap -> {
			newPlayer.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY).ifPresent(newCap -> {
				newCap.setUnlockedEnchantmentLevels(oldCap.getUnlockedEnchantmentLevels());
			});
		});
		oldPlayer.invalidateCaps();
	}
}