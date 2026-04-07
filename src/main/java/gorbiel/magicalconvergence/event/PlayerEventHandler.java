package gorbiel.magicalconvergence.event;

import gorbiel.magicalconvergence.EnchantingConvergence;
import gorbiel.magicalconvergence.inventory.container.ContainerEnchantingConvergence;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = EnchantingConvergence.MOD_ID)
public class PlayerEventHandler {

    // TODO disabled for now
    // @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onXpChangedEvent(PlayerXpEvent.XpChange event) {
        // copied from Player#giveExperiencePoints
        Player player = event.getEntity();
        int amount = event.getAmount();

        player.increaseScore(amount);
        player.experienceProgress += (float) amount / (float) player.getXpNeededForNextLevel();
        player.totalExperience = Mth.clamp(player.totalExperience + amount, 0, Integer.MAX_VALUE);

        while (player.experienceProgress < 0.0F) {
            float f = player.experienceProgress * (float) player.getXpNeededForNextLevel();
            if (player.experienceLevel > 0) {
                player.giveExperienceLevels(-1);
                player.experienceProgress = 1.0F + f / (float) player.getXpNeededForNextLevel();
            } else {
                player.giveExperienceLevels(-1);
                player.experienceProgress = 0.0F;
            }
        }

        while (player.experienceProgress >= 1.0F) {
            player.experienceProgress = (player.experienceProgress - 1.0F) * (float) player.getXpNeededForNextLevel();
            player.giveExperienceLevels(1);
            player.experienceProgress /= (float) player.getXpNeededForNextLevel();
        }

        event.setCanceled(true);
    }

    private static int xpBarCap(Player player) {
        return player.experienceLevel / 5 + 50;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (state.is(Blocks.ENCHANTING_TABLE) && blockEntity instanceof EnchantmentTableBlockEntity enchantingTable) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            MenuProvider provider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return enchantingTable.getDisplayName();
                }

                @Override
                public ContainerEnchantingConvergence createMenu(
                        int id, @NotNull Inventory inventory, @NotNull Player player) {
                    return new ContainerEnchantingConvergence(id, inventory, ContainerLevelAccess.create(level, pos));
                }
            };

            event.getEntity().openMenu(provider);
        }
    }
}
