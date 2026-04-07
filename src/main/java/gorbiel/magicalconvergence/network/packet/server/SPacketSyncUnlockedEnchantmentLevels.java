package gorbiel.magicalconvergence.network.packet.server;

import gorbiel.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevelsProvider;
import gorbiel.magicalconvergence.client.ClientEnchantingConvergence;
import gorbiel.magicalconvergence.network.packet.IPacket;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketSyncUnlockedEnchantmentLevels implements IPacket {

    private Object2IntMap<ResourceLocation> map = new Object2IntOpenHashMap<>();

    public SPacketSyncUnlockedEnchantmentLevels() {}

    public SPacketSyncUnlockedEnchantmentLevels(Player player) {
        player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY)
                .ifPresent(cap -> this.map = new Object2IntOpenHashMap<>(cap.getUnlockedEnchantmentLevels()));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.map.size());
        for (Object2IntMap.Entry<ResourceLocation> entry : this.map.object2IntEntrySet()) {
            buffer.writeUtf(entry.getKey().toString());
            buffer.writeInt(entry.getIntValue());
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        this.map.clear();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            this.map.put(ResourceLocation.parse(buffer.readUtf()), buffer.readInt());
        }
    }

    @Override
    public void handle(Supplier<Context> ctxSupplier) {
        Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Player player = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientEnchantingConvergence::getPlayer);
            if (player != null) {
                player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY)
                        .ifPresent(cap -> cap.setUnlockedEnchantmentLevels(this.map));
            }
        });
        ctx.setPacketHandled(true);
    }
}
