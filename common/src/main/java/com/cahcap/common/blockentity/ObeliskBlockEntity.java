package com.cahcap.common.blockentity;

import com.cahcap.common.recipe.ObeliskOfferingRecipe;
import com.cahcap.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Block Entity for the Obelisk multiblock structure.
 *
 * Features:
 * - Players offer food items by right-clicking the formed obelisk
 * - After a configurable wait time, a mob spawns in front of the obelisk
 * - Simple static recipe list (to be made data-driven later)
 */
public class ObeliskBlockEntity extends MultiblockPartBlockEntity {

    // ==================== State ====================

    private ItemStack offeringItem = ItemStack.EMPTY;
    private int offeringTimer = 0;
    private int totalOfferingTime = 0;
    @Nullable
    private EntityType<?> pendingMobType = null;
    private int spawnDistance = 1;

    // Cached render bounding box
    public AABB renderAABB = null;

    public ObeliskBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(), pos, state, new int[]{3, 3, 3});
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<ObeliskBlockEntity> getBlockEntityType() {
        return (BlockEntityType<ObeliskBlockEntity>) ModRegistries.OBELISK_BE.get();
    }

    @Override
    public ObeliskBlockEntity getMaster() {
        return super.getMaster();
    }

    // ==================== Getters ====================

    public ItemStack getOfferingItem() {
        ObeliskBlockEntity master = getMaster();
        return master != null ? master.offeringItem : ItemStack.EMPTY;
    }

    public boolean isOffering() {
        ObeliskBlockEntity master = getMaster();
        return master != null && master.offeringTimer > 0;
    }

    public int getOfferingTimer() {
        ObeliskBlockEntity master = getMaster();
        return master != null ? master.offeringTimer : 0;
    }

    public int getTotalOfferingTime() {
        ObeliskBlockEntity master = getMaster();
        return master != null ? master.totalOfferingTime : 0;
    }

    public float getOfferingProgress() {
        ObeliskBlockEntity master = getMaster();
        if (master == null || master.totalOfferingTime <= 0) return 0f;
        return 1.0f - ((float) master.offeringTimer / master.totalOfferingTime);
    }

    // ==================== Recipe Lookup ====================

    /**
     * Find a matching recipe for the given item stack via RecipeManager.
     */
    @Nullable
    public ObeliskOfferingRecipe findRecipe(ItemStack stack) {
        if (stack.isEmpty() || level == null) return null;
        SingleRecipeInput input = new SingleRecipeInput(stack);
        Optional<RecipeHolder<ObeliskOfferingRecipe>> holder = level.getRecipeManager()
                .getRecipeFor(ModRegistries.OBELISK_OFFERING_RECIPE_TYPE.get(), input, level);
        return holder.map(RecipeHolder::value).orElse(null);
    }

    // ==================== Offering ====================

    /**
     * Start the offering process. Consumes one item from the player's stack.
     */
    public void startOffering(ItemStack playerStack, ObeliskOfferingRecipe recipe, boolean isCreative) {
        ObeliskBlockEntity master = getMaster();
        if (master == null) return;

        master.offeringItem = playerStack.copyWithCount(1);
        master.offeringTimer = recipe.getWaitTicks();
        master.totalOfferingTime = recipe.getWaitTicks();
        master.pendingMobType = EntityType.byString(recipe.getEntityType().toString()).orElse(null);
        master.spawnDistance = recipe.getSpawnDistance();

        if (!isCreative) {
            playerStack.shrink(1);
        }

        master.setChanged();
        master.syncToClient();
    }

    /**
     * Start offering from automation (hopper/pipe). Item already consumed by the handler.
     */
    public void startOfferingFromAutomation(ItemStack offeringStack, ObeliskOfferingRecipe recipe) {
        ObeliskBlockEntity master = getMaster();
        if (master == null) return;

        master.offeringItem = offeringStack.copy();
        master.offeringTimer = recipe.getWaitTicks();
        master.totalOfferingTime = recipe.getWaitTicks();
        master.pendingMobType = EntityType.byString(recipe.getEntityType().toString()).orElse(null);
        master.spawnDistance = recipe.getSpawnDistance();

        master.setChanged();
        master.syncToClient();
    }

    /**
     * Cancel the offering and return the item to the player.
     */
    public ItemStack cancelOffering() {
        ObeliskBlockEntity master = getMaster();
        if (master == null || master.offeringTimer <= 0) return ItemStack.EMPTY;

        ItemStack returned = master.offeringItem.copy();
        master.offeringItem = ItemStack.EMPTY;
        master.offeringTimer = 0;
        master.totalOfferingTime = 0;
        master.pendingMobType = null;
        master.spawnDistance = 1;

        master.setChanged();
        master.syncToClient();
        return returned;
    }

    // ==================== Server Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, ObeliskBlockEntity blockEntity) {
        if (!blockEntity.isMaster()) return;

        if (blockEntity.offeringTimer <= 0) return;

        blockEntity.offeringTimer--;

        if (blockEntity.offeringTimer <= 0) {
            // Offering complete: spawn mob
            if (blockEntity.pendingMobType != null && level instanceof ServerLevel serverLevel) {
                // Pedestal is at the front of the structure; spawn mob in front of pedestal
                BlockPos offeringTablePos = blockEntity.getOfferingTablePos();
                if (offeringTablePos == null) offeringTablePos = pos;
                BlockPos spawnPos = offeringTablePos.relative(
                        blockEntity.facing, blockEntity.spawnDistance);

                Entity entity = blockEntity.pendingMobType.spawn(
                        serverLevel,
                        spawnPos,
                        MobSpawnType.MOB_SUMMONED
                );
            }

            // Clear offering state
            blockEntity.offeringItem = ItemStack.EMPTY;
            blockEntity.offeringTimer = 0;
            blockEntity.totalOfferingTime = 0;
            blockEntity.pendingMobType = null;
            blockEntity.spawnDistance = 1;

            blockEntity.setChanged();
            blockEntity.syncToClient();
        } else if (blockEntity.offeringTimer % 20 == 0) {
            // Periodic sync for client-side animation
            blockEntity.syncToClient();
        }
    }

    // ==================== Multiblock Management ====================

    @Override
    public void disassemble() {
        if (level == null || level.isClientSide || !formed) {
            return;
        }

        ObeliskBlockEntity master = getMaster();
        if (master == null) {
            return;
        }

        BlockPos masterPos = master.getBlockPos();
        BlockPos breakPos = getBlockPos();

        // Drop offering item if present
        dropItem(level, masterPos, master.offeringItem);
        master.offeringItem = ItemStack.EMPTY;
        master.offeringTimer = 0;
        master.totalOfferingTime = 0;
        master.pendingMobType = null;
        master.spawnDistance = 1;

        // Get all block positions in the structure
        List<BlockPos> structurePositions = getStructurePositions(masterPos);

        // First pass: Mark all blocks as not formed
        for (BlockPos targetPos : structurePositions) {
            if (level.getBlockState(targetPos).is(ModRegistries.OBELISK.get())) {
                if (level.getBlockEntity(targetPos) instanceof ObeliskBlockEntity obelisk) {
                    obelisk.formed = false;
                    obelisk.renderAABB = null;

                    if (!targetPos.equals(breakPos)) {
                        obelisk.suppressDrops = true;
                    }

                    obelisk.setChanged();
                    BlockState blockState = level.getBlockState(targetPos);
                    level.sendBlockUpdated(targetPos, blockState, blockState, 3);
                }
            }
        }

        // Second pass: Replace non-broken blocks with original blocks
        for (BlockPos targetPos : structurePositions) {
            if (level.getBlockState(targetPos).is(ModRegistries.OBELISK.get())) {
                if (!targetPos.equals(breakPos)) {
                    BlockState original = null;
                    if (level.getBlockEntity(targetPos) instanceof ObeliskBlockEntity obelisk) {
                        original = obelisk.originalBlockState;
                    }
                    if (original == null) {
                        original = ModRegistries.LUMISTONE.get().defaultBlockState();
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

    @Override
    public ItemStack getOriginalBlock() {
        return new ItemStack(ModRegistries.LUMISTONE.get());
    }

    @Override
    public BlockState getOriginalBlockState() {
        if (originalBlockState != null) {
            return originalBlockState;
        }
        return ModRegistries.LUMISTONE.get().defaultBlockState();
    }

    // ==================== NBT Serialization ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (!offeringItem.isEmpty()) {
            tag.put("OfferingItem", offeringItem.save(registries));
        }
        tag.putInt("OfferingTimer", offeringTimer);
        tag.putInt("TotalOfferingTime", totalOfferingTime);
        tag.putInt("SpawnDistance", spawnDistance);

        if (pendingMobType != null) {
            tag.putString("PendingMobType", EntityType.getKey(pendingMobType).toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        offeringItem = tag.contains("OfferingItem") ?
                ItemStack.parse(registries, tag.getCompound("OfferingItem")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;

        offeringTimer = tag.getInt("OfferingTimer");
        totalOfferingTime = tag.getInt("TotalOfferingTime");
        spawnDistance = tag.getInt("SpawnDistance");
        if (spawnDistance <= 0) spawnDistance = 1;

        if (tag.contains("PendingMobType")) {
            pendingMobType = EntityType.byString(tag.getString("PendingMobType")).orElse(null);
        } else {
            pendingMobType = null;
        }
    }

    // ==================== Coordinate Helpers ====================

    /**
     * Rotate a blueprint offset to world coordinates based on facing direction.
     * Blueprint uses default facing NORTH (-Z = front).
     */
    private static BlockPos rotateOffset(BlockPos offset, Direction facing) {
        int x = offset.getX(), y = offset.getY(), z = offset.getZ();
        return switch (facing) {
            case NORTH -> offset;
            case SOUTH -> new BlockPos(-x, y, -z);
            case EAST  -> new BlockPos(-z, y, x);
            case WEST  -> new BlockPos(z, y, -x);
            default    -> offset;
        };
    }

    /**
     * Get the world position of the offering pedestal block.
     * Blockbench element "offeringpedestal": from=[0,-16,-12] to=[16,-4,0]
     * Blueprint and model both use NORTH default, so coords match directly.
     * → block offset (0, -1, -1) in blueprint space.
     */
    public BlockPos getOfferingTablePos() {
        BlockPos masterPos = getMasterPos();
        if (masterPos == null) return null;
        BlockPos offeringTableOffset = rotateOffset(new BlockPos(0, -1, -1), facing);
        return masterPos.offset(offeringTableOffset);
    }

    /**
     * Get the precise pedestal top-center position in world space.
     * Blockbench coords: center X=8, top Y=-4, center Z=-6
     * Blueprint = model space (both NORTH default), no flip needed.
     * Offset from master center: (0, _, -0.875).
     */
    public double[] getPedestalCenter() {
        BlockPos masterPos = getMasterPos();
        if (masterPos == null) return null;
        // Offset from master block center in blueprint space (default NORTH = model space)
        float dx = 0.0f;
        float dz = -0.875f;
        // Rotate (dx, dz) by facing (NORTH = identity)
        float rx, rz;
        switch (facing) {
            case SOUTH -> { rx = -dx; rz = -dz; }
            case EAST  -> { rx = -dz; rz = dx; }
            case WEST  -> { rx = dz;  rz = -dx; }
            default    -> { rx = dx;  rz = dz; } // NORTH (default)
        }
        return new double[]{
                masterPos.getX() + 0.5 + rx,
                masterPos.getY() - 0.25,
                masterPos.getZ() + 0.5 + rz
        };
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
