package gorbiel.magicalconvergence.client.gui.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gorbiel.magicalconvergence.EnchantingConvergence;
import gorbiel.magicalconvergence.inventory.container.ContainerEnchantingConvergence;
import gorbiel.magicalconvergence.inventory.container.EnchantingMode;
import gorbiel.magicalconvergence.util.EnchantingConvergenceHelper;
import gorbiel.magicalconvergence.util.UnlockedEnchantmentHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/** Copied from {@link EnchantmentScreen} */
@OnlyIn(Dist.CLIENT)
public class ScreenEnchantingConvergence extends AbstractContainerScreen<ContainerEnchantingConvergence> {

    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EnchantingConvergence.MOD_ID, "textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE =
            ResourceLocation.parse("textures/entity/enchanting_table_book.png");

    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;

    public int ticks;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    private static final int ROW_X = 60;
    private static final int ROW_Y = 14;
    private static final int ROW_WIDTH = 200;
    private static final int ROW_HEIGHT = 17; // was 19
    private static final int ROW_SPACING = 19;

    private final int enchantButtons = 4;
    private final List<Enchantment> prevEnchList = new ArrayList<>();
    private int scrollStartIndex = 0;
    private boolean clickedScrollbar;
    private EnchantingMode enchantingMode = EnchantingMode.NORMAL;

    public ScreenEnchantingConvergence(
            ContainerEnchantingConvergence menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 275;
        this.imageHeight = 187;
        this.inventoryLabelY = 94;
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.tickBook();
        this.updateScrollbar();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(261, 14, 6, 76, mouseX, mouseY)) {
            this.clickedScrollbar = true;
            return true;
        } else {
            this.clickedScrollbar = false;
        }

        ItemStack stack = this.menu.getSlot(0).getItem();

        for (int i = 0; i < this.enchantButtons; i++) {
            if (!this.isHovering(ROW_X, ROW_Y + ROW_SPACING * i, ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                continue;
            }

            if (this.enchantingMode == EnchantingMode.NORMAL) {
                if (this.scrollStartIndex + i >= this.menu.getEnchantmentList().size()) {
                    continue;
                }

                Enchantment ench = this.menu.getEnchantmentList().get(this.scrollStartIndex + i);
                int level = stack.getEnchantmentLevel(ench);

                if (level >= ench.getMaxLevel()) {
                    continue;
                }

                if (!this.minecraft.player.getAbilities().instabuild) {
                    if (!UnlockedEnchantmentHelper.isUnlocked(this.minecraft.player, ench, level + 1)) {
                        continue;
                    }

                    int levelCost = EnchantingConvergenceHelper.getLevelCost(stack, ench, level + 1);
                    int lapisCost = EnchantingConvergenceHelper.getLapisCost(stack, ench, level + 1);
                    int powerCost = EnchantingConvergenceHelper.getPowerCost(ench, level + 1);

                    if (levelCost > this.minecraft.player.experienceLevel
                            || lapisCost > this.menu.getLapisAmount()
                            || powerCost > this.menu.getEnchantingPower()) {
                        continue;
                    }
                }

                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.scrollStartIndex + i);
                return true;
            } else {
                if (!this.minecraft.player.getAbilities().instabuild) {
                    int levelCost = (i + 1) * 10;
                    int lapisCost = (i + 1) * 5;
                    int powerCost = i * 4 + 3;

                    if (levelCost > this.minecraft.player.experienceLevel
                            || lapisCost > this.menu.getLapisAmount()
                            || powerCost > this.menu.getEnchantingPower()) {
                        continue;
                    }
                }

                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId, (1 << 31) | (this.scrollStartIndex + i));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        //		graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, this.leftPos, this.topPos, 0, 0,
        // this.imageWidth, this.imageHeight);
        graphics.blit(
                ENCHANTMENT_TABLE_GUI_TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                512,
                256);

        boolean scrollbarEnabled = this.enchantingMode == EnchantingMode.NORMAL
                && this.menu.getEnchantmentList().size() > this.enchantButtons;
        int offset = scrollbarEnabled
                ? (int) ((double) this.scrollStartIndex
                        / (double) Math.max(this.menu.getEnchantmentList().size() - this.enchantButtons, 1)
                        * 55.0D)
                : 0;

        //		graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, this.leftPos + 261, this.topPos + 14 + offset,
        // 219 + (scrollbarEnabled ? 0 : 6), 187, 6, 21);
        graphics.blit(
                ENCHANTMENT_TABLE_GUI_TEXTURE,
                this.leftPos + 261,
                this.topPos + 14 + offset,
                219 + (scrollbarEnabled ? 0 : 6),
                187,
                6,
                21,
                512,
                256);

        this.renderBook(graphics, this.leftPos, this.topPos, partialTick);

        int level = this.minecraft.player.experienceLevel;
        int lapis = this.menu.getLapisAmount();
        int power = this.menu.getEnchantingPower();
        boolean creative = this.minecraft.player.getAbilities().instabuild;
        ItemStack stack = this.menu.getSlot(0).getItem();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        for (int i = 0; i < this.enchantButtons; i++) {
            int j = this.scrollStartIndex + i;
            int x1 = this.leftPos + 60;
            int y1 = this.topPos + 14 + 19 * i;

            if (this.enchantingMode == EnchantingMode.NORMAL) {
                if (j >= this.menu.getEnchantmentList().size()) {
                    //					graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, 206, 200, 19);
                    graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, 206, 200, 19, 512, 256);
                    continue;
                }

                Enchantment ench = this.menu.getEnchantmentList().get(j);
                int enchLevel = enchantments.getOrDefault(ench, 0);
                boolean selected =
                        this.isHovering(ROW_X, ROW_Y + ROW_SPACING * i, ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY);

                if (!creative
                        && enchLevel < ench.getMaxLevel()
                        && !UnlockedEnchantmentHelper.isUnlocked(this.minecraft.player, ench, enchLevel + 1)) {
                    //					graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, 206, 200, 19);
                    graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, 206, 200, 19, 512, 256);
                    //					graphics.drawString(this.font, this.getDisplayName(ench), x1 + 6, y1 + 6, 0x342F25,
                    // false);
                    Component text = this.getDisplayName(ench).copy().withStyle(ChatFormatting.DARK_GRAY);
                    graphics.drawString(this.font, text, x1 + 6, y1 + 6, 0, false);
                    continue;
                }

                //				graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, selected ? 225 : 187, 200,
                // 19);
                graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, selected ? 225 : 187, 200, 19, 512, 256);

                if (enchLevel >= ench.getMaxLevel()) {
                    //					graphics.drawString(this.font, this.getDisplayName(ench).copy().append(" MAX"), x1
                    // + 6, y1 + 6, selected ? 0xFFFF80 : 0x685E4A, false);
                    Component text = this.getDisplayName(ench)
                            .copy()
                            .append(" MAX")
                            .withStyle(style -> style.withColor(selected ? 0xFFFF80 : 0x685E4A));
                    graphics.drawString(this.font, text, x1 + 6, y1 + 6, 0, false);
                    continue;
                }

                if (!enchantments.containsKey(ench)
                        && !EnchantmentHelper.isEnchantmentCompatible(enchantments.keySet(), ench)) {
                    //					graphics.drawString(this.font,
                    // this.getDisplayName(ench).copy().withStyle(ChatFormatting.RED,
                    // ChatFormatting.STRIKETHROUGH), x1 + 6, y1 + 6, selected ? 0xFFFF80 : 0x685E4A, false);
                    Component text = this.getDisplayName(ench)
                            .copy()
                            .withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH);
                    graphics.drawString(this.font, text, x1 + 6, y1 + 6, 0, false);
                    continue;
                }

                int levelCost = EnchantingConvergenceHelper.getLevelCost(stack, ench, enchLevel + 1);
                int lapisCost = EnchantingConvergenceHelper.getLapisCost(stack, ench, enchLevel + 1);
                int powerCost = EnchantingConvergenceHelper.getPowerCost(ench, enchLevel + 1);

                boolean enoughLevels = level >= levelCost || creative;
                boolean enoughLapis = lapis >= lapisCost || creative;
                boolean enoughPower = power >= powerCost || creative;

                //				graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1 + 143, y1, 200, 187, 19, 19);
                graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1 + 143, y1, 200, 187, 19, 19, 512, 256);
                graphics.renderItem(new ItemStack(Items.LAPIS_LAZULI), x1 + 162, y1 + 1);
                graphics.renderItem(new ItemStack(Blocks.BOOKSHELF), x1 + 181, y1 + 1);

                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);

                String requiredLevels = Integer.toString(levelCost);
                graphics.drawString(
                        this.font,
                        requiredLevels,
                        x1 + 143 + 18 - this.font.width(requiredLevels),
                        y1 + 10,
                        enoughLevels ? 0xF0F0F0 : 0xA50000,
                        false);

                String requiredLapis = Integer.toString(lapisCost);
                graphics.drawString(
                        this.font,
                        requiredLapis,
                        x1 + 162 + 18 - this.font.width(requiredLapis),
                        y1 + 10,
                        enoughLapis ? 0xF0F0F0 : 0xA50000,
                        false);

                String requiredPower = Integer.toString(powerCost);
                graphics.drawString(
                        this.font,
                        requiredPower,
                        x1 + 181 + 18 - this.font.width(requiredPower),
                        y1 + 10,
                        enoughPower ? 0xF0F0F0 : 0xA50000,
                        false);

                graphics.pose().popPose();

                //				graphics.drawString(this.font, ench.getFullname(enchLevel + 1), x1 + 6, y1 + 6,
                // selected ? 0xFFFF80 : 0x685E4A, false);
                Component text = ench.getFullname(enchLevel + 1)
                        .copy()
                        .withStyle(style -> style.withColor(selected ? 0xFFFF80 : 0x685E4A));
                graphics.drawString(this.font, text, x1 + 6, y1 + 6, 0, false);

            } else {
                boolean selected =
                        this.isHovering(ROW_X, ROW_Y + ROW_SPACING * i, ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY);

                if (stack.isEmpty() || stack.getItem() != Items.BOOK) {
                    //					graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, 206, 200, 19);
                    graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, 206, 200, 19, 512, 256);
                    continue;
                }

                //				graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, selected ? 225 : 187, 200,
                // 19);
                graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1, y1, 0, selected ? 225 : 187, 200, 19, 512, 256);

                int levelCost = (i + 1) * 10;
                int lapisCost = (i + 1) * 5;
                int powerCost = i * 4 + 3;

                boolean enoughLevels = level >= levelCost || creative;
                boolean enoughLapis = lapis >= lapisCost || creative;
                boolean enoughPower = power >= powerCost || creative;

                //				graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1 + 143, y1, 200, 187, 19, 19);
                graphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, x1 + 143, y1, 200, 187, 19, 19, 512, 256);
                graphics.renderItem(new ItemStack(Items.LAPIS_LAZULI), x1 + 162, y1 + 1);
                graphics.renderItem(new ItemStack(Blocks.BOOKSHELF), x1 + 181, y1 + 1);

                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);

                String requiredLevels = Integer.toString(levelCost);
                graphics.drawString(
                        this.font,
                        requiredLevels,
                        x1 + 143 + 18 - this.font.width(requiredLevels),
                        y1 + 10,
                        enoughLevels ? 0xF0F0F0 : 0xA50000,
                        false);

                String requiredLapis = Integer.toString(lapisCost);
                graphics.drawString(
                        this.font,
                        requiredLapis,
                        x1 + 162 + 18 - this.font.width(requiredLapis),
                        y1 + 10,
                        enoughLapis ? 0xF0F0F0 : 0xA50000,
                        false);

                String requiredPower = Integer.toString(powerCost);
                graphics.drawString(
                        this.font,
                        requiredPower,
                        x1 + 181 + 18 - this.font.width(requiredPower),
                        y1 + 10,
                        enoughPower ? 0xF0F0F0 : 0xA50000,
                        false);

                graphics.pose().popPose();

                FormattedText galactic = EnchantmentNames.getInstance().getRandomName(this.font, 86);
                graphics.drawWordWrap(this.font, galactic, x1 + 6, y1 + 6, 86, selected ? 0xFFFF80 : 0x685E4A);
            }
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                this.leftPos + 216 + 17,
                this.topPos + 111 + 40,
                20,
                (float) (this.leftPos + 216 + 17) - mouseX,
                (float) (this.topPos + 111 + 40 - 30) - mouseY,
                this.minecraft.player);
    }

    private void renderBook(GuiGraphics graphics, int leftPos, int topPos, float partialTick) {
        float openLerp = Mth.lerp(partialTick, this.oOpen, this.open);
        float flipLerp = Mth.lerp(partialTick, this.oFlip, this.flip);

        Lighting.setupForEntityInInventory();
        graphics.pose().pushPose();
        graphics.pose().translate((float) leftPos + 33.0F, (float) topPos + 31.0F, 100.0F);
        graphics.pose().scale(-40.0F, 40.0F, 40.0F);
        graphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
        graphics.pose().translate((1.0F - openLerp) * 0.2F, (1.0F - openLerp) * 0.1F, (1.0F - openLerp) * 0.25F);
        float yRot = -(1.0F - openLerp) * 90.0F - 90.0F;
        graphics.pose().mulPose(Axis.YP.rotationDegrees(yRot));
        graphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));

        float page1 = Mth.clamp(Mth.frac(flipLerp + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float page2 = Mth.clamp(Mth.frac(flipLerp + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);

        this.bookModel.setupAnim(0.0F, page1, page2, openLerp);
        VertexConsumer vc =
                graphics.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTMENT_TABLE_BOOK_TEXTURE));
        this.bookModel.renderToBuffer(graphics.pose(), vc, 15728880, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        graphics.flush();
        graphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        partialTick = this.minecraft.getFrameTime();
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        boolean creative = this.minecraft.player.isCreative();
        ItemStack stack = this.menu.getSlot(0).getItem();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        for (int i = 0; i < this.enchantButtons; i++) {
            if (!this.isHovering(ROW_X, ROW_Y + ROW_SPACING * i, ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                continue;
            }

            List<Component> tooltip = new ArrayList<>();

            //			if (this.enchantingMode == EnchantingMode.NORMAL) {
            //				int j = this.scrollStartIndex + i;
            //				if (j >= this.menu.getEnchantmentList().size()) {
            //					continue;
            //				}
            //
            //				Enchantment ench = this.menu.getEnchantmentList().get(j);
            //				int enchLevel = Math.min(enchantments.getOrDefault(ench, 0) + 1, ench.getMaxLevel());
            //
            //				tooltip.add(ench.getFullname(enchLevel).copy().withStyle(ChatFormatting.WHITE));
            //
            //				int levelCost = EnchantingConvergenceHelper.getLevelCost(stack, ench, enchLevel);
            //				int lapisCost = EnchantingConvergenceHelper.getLapisCost(stack, ench, enchLevel);
            //				int powerCost = EnchantingConvergenceHelper.getPowerCost(ench, enchLevel);
            //
            //				tooltip.add(Component.empty());
            //
            //				if (!creative && !UnlockedEnchantmentHelper.isUnlocked(this.minecraft.player, ench,
            // enchLevel)) {
            //					tooltip.add(Component.literal("Locked").withStyle(ChatFormatting.RED));
            //				} else {
            //					tooltip.add(Component.literal("XP: " +
            // levelCost).withStyle(this.minecraft.player.experienceLevel >= levelCost || creative ?
            // ChatFormatting.GRAY : ChatFormatting.RED));
            //					tooltip.add(Component.literal("Lapis: " +
            // lapisCost).withStyle(this.menu.getLapisAmount() >= lapisCost || creative ?
            // ChatFormatting.GRAY : ChatFormatting.RED));
            //					tooltip.add(Component.literal("Power: " +
            // powerCost).withStyle(this.menu.getEnchantingPower() >= powerCost || creative ?
            // ChatFormatting.GRAY : ChatFormatting.RED));
            //				}
            //			} else {
            //				int levelCost = (i + 1) * 10;
            //				int lapisCost = (i + 1) * 5;
            //				int powerCost = i * 4 + 3;
            //
            //				tooltip.add(Component.literal("Unlock random
            // enchantment").withStyle(ChatFormatting.WHITE));
            //				tooltip.add(Component.empty());
            //				tooltip.add(Component.literal("XP: " +
            // levelCost).withStyle(this.minecraft.player.experienceLevel >= levelCost || creative ?
            // ChatFormatting.GRAY : ChatFormatting.RED));
            //				tooltip.add(Component.literal("Lapis: " +
            // lapisCost).withStyle(this.menu.getLapisAmount() >= lapisCost || creative ?
            // ChatFormatting.GRAY : ChatFormatting.RED));
            //				tooltip.add(Component.literal("Power: " +
            // powerCost).withStyle(this.menu.getEnchantingPower() >= powerCost || creative ?
            // ChatFormatting.GRAY : ChatFormatting.RED));
            //			}

            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            break;
        }

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    public void tickBook() {
        ItemStack itemstack = this.menu.getSlot(0).getItem();
        if (!ItemStack.matches(itemstack, this.last)) {
            this.last = itemstack;
            do {
                this.flipT += (float) (this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }

        ++this.ticks;
        this.oFlip = this.flip;
        this.oOpen = this.open;

        if (!this.menu.getEnchantmentList().isEmpty()) {
            this.open += 0.2F;
        } else {
            this.open -= 0.2F;
        }

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float f1 = (this.flipT - this.flip) * 0.4F;
        f1 = Mth.clamp(f1, -0.2F, 0.2F);
        this.flipA += (f1 - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }

    private void updateScrollbar() {
        List<Enchantment> enchList = this.menu.getEnchantmentList();
        boolean same = this.prevEnchList.size() == enchList.size();

        if (same) {
            for (int i = 0; i < enchList.size(); i++) {
                if (this.prevEnchList.get(i) != enchList.get(i)) {
                    this.prevEnchList.set(i, enchList.get(i));
                    same = false;
                }
            }
        } else {
            this.prevEnchList.clear();
            this.prevEnchList.addAll(enchList);
        }

        if (this.enchantingMode != EnchantingMode.NORMAL || !same) {
            this.scrollStartIndex = 0;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.enchantingMode == EnchantingMode.NORMAL && this.isHovering(59, 13, 209, 78, mouseX, mouseY)) {
            this.scrollStartIndex = Mth.clamp(
                    (int) (this.scrollStartIndex - delta),
                    0,
                    Math.max(this.menu.getEnchantmentList().size() - this.enchantButtons, 0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.enchantingMode == EnchantingMode.NORMAL && this.clickedScrollbar) {
            double y1 = this.topPos + 14 + 10.5D;
            double y2 = this.topPos + 14 + 76 - 10.5D;
            double scrollLength = Math.max(this.menu.getEnchantmentList().size() - this.enchantButtons, 0);
            this.scrollStartIndex = Mth.clamp(
                    (int) ((mouseY - y1) / (y2 - y1) * scrollLength + 0.5D),
                    0,
                    Math.max(this.menu.getEnchantmentList().size() - this.enchantButtons, 0));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    protected Component getDisplayName(Enchantment ench) {
        MutableComponent component = Component.translatable(ench.getDescriptionId());
        return ench.isCurse() ? component.withStyle(ChatFormatting.RED) : component.withStyle(ChatFormatting.GRAY);
    }
}
