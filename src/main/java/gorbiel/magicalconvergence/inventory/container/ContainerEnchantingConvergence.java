package gorbiel.magicalconvergence.inventory.container;

import com.mojang.datafixers.util.Pair;
import gorbiel.magicalconvergence.init.EnchantingConvergenceContainers;
import gorbiel.magicalconvergence.util.EnchantingConvergenceHelper;
import gorbiel.magicalconvergence.util.MutableEnchantmentData;
import gorbiel.magicalconvergence.util.UnlockedEnchantmentHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ContainerEnchantingConvergence extends AbstractContainerMenu {

    public static final ResourceLocation LOCATION_BLOCKS_TEXTURE = ResourceLocation.parse("textures/atlas/blocks.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET =
            ResourceLocation.parse("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE =
            ResourceLocation.parse("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS =
            ResourceLocation.parse("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = ResourceLocation.parse("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD =
            ResourceLocation.parse("item/empty_armor_slot_shield");
    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
        EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET
    };
    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS =
            new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Container tableInventory = new SimpleContainer(2) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later -
         * the game won't think it hasn't changed and skip it.
         */
        @Override
        public void setChanged() {
            super.setChanged();
            ContainerEnchantingConvergence.this.slotsChanged(this);
        }
    };
    private final Player player;
    private final ContainerLevelAccess worldPosCallable;
    private final List<Enchantment> enchList = new ArrayList<>();
    private final DataSlot power = DataSlot.standalone();

    public ContainerEnchantingConvergence(int id, Inventory playerInventory) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public ContainerEnchantingConvergence(int id, Inventory playerInventory, ContainerLevelAccess worldPosCallable) {
        super(EnchantingConvergenceContainers.ENCHANTING_TABLE.get(), id);
        this.player = playerInventory.player;
        this.worldPosCallable = worldPosCallable;
        this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well
             * as furnace fuel.
             */
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return true;
            }

            /**
             * Returns the maximum stack size for a given slot (usually the same as
             * getInventoryStackLimit(), but 1 in the case of armor slots)
             */
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.tableInventory, 1, 35, 47) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well
             * as furnace fuel.
             */
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Tags.Items.GEMS_LAPIS);
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 105 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 163));
        }

        for (int k = 0; k < 4; ++k) {
            final EquipmentSlot equipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlot(new Slot(playerInventory, 39 - k, 195, 107 + k * 18) {
                /**
                 * Returns the maximum stack size for a given slot (usually the same as
                 * getInventoryStackLimit(), but 1 in the case of armor slots)
                 */
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                /**
                 * Check if the stack is allowed to be placed in this slot, used for armor slots as well
                 * as furnace fuel.
                 */
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.canEquip(equipmentslot, ContainerEnchantingConvergence.this.player);
                }

                /** Return whether this slot's stack can be taken from this slot. */
                @Override
                public boolean mayPickup(@NotNull Player playerIn) {
                    ItemStack itemstack = this.getItem();
                    return (itemstack.isEmpty()
                                    || playerIn.isCreative()
                                    || !EnchantmentHelper.hasBindingCurse(itemstack))
                            && super.mayPickup(playerIn);
                }

                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(LOCATION_BLOCKS_TEXTURE, ARMOR_SLOT_TEXTURES[equipmentslot.getIndex()]);
                }
            });
        }

        this.addSlot(new Slot(playerInventory, 40, 217, 161) {
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(LOCATION_BLOCKS_TEXTURE, EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addDataSlot(this.power);
        this.calcEnchantingPower();
    }

    public int getEnchantingPower() {
        return this.power.get();
    }

    private int calcEnchantingPower() {
        return this.worldPosCallable.evaluate(
                (world, pos) -> {
                    int power = 0;

                    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                    for (int k = -1; k <= 1; ++k) {
                        for (int l = -1; l <= 1; ++l) {
                            if ((k != 0 || l != 0)
                                    && world.isEmptyBlock(mutable.set(pos).move(l, 0, k))
                                    && world.isEmptyBlock(mutable.set(pos).move(l, 1, k))) {
                                power += (int)
                                        this.getPower(world, mutable.set(pos).move(l * 2, 0, k * 2));
                                power += (int)
                                        this.getPower(world, mutable.set(pos).move(l * 2, 1, k * 2));

                                if (l != 0 && k != 0) {
                                    power += (int) this.getPower(
                                            world, mutable.set(pos).move(l * 2, 0, k));
                                    power += (int) this.getPower(
                                            world, mutable.set(pos).move(l * 2, 1, k));
                                    power += (int) this.getPower(
                                            world, mutable.set(pos).move(l, 0, k * 2));
                                    power += (int) this.getPower(
                                            world, mutable.set(pos).move(l, 1, k * 2));
                                }
                            }
                        }
                    }

                    this.power.set(power);
                    return power;
                },
                0);
    }

    private float getPower(Level world, BlockPos pos) {
        return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
    }

    /** Callback for when the crafting matrix is changed. */
    @Override
    public void slotsChanged(@NotNull Container inventoryIn) {
        if (inventoryIn != this.tableInventory) {
            return;
        }

        /*
         * debug
         * EnchantingConvergence.LOGGER.info("");
         * for (Enchantment e : ForgeRegistries.ENCHANTMENTS.getValues()) {
         * int[] arr = new int[e.getMaxLevel()];
         * for (int i = 1; i <= e.getMaxLevel(); i++) {
         * int p1 = EnchantingConvergenceHelper.getPowerCost(e, i);
         * arr[i - 1] = p1;
         * }
         * String s = new TranslationTextComponent(e.getName()).getString();
         * StringBuilder sb = new StringBuilder(s);
         * for (int j = MathHelper.ceil((24.0D - s.length()) / 8.0D); j > 0; j--) {
         * sb.append('\t');
         * }
         * sb.append('\t');
         * int diff = 0;
         * if (arr.length > 1) {
         * int last = arr[1] - arr[0];
         * for (int i = 1; i < arr.length - 1; i++) {
         * if (arr[i + 1] - arr[i] != last) {
         * diff = Math.max(Math.abs(last - (arr[i + 1] - arr[i])), diff);
         * last = arr[i + 1] - arr[i];
         * }
         * }
         * }
         * sb.append(diff);
         * sb.append('\t');
         * for (int i = 0; i < arr.length; i++) {
         * if (arr[i] < 10) {
         * sb.append(' ');
         * }
         * sb.append(arr[i]);
         * sb.append(' ');
         * }
         * // if (diff > 1)
         * if (arr[arr.length - 1] > 11)
         * EnchantingConvergence.LOGGER.info("\t{}", sb);
         * }
         */

        this.calcEnchantingPower();

        ItemStack stack = inventoryIn.getItem(0);
        this.enchList.clear();

        if (stack.isEmpty()) {
            return;
        }

        this.enchList.addAll(EnchantingConvergenceHelper.getValidEnchantments(stack, this.player));
    }

    /**
     * Handles the given Button-click on the server, currently only used by enchanting. Name is for
     * legacy.
     */
    @Override
    public boolean clickMenuButton(Player playerIn, int id) {
        ItemStack stack = this.tableInventory.getItem(0);
        ItemStack stack1 = this.tableInventory.getItem(1);
        EnchantingMode mode = (id >>> 31) == 1 ? EnchantingMode.BOOK : EnchantingMode.NORMAL;
        int enchantmentId = id & 0x7FFFFFFF;
        int level = playerIn.experienceLevel;
        int lapis = stack1.isEmpty() ? 0 : stack1.getCount();
        int power = this.power.get();

        if (stack.isEmpty()) {
            return false;
        }

        if (mode == EnchantingMode.NORMAL) {
            if (enchantmentId >= this.enchList.size()) {
                return false;
            }

            Enchantment enchantment = this.enchList.get(enchantmentId);
            int enchantmentLevel = EnchantmentHelper.getEnchantments(stack).getOrDefault(enchantment, 0) + 1;

            if (enchantmentLevel > enchantment.getMaxLevel()) {
                return false;
            }

            Collection<Enchantment> enchantments =
                    EnchantmentHelper.getEnchantments(stack).keySet();
            if (!enchantments.contains(enchantment)
                    && !EnchantmentHelper.isEnchantmentCompatible(enchantments, enchantment)) {
                return false;
            }

            int levelCost = EnchantingConvergenceHelper.getLevelCost(stack, enchantment, enchantmentLevel);
            int lapisCost = EnchantingConvergenceHelper.getLapisCost(stack, enchantment, enchantmentLevel);
            int powerCost = EnchantingConvergenceHelper.getPowerCost(enchantment, enchantmentLevel);

            if ((!UnlockedEnchantmentHelper.isUnlocked(playerIn, enchantment, enchantmentLevel)
                            || levelCost > level
                            || lapisCost > lapis
                            || powerCost > power)
                    && !playerIn.isCreative()) {
                return false;
            }

            playerIn.onEnchantmentPerformed(stack, levelCost);

            if (!playerIn.isCreative()) {
                stack1.shrink(lapisCost);
                if (stack1.isEmpty()) {
                    this.tableInventory.setItem(1, ItemStack.EMPTY);
                }
            }

            if (stack.getItem() == Items.BOOK) {
                CompoundTag tag = stack.getTag();
                stack = new ItemStack(Items.ENCHANTED_BOOK);

                if (tag != null) {
                    stack.setTag(tag.copy());
                }

                setOrAddEnchantmentLevel(stack, enchantment, enchantmentLevel);
                this.tableInventory.setItem(0, stack);
            } else {
                setOrAddEnchantmentLevel(stack, enchantment, enchantmentLevel);
            }

            playerIn.awardStat(Stats.ENCHANT_ITEM);
            if (playerIn instanceof ServerPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer) playerIn, stack, 3);
            }

            this.tableInventory.setChanged();
            this.slotsChanged(this.tableInventory);
            this.worldPosCallable.execute((world, pos) -> world.playSound(
                    null,
                    pos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS,
                    1.0F,
                    world.random.nextFloat() * 0.1F + 0.9F));
        } else if (mode == EnchantingMode.BOOK) {
            if (stack.getItem() != Items.BOOK) {
                return false;
            }

            int levelCost = (enchantmentId + 1) * 10;
            int lapisCost = (enchantmentId + 1) * 5;
            int powerCost = enchantmentId * 4 + 3;

            if ((levelCost > level || lapisCost > lapis || powerCost > power) && !playerIn.isCreative()) {
                return false;
            }

            playerIn.onEnchantmentPerformed(stack, levelCost);

            if (!playerIn.isCreative()) {
                stack1.shrink(lapisCost);
                if (stack1.isEmpty()) {
                    this.tableInventory.setItem(1, ItemStack.EMPTY);
                }
            }

            CompoundTag tag = stack.getTag();
            stack = new ItemStack(Items.ENCHANTED_BOOK);

            if (tag != null) {
                stack.setTag(tag.copy());
            }

            List<MutableEnchantmentData> list = new ArrayList<>();
            double totalWeight = 0;
            for (Enchantment e : EnchantingConvergenceHelper.getValidEnchantments(stack, playerIn)) {
                for (int i = 1; i <= e.getMaxLevel(); i++) {
                    int p = EnchantingConvergenceHelper.getPowerCost(e, i);
                    if (p <= (enchantmentId - 1) * 4 + 3) {
                        continue;
                    }
                    if (p > enchantmentId * 4 + 3) {
                        continue;
                    }
                    double d = 1.0D;
                    if (UnlockedEnchantmentHelper.isUnlocked(playerIn, e, i)) {
                        d *= 0.25D;
                    }
                    MutableEnchantmentData enchData = new MutableEnchantmentData(e, i, d);
                    list.add(enchData);
                    totalWeight += enchData.weight;
                }
            }
            double weight = new Random().nextDouble() * totalWeight;
            MutableEnchantmentData selectedEnchData = null;
            for (MutableEnchantmentData enchData : list) {
                if ((weight -= enchData.weight) < 0) {
                    selectedEnchData = enchData;
                    break;
                }
            }
            if (selectedEnchData == null) {
                return false;
            }

            UnlockedEnchantmentHelper.unlock(playerIn, selectedEnchData.enchantment, selectedEnchData.level);
            EnchantedBookItem.addEnchantment(
                    stack, new EnchantmentInstance(selectedEnchData.enchantment, selectedEnchData.level));
            this.tableInventory.setItem(0, stack);

            playerIn.awardStat(Stats.ENCHANT_ITEM);
            if (playerIn instanceof ServerPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer) playerIn, stack, 3);
            }

            this.tableInventory.setChanged();
            this.slotsChanged(this.tableInventory);
            this.worldPosCallable.execute((world, pos) -> world.playSound(
                    null,
                    pos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS,
                    1.0F,
                    world.random.nextFloat() * 0.1F + 0.9F));
        }

        return true;
    }

    private static void setOrAddEnchantmentLevel(ItemStack stack, Enchantment enchantment, int level) {
        boolean flag = false;
        ResourceLocation registryName = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        ListTag enchantmentList = stack.getEnchantmentTags();

        //		for (int i = 0; i < enchantmentList.size(); i++) {} // empty for for some reason?
        for (Tag nbt : enchantmentList) {
            ResourceLocation resourcelocation1 = ResourceLocation.tryParse(((CompoundTag) nbt).getString("id"));
            if (resourcelocation1 != null && resourcelocation1.equals(registryName)) {
                ((CompoundTag) nbt).putInt("lvl", level);
                flag = true;
                break;
            }
        }

        if (!flag) {
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
                EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(enchantment, level));
            } else {
                stack.enchant(enchantment, level);
            }
        }
    }

    public List<Enchantment> getEnchantmentList() {
        return this.enchList;
    }

    public int getLapisAmount() {
        ItemStack stack = this.tableInventory.getItem(1);
        return stack.isEmpty() ? 0 : stack.getCount();
    }

    /** Called when the container is closed. */
    @Override
    public void removed(@NotNull Player playerIn) {
        super.removed(playerIn);
        this.worldPosCallable.execute((world, pos) -> this.clearContainer(playerIn, this.tableInventory));
    }

    /** Determines whether supplied player can use this container */
    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(this.worldPosCallable, playerIn, Blocks.ENCHANTING_TABLE);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack
     * between the player inventory and the other inventory(s).
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) { // original statement "slot != null && slot.hasItem()" but slot should never
            // be null
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 1) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.getItem() == Items.LAPIS_LAZULI) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemstack1)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack2 = itemstack1.copy();
                itemstack2.setCount(1);
                itemstack1.shrink(1);
                this.slots.get(0).set(itemstack2);
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}
