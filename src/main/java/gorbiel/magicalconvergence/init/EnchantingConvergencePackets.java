package gorbiel.magicalconvergence.init;

import java.util.function.Supplier;

import gorbiel.magicalconvergence.EnchantingConvergence;
import gorbiel.magicalconvergence.network.packet.IPacket;
import gorbiel.magicalconvergence.network.packet.server.SPacketSyncUnlockedEnchantmentLevels;

public class EnchantingConvergencePackets {

	private static int id = 1;

	public static void registerPackets() {
		registerPacket(SPacketSyncUnlockedEnchantmentLevels.class, SPacketSyncUnlockedEnchantmentLevels::new);
	}

	private static <T extends IPacket> void registerPacket(Class<T> packetClass, Supplier<T> packetSupplier) {
		EnchantingConvergence.NETWORK.registerMessage(
				id++,
				packetClass,
                IPacket::encode,
				buffer -> {
					T packet = packetSupplier.get();
					packet.decode(buffer);
					return packet;
				},
                IPacket::handle
		);
	}
}