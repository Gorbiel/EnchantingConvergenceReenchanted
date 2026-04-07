package gorbiel.magicalconvergence.client;

import gorbiel.magicalconvergence.client.gui.screen.ScreenEnchantingConvergence;
import gorbiel.magicalconvergence.init.EnchantingConvergenceContainers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEnchantingConvergence {

    public static void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
                EnchantingConvergenceContainers.ENCHANTING_TABLE.get(), ScreenEnchantingConvergence::new));
    }

    @OnlyIn(Dist.CLIENT)
    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }
}
