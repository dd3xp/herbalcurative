package com.cahcap.common.blockentity;

import com.cahcap.common.block.KilnBlock;
import com.cahcap.common.recipe.KilnCatalystRecipe;
import com.cahcap.common.recipe.KilnSmeltingRecipe;
import com.cahcap.common.util.ItemTransferHelper;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Block Entity for the Kiln multiblock structure.
 *
 * Features:
 * - Perpetually burning furnace (no fuel needed), same smelting speed as vanilla furnace
 * - Catalyst slot: Burnt Nodes double ore output
 * - Auto I/O from adjacent containers:
 *   - Right side (relative to front): input materials
 *   - Left side (relative to front): output products
 *   - Back side: input catalysts
 * - Hopper/pipe support via IItemHandler capability
 */
public class KilnBlockEntity extends MultiblockPartBlockEntity {

    // Smelting time in ticks (same as vanilla furnace: 200 ticks = 10 seconds)
    public static final int SMELT_TIME = 200;

    // Auto I/O interval in ticks
    private static final int AUTO_IO_INTERVAL = 10;

    // Storage
    private ItemStack inputSlot = ItemStack.EMPTY;
    private ItemStack catalystSlot = ItemStack.EMPTY;
    private ItemStack outputSlot = ItemStack.EMPTY;

    // Smelting state
    private int smeltProgress = 0;
    private boolean isSmelting = false;
    private boolean catalyzed = false; // Whether current smelt uses catalyst
    private int catalystUsesRemaining = 0; // Remaining uses from current catalyst item

    // Current catalyst properties (from recipe lookup, saved to NBT for persistence)
    private int currentCatalystOutputMultiplier = 1;
    private int currentCatalystSpeedMultiplier = 1;
    // Whether the current input is affected by catalyst output multiplier
    private boolean inputAffectedByCatalyst = false;

    // Cached render bounding box
    public AABB renderAABB = null;

    public KilnBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state, new int[]{3, 3, 3});
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<KilnBlockEntity> getBlockEntityType() {
        return (BlockEntityType<KilnBlockEntity>) ModRegistries.KILN_BE.get();
    }

    @Override
    public KilnBlockEntity getMaster() {
        return super.getMaster();
    }

    // ==================== Getters ====================

    public ItemStack getInputSlot() {
        KilnBlockEntity master = getMaster();
        return master != null ? master.inputSlot : ItemStack.EMPTY;
    }

    public ItemStack getCatalystSlot() {
        KilnBlockEntity master = getMaster();
        return master != null ? master.catalystSlot : ItemStack.EMPTY;
    }

    public ItemStack getOutputSlot() {
        KilnBlockEntity master = getMaster();
        return master != null ? master.outputSlot : ItemStack.EMPTY;
    }

    public boolean isSmelting() {
        KilnBlockEntity master = getMaster();
        return master != null && master.isSmelting;
    }

    public int getSmeltProgress() {
        KilnBlockEntity master = getMaster();
        return master != null ? master.smeltProgress : 0;
    }

    public boolean isCatalyzed() {
        KilnBlockEntity master = getMaster();
        return master != null && master.catalyzed;
    }

    public int getCurrentSmeltTime() {
        KilnBlockEntity master = getMaster();
        if (master == null) return SMELT_TIME;
        if (master.catalyzed && master.currentCatalystSpeedMultiplier > 1) {
            return SMELT_TIME / master.currentCatalystSpeedMultiplier;
        }
        return SMELT_TIME;
    }

    // ==================== Item Management ====================

    /**
     * Check if an item is a valid catalyst by looking up kiln catalyst recipes.
     */
    public boolean isCatalyst(ItemStack stack) {
        if (level == null || stack.isEmpty()) return false;
        return findCatalystRecipe(level, stack).isPresent();
    }

    /**
     * Find the catalyst recipe matching the given item stack.
     */
    private static Optional<KilnCatalystRecipe> findCatalystRecipe(Level level, ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return level.getRecipeManager()
                .getRecipeFor(ModRegistries.KILN_CATALYST_RECIPE_TYPE.get(), input, level)
                .map(RecipeHolder::value);
    }

    /**
     * Check if the current input is affected by the active catalyst's output multiplier.
     * Searches all catalyst recipes to find any that match the catalyst slot or have remaining uses.
     */
    private boolean checkInputAffected(Level level, ItemStack smeltInput) {
        // Try to find the recipe from catalyst slot first
        if (!catalystSlot.isEmpty()) {
            Optional<KilnCatalystRecipe> recipe = findCatalystRecipe(level, catalystSlot);
            if (recipe.isPresent()) {
                return recipe.get().isInputAffected(smeltInput);
            }
        }
        // Catalyst slot empty but uses remaining: search all catalyst recipes
        // to find one whose affectedInputs matches our smelt input.
        // Since we store the multiplier from the original recipe, use that to infer.
        // For simplicity, iterate all catalyst recipes.
        for (RecipeHolder<KilnCatalystRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(ModRegistries.KILN_CATALYST_RECIPE_TYPE.get())) {
            KilnCatalystRecipe recipe = holder.value();
            if (recipe.getOutputMultiplier() == currentCatalystOutputMultiplier
                    && recipe.getSpeedMultiplier() == currentCatalystSpeedMultiplier) {
                return recipe.isInputAffected(smeltInput);
            }
        }
        return false;
    }

    /**
     * Add input items. Returns the number added.
     * Input slot caches up to 1 stack (64 of same item).
     */
    public int addInput(ItemStack stack, boolean isCreative) {
        KilnBlockEntity master = getMaster();
        if (master == null) return 0;

        if (master.inputSlot.isEmpty()) {
            int toAdd = Math.min(stack.getCount(), stack.getMaxStackSize());
            master.inputSlot = stack.copyWithCount(toAdd);
            if (!isCreative) stack.shrink(toAdd);
            master.setChanged();
            master.syncToClient();
            return toAdd;
        } else if (ItemStack.isSameItemSameComponents(master.inputSlot, stack)) {
            int current = master.inputSlot.getCount();
            int canAdd = Math.min(stack.getCount(), master.inputSlot.getMaxStackSize() - current);
            if (canAdd <= 0) return 0;
            master.inputSlot.grow(canAdd);
            if (!isCreative) stack.shrink(canAdd);
            master.setChanged();
            master.syncToClient();
            return canAdd;
        }
        return 0;
    }

    /**
     * Add catalyst items. Returns the number added.
     */
    public int addCatalyst(ItemStack stack, boolean isCreative) {
        KilnBlockEntity master = getMaster();
        if (master == null || !isCatalyst(stack)) return 0;

        int current = master.catalystSlot.isEmpty() ? 0 : master.catalystSlot.getCount();
        int canAdd = Math.min(stack.getCount(), 64 - current);
        if (canAdd <= 0) return 0;

        if (master.catalystSlot.isEmpty()) {
            master.catalystSlot = stack.copyWithCount(canAdd);
        } else {
            master.catalystSlot.grow(canAdd);
        }

        if (!isCreative) stack.shrink(canAdd);
        master.setChanged();
        master.syncToClient();
        return canAdd;
    }

    public ItemStack extractInput() {
        KilnBlockEntity master = getMaster();
        if (master == null || master.inputSlot.isEmpty() || master.isSmelting) return ItemStack.EMPTY;

        ItemStack extracted = master.inputSlot.copy();
        master.inputSlot = ItemStack.EMPTY;
        master.setChanged();
        master.syncToClient();
        return extracted;
    }

    public ItemStack extractCatalyst() {
        KilnBlockEntity master = getMaster();
        if (master == null || master.catalystSlot.isEmpty()) return ItemStack.EMPTY;

        ItemStack extracted = master.catalystSlot.copy();
        master.catalystSlot = ItemStack.EMPTY;
        master.setChanged();
        master.syncToClient();
        return extracted;
    }

    public ItemStack extractOutput() {
        KilnBlockEntity master = getMaster();
        if (master == null || master.outputSlot.isEmpty()) return ItemStack.EMPTY;

        ItemStack extracted = master.outputSlot.copy();
        master.outputSlot = ItemStack.EMPTY;
        master.setChanged();
        master.syncToClient();
        return extracted;
    }

    /**
     * Set the LIT block state for all blocks in the structure.
     * Only sets the master block's LIT state (light propagates from there).
     */
    private void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.getValue(KilnBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(KilnBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    // Cached recipe preview (invalidated when input changes)
    private ItemStack cachedPreviewInput = ItemStack.EMPTY;
    private ItemStack cachedPreviewResult = ItemStack.EMPTY;

    /**
     * Get the expected smelting result for the current input (for tooltip preview).
     * Cached to avoid recipe lookups every render frame.
     */
    public ItemStack getRecipePreview() {
        KilnBlockEntity master = getMaster();
        if (master == null || master.inputSlot.isEmpty() || level == null) return ItemStack.EMPTY;
        if (!ItemStack.isSameItemSameComponents(master.inputSlot, cachedPreviewInput)) {
            cachedPreviewInput = master.inputSlot.copy();
            Optional<ItemStack> result = getSmeltingResult(level, master.inputSlot);
            cachedPreviewResult = result.map(ItemStack::copy).orElse(ItemStack.EMPTY);
        }
        return cachedPreviewResult;
    }

    // ==================== Smelting Logic ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, KilnBlockEntity blockEntity) {
        if (!blockEntity.isMaster()) return;

        int tickCount = (int) (level.getGameTime() % AUTO_IO_INTERVAL);

        // Auto I/O from adjacent containers
        if (tickCount == 0) {
            blockEntity.handleAutoIO(level);
        }

        // Try to start smelting
        if (!blockEntity.isSmelting && !blockEntity.inputSlot.isEmpty()) {
            Optional<ItemStack> result = getSmeltingResult(level, blockEntity.inputSlot);
            if (result.isPresent() && canOutput(blockEntity, result.get())) {
                blockEntity.isSmelting = true;
                blockEntity.smeltProgress = 0;
                // Check catalyst: use remaining uses, or consume a new one
                if (blockEntity.catalystUsesRemaining > 0) {
                    blockEntity.catalyzed = true;
                    // Re-check if input is affected (input may have changed)
                    blockEntity.inputAffectedByCatalyst = blockEntity.checkInputAffected(level, blockEntity.inputSlot);
                } else if (!blockEntity.catalystSlot.isEmpty()) {
                    Optional<KilnCatalystRecipe> catalystRecipe = findCatalystRecipe(level, blockEntity.catalystSlot);
                    if (catalystRecipe.isPresent()) {
                        KilnCatalystRecipe recipe = catalystRecipe.get();
                        blockEntity.catalyzed = true;
                        blockEntity.catalystSlot.shrink(1);
                        if (blockEntity.catalystSlot.isEmpty()) {
                            blockEntity.catalystSlot = ItemStack.EMPTY;
                        }
                        blockEntity.catalystUsesRemaining = recipe.getUsesPerItem();
                        blockEntity.currentCatalystOutputMultiplier = recipe.getOutputMultiplier();
                        blockEntity.currentCatalystSpeedMultiplier = recipe.getSpeedMultiplier();
                        blockEntity.inputAffectedByCatalyst = recipe.isInputAffected(blockEntity.inputSlot);
                    } else {
                        blockEntity.catalyzed = false;
                    }
                } else {
                    blockEntity.catalyzed = false;
                }
                blockEntity.setLit(level, pos, state, true);
                blockEntity.setChanged();
                blockEntity.syncToClient();
            }
        }

        // Progress smelting
        if (blockEntity.isSmelting) {
            blockEntity.smeltProgress++;

            int targetTime = blockEntity.catalyzed ? SMELT_TIME / blockEntity.currentCatalystSpeedMultiplier : SMELT_TIME;
            if (blockEntity.smeltProgress >= targetTime) {
                // Complete smelting
                Optional<ItemStack> result = getSmeltingResult(level, blockEntity.inputSlot);
                if (result.isPresent()) {
                    ItemStack output = result.get().copy();

                    // Catalyst multiplies output (only for affected inputs) and consumes 1 use
                    if (blockEntity.catalyzed) {
                        if (blockEntity.inputAffectedByCatalyst) {
                            output.setCount(output.getCount() * blockEntity.currentCatalystOutputMultiplier);
                        }
                        blockEntity.catalystUsesRemaining--;
                    }

                    // Place in output slot
                    if (blockEntity.outputSlot.isEmpty()) {
                        blockEntity.outputSlot = output;
                    } else if (ItemStack.isSameItemSameComponents(blockEntity.outputSlot, output)) {
                        blockEntity.outputSlot.grow(output.getCount());
                    }

                    // Consume input
                    blockEntity.inputSlot.shrink(1);
                    if (blockEntity.inputSlot.isEmpty()) {
                        blockEntity.inputSlot = ItemStack.EMPTY;
                    }
                }

                blockEntity.isSmelting = false;
                blockEntity.smeltProgress = 0;
                blockEntity.catalyzed = false;

                // Only turn off light if there's nothing left to smelt
                boolean canContinue = !blockEntity.inputSlot.isEmpty()
                        && getSmeltingResult(level, blockEntity.inputSlot).isPresent();
                if (!canContinue) {
                    blockEntity.setLit(level, pos, level.getBlockState(pos), false);
                }
                blockEntity.setChanged();
                blockEntity.syncToClient();
            } else if (blockEntity.smeltProgress % 40 == 0) {
                blockEntity.syncToClient();
            }
        }
    }

    private static Optional<ItemStack> getSmeltingResult(Level level, ItemStack input) {
        if (input.isEmpty()) return Optional.empty();

        RecipeManager recipeManager = level.getRecipeManager();
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);

        // First try custom kiln recipes
        Optional<RecipeHolder<KilnSmeltingRecipe>> kilnRecipe = recipeManager.getRecipeFor(
                ModRegistries.KILN_SMELTING_RECIPE_TYPE.get(), recipeInput, level);
        if (kilnRecipe.isPresent()) {
            return Optional.of(kilnRecipe.get().value().getResultItem(level.registryAccess()));
        }

        // Fall back to vanilla smelting recipes
        Optional<RecipeHolder<SmeltingRecipe>> smeltingRecipe = recipeManager.getRecipeFor(
                RecipeType.SMELTING, recipeInput, level);
        return smeltingRecipe.map(r -> r.value().getResultItem(level.registryAccess()));
    }

    private static boolean canOutput(KilnBlockEntity be, ItemStack result) {
        if (be.outputSlot.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(be.outputSlot, result)) return false;
        // Calculate output count based on catalyst multiplier
        int multiplier = 1;
        if (be.catalystUsesRemaining > 0) {
            multiplier = be.currentCatalystOutputMultiplier;
        } else if (!be.catalystSlot.isEmpty() && be.level != null) {
            Optional<KilnCatalystRecipe> recipe = findCatalystRecipe(be.level, be.catalystSlot);
            multiplier = recipe.map(KilnCatalystRecipe::getOutputMultiplier).orElse(1);
        }
        int outputCount = result.getCount() * multiplier;
        return be.outputSlot.getCount() + outputCount <= be.outputSlot.getMaxStackSize();
    }

    // ==================== Auto I/O ====================

    private void handleAutoIO(Level level) {
        if (ItemTransferHelper.INSTANCE == null) return;

        Direction front = facing;
        Direction right = mirrored ? front.getCounterClockWise() : front.getClockWise();
        Direction left = mirrored ? front.getClockWise() : front.getCounterClockWise();
        Direction back = front.getOpposite();

        // Auto I/O positions: adjacent to the Layer 1 center block (one block below master).
        // Layer 1 center = master.below(), container is 2 blocks out (1 = structure edge, 1 = container)
        BlockPos layer1Center = worldPosition.below();

        // Right side of front: auto-input materials
        BlockPos inputPos = layer1Center.relative(right, 2);
        autoInputFrom(level, inputPos, right.getOpposite());

        // Back side: auto-input catalysts
        BlockPos catalystPos = layer1Center.relative(back, 2);
        autoInputCatalystFrom(level, catalystPos, back.getOpposite());

        // Left side of front: auto-output products
        BlockPos outputPos = layer1Center.relative(left, 2);
        autoOutputTo(level, outputPos, left.getOpposite());
    }

    private void autoInputFrom(Level level, BlockPos containerPos, Direction accessSide) {
        int current = inputSlot.isEmpty() ? 0 : inputSlot.getCount();
        int maxStack = inputSlot.isEmpty() ? 64 : inputSlot.getMaxStackSize();
        if (current >= maxStack) return;

        int canAdd = maxStack - current;
        ItemStack extracted = ItemTransferHelper.INSTANCE.extractItem(level, containerPos, accessSide, canAdd,
                stack -> {
                    if (inputSlot.isEmpty()) return getSmeltingResult(level, stack).isPresent();
                    return ItemStack.isSameItemSameComponents(inputSlot, stack);
                });

        if (!extracted.isEmpty()) {
            if (inputSlot.isEmpty()) {
                inputSlot = extracted;
            } else {
                inputSlot.grow(extracted.getCount());
            }
            setChanged();
            syncToClient();
        }
    }

    private void autoInputCatalystFrom(Level level, BlockPos containerPos, Direction accessSide) {
        int current = catalystSlot.isEmpty() ? 0 : catalystSlot.getCount();
        if (current >= 64) return;

        int canAdd = 64 - current;
        ItemStack extracted = ItemTransferHelper.INSTANCE.extractItem(level, containerPos, accessSide, canAdd,
                this::isCatalyst);

        if (!extracted.isEmpty()) {
            if (catalystSlot.isEmpty()) {
                catalystSlot = extracted;
            } else {
                catalystSlot.grow(extracted.getCount());
            }
            setChanged();
            syncToClient();
        }
    }

    private void autoOutputTo(Level level, BlockPos containerPos, Direction accessSide) {
        if (outputSlot.isEmpty()) return;

        ItemStack remainder = ItemTransferHelper.INSTANCE.insertItem(level, containerPos, accessSide, outputSlot.copy());
        if (remainder.getCount() < outputSlot.getCount()) {
            outputSlot = remainder.isEmpty() ? ItemStack.EMPTY : remainder;
            setChanged();
            syncToClient();
        }
    }

    // ==================== Multiblock Management ====================

    @Override
    public void disassemble() {
        if (level == null || level.isClientSide || !formed) {
            return;
        }

        KilnBlockEntity master = getMaster();
        if (master == null) {
            return;
        }

        BlockPos masterPos = master.getBlockPos();
        BlockPos breakPos = getBlockPos();

        // Drop stored items
        dropItem(level, masterPos, master.inputSlot);
        dropItem(level, masterPos, master.catalystSlot);
        dropItem(level, masterPos, master.outputSlot);
        master.inputSlot = ItemStack.EMPTY;
        master.catalystSlot = ItemStack.EMPTY;
        master.outputSlot = ItemStack.EMPTY;

        // Get all block positions in the structure
        List<BlockPos> structurePositions = getStructurePositions(masterPos);

        // First pass: Mark all blocks as not formed
        for (BlockPos targetPos : structurePositions) {
            if (level.getBlockState(targetPos).is(ModRegistries.KILN.get())) {
                if (level.getBlockEntity(targetPos) instanceof KilnBlockEntity kiln) {
                    kiln.formed = false;
                    kiln.renderAABB = null;

                    if (!targetPos.equals(breakPos)) {
                        kiln.suppressDrops = true;
                    }

                    kiln.setChanged();
                    BlockState state = level.getBlockState(targetPos);
                    level.sendBlockUpdated(targetPos, state, state, 3);
                }
            }
        }

        // Second pass: Replace non-broken blocks with original blocks
        for (BlockPos targetPos : structurePositions) {
            if (level.getBlockState(targetPos).is(ModRegistries.KILN.get())) {
                if (!targetPos.equals(breakPos)) {
                    BlockState original = null;
                    if (level.getBlockEntity(targetPos) instanceof KilnBlockEntity kiln) {
                        original = kiln.originalBlockState;
                    }
                    if (original == null) {
                        original = getOriginalBlockForPosition(targetPos, masterPos);
                    }
                    level.setBlock(targetPos, original, 2);
                }
            }
        }

        setChanged();
    }

    private void dropItem(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity entity = new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    stack.copy()
            );
            level.addFreshEntity(entity);
        }
    }

    private List<BlockPos> getStructurePositions(BlockPos masterPos) {
        List<BlockPos> positions = new ArrayList<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(masterPos.offset(x, dy, z));
                }
            }
        }
        return positions;
    }

    /**
     * Get the original block state for a position in the multiblock.
     */
    public BlockState getOriginalBlockForPosition(BlockPos targetPos, BlockPos masterPos) {
        int dx = targetPos.getX() - masterPos.getX();
        int dy = targetPos.getY() - masterPos.getY();
        int dz = targetPos.getZ() - masterPos.getZ();

        if (dy == -1) {
            // Layer 1: center = soul sand (default pyrisage-plantable), rest = lumistone bricks
            if (dx == 0 && dz == 0) {
                return Blocks.SOUL_SAND.defaultBlockState();
            }
            return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
        } else if (dy == 0) {
            // Layer 2: center = pyrisage, front = lumistone brick slab (top),
            // rest = lumistone bricks
            if (dx == 0 && dz == 0) {
                // Restore pyrisage on soul sand when disassembled
                return ModRegistries.PYRISAGE.get().defaultBlockState();
            }
            // Check if this is the front position
            Direction relDir = getRelativeDirection(dx, dz);
            if (relDir == facing) {
                return ModRegistries.LUMISTONE_BRICK_SLAB.get().defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP);
            }
            return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
        } else if (dy == 1) {
            // Layer 3: center column (along facing axis) = lumistone bricks, sides = slabs
            boolean facingAlongZ = (facing == Direction.NORTH || facing == Direction.SOUTH);
            boolean isCenterColumn = facingAlongZ ? (dx == 0) : (dz == 0);
            if (isCenterColumn) {
                return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
            } else {
                return ModRegistries.LUMISTONE_BRICK_SLAB.get().defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            }
        }

        return Blocks.AIR.defaultBlockState();
    }

    /**
     * Get the item that should drop for a position in the multiblock.
     */
    public ItemStack getOriginalItemForPosition(BlockPos targetPos, BlockPos masterPos) {
        BlockState state = getOriginalBlockForPosition(targetPos, masterPos);
        if (state.isAir()) return ItemStack.EMPTY;
        return new ItemStack(state.getBlock());
    }

    private Direction getRelativeDirection(int x, int z) {
        if (x == 0 && z == -1) return Direction.NORTH;
        if (x == 0 && z == 1) return Direction.SOUTH;
        if (x == -1 && z == 0) return Direction.WEST;
        if (x == 1 && z == 0) return Direction.EAST;
        return null;
    }

    @Override
    public ItemStack getOriginalBlock() {
        return new ItemStack(ModRegistries.LUMISTONE_BRICKS.get());
    }

    @Override
    public BlockState getOriginalBlockState() {
        if (originalBlockState != null) {
            return originalBlockState;
        }
        // Legacy fallback for worlds saved before original block memorization
        BlockPos masterPos = getMasterPos();
        if (masterPos == null) {
            return ModRegistries.LUMISTONE_BRICKS.get().defaultBlockState();
        }
        return getOriginalBlockForPosition(getBlockPos(), masterPos);
    }

    // ==================== NBT Serialization ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (!inputSlot.isEmpty()) {
            tag.put("Input", inputSlot.save(registries));
        }
        if (!catalystSlot.isEmpty()) {
            tag.put("Catalyst", catalystSlot.save(registries));
        }
        if (!outputSlot.isEmpty()) {
            tag.put("Output", outputSlot.save(registries));
        }

        tag.putInt("SmeltProgress", smeltProgress);
        tag.putBoolean("IsSmelting", isSmelting);
        tag.putBoolean("Catalyzed", catalyzed);
        tag.putInt("CatalystUsesRemaining", catalystUsesRemaining);
        tag.putInt("CatalystOutputMultiplier", currentCatalystOutputMultiplier);
        tag.putInt("CatalystSpeedMultiplier", currentCatalystSpeedMultiplier);
        tag.putBoolean("InputAffectedByCatalyst", inputAffectedByCatalyst);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        inputSlot = tag.contains("Input") ?
                ItemStack.parse(registries, tag.getCompound("Input")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        catalystSlot = tag.contains("Catalyst") ?
                ItemStack.parse(registries, tag.getCompound("Catalyst")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        outputSlot = tag.contains("Output") ?
                ItemStack.parse(registries, tag.getCompound("Output")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;

        smeltProgress = tag.getInt("SmeltProgress");
        isSmelting = tag.getBoolean("IsSmelting");
        catalyzed = tag.getBoolean("Catalyzed");
        catalystUsesRemaining = tag.getInt("CatalystUsesRemaining");
        currentCatalystOutputMultiplier = tag.getInt("CatalystOutputMultiplier");
        if (currentCatalystOutputMultiplier <= 0) currentCatalystOutputMultiplier = 1;
        currentCatalystSpeedMultiplier = tag.getInt("CatalystSpeedMultiplier");
        if (currentCatalystSpeedMultiplier <= 0) currentCatalystSpeedMultiplier = 1;
        inputAffectedByCatalyst = tag.getBoolean("InputAffectedByCatalyst");
    }

    // ==================== Render ====================

    /**
     * Compute render bounding box lazily.
     * Renderer can call this to get the expanded AABB.
     */
    public AABB computeRenderAABB() {
        if (renderAABB == null && formed) {
            BlockPos masterPos = getMasterPos();
            if (masterPos != null) {
                renderAABB = new AABB(
                        masterPos.getX() - 1, masterPos.getY() - 1, masterPos.getZ() - 1,
                        masterPos.getX() + 2, masterPos.getY() + 2, masterPos.getZ() + 2
                );
            }
        }
        return renderAABB;
    }
}
