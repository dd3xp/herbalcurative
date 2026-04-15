package com.cahcap.common.multiblock;

import com.cahcap.common.blockentity.MultiblockPartBlockEntity;
import com.cahcap.common.util.CustomVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Data-driven multiblock system.
 * <p>
 * Each multiblock structure is defined as a {@code Multiblock} instance built via
 * {@link #builder()}. The 3D pattern uses character symbols mapped to block predicates.
 * One unified algorithm handles trigger detection, structure validation, facing detection,
 * and assembly for all multiblock types.
 * <p>
 * Blueprints are defined with a default facing of NORTH (-Z = front).
 * This matches Blockbench's model coordinate system (y=0 = NORTH).
 * Rotation is handled automatically during validation and assembly.
 * <p>
 * Assembly order: setBlock with flags 0 first (no client notify), configure BlockEntities,
 * then sendBlockUpdated. This ensures the client receives block + BE data together.
 */
public class Multiblock {

    // ---- Shared block state properties for all multiblock blocks ----

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");

    /**
     * Add properties for multiblock blocks with interior space (player can stand inside).
     * Avoids suffocation/view-blocking when BE data arrives after block state.
     */
    public static BlockBehaviour.Properties addInteriorSpaceProperties(BlockBehaviour.Properties props) {
        return props;
    }

    // ---- Instance fields (blueprint data) ----

    private final List<PatternEntry> entries;
    private final Set<Character> triggerSymbols;
    private final Map<Character, Predicate<BlockState>> predicates;
    private final Supplier<Block> resultBlock;
    private final boolean supportsMirroring;
    private final SoundEvent sound;
    private final float soundVolume;
    private final float soundPitch;

    private record PatternEntry(BlockPos offset, char symbol, boolean isMaster, int positionIndex) {}

    private record BlockTransform(BlockPos offsetFromMaster, boolean isMaster, int posInMultiblock, int positionIndex) {
        BlockPos worldPos(BlockPos masterPos) {
            return masterPos.offset(offsetFromMaster.getX(), offsetFromMaster.getY(), offsetFromMaster.getZ());
        }
    }

    private Multiblock(List<PatternEntry> entries, Set<Character> triggerSymbols,
                       Map<Character, Predicate<BlockState>> predicates,
                       Supplier<Block> resultBlock, boolean supportsMirroring,
                       SoundEvent sound, float soundVolume, float soundPitch) {
        this.entries = entries;
        this.triggerSymbols = triggerSymbols;
        this.predicates = predicates;
        this.resultBlock = resultBlock;
        this.supportsMirroring = supportsMirroring;
        this.sound = sound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
    }

    // ---- Public API ----

    /**
     * Quick check: can the given block state trigger this multiblock's assembly?
     */
    public boolean isBlockTrigger(BlockState state) {
        for (char symbol : triggerSymbols) {
            if (predicates.get(symbol).test(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unified assembly entry point.
     * Tries all trigger positions × all rotations, validates, picks the best match
     * For symmetric structures with triggers on multiple sides, the facing is
     * determined by which trigger block was clicked (the clicked side becomes the front).
     * Falls back to player facing for center triggers with no horizontal offset.
     *
     * @param level      The world
     * @param clickedPos The position the player clicked
     * @param side       The face of the block that was clicked (nullable)
     * @param player     The player
     * @return true if a valid structure was found and assembled
     */
    public boolean tryAssemble(Level level, BlockPos clickedPos,
                               @Nullable Direction side, Player player) {
        if (level.isClientSide) return false;

        // Fallback for triggers at the center (no horizontal offset from master)
        Direction playerFacing = (side != null && side.getAxis() != Direction.Axis.Y)
                ? side : player.getDirection().getOpposite();

        // Collect trigger entries
        List<PatternEntry> triggers = new ArrayList<>();
        for (PatternEntry e : entries) {
            if (triggerSymbols.contains(e.symbol())) {
                triggers.add(e);
            }
        }

        // Try all trigger positions × all rotations × mirror states
        record Match(BlockPos masterPos, Direction facing, boolean mirrored) {}
        Match preferredMatch = null;
        Match anyMatch = null;
        Set<Long> tried = new HashSet<>();
        boolean[] mirrorOptions = supportsMirroring ? new boolean[]{false, true} : new boolean[]{false};

        for (PatternEntry trigger : triggers) {
            for (boolean mirrored : mirrorOptions) {
                for (Direction facing : Direction.Plane.HORIZONTAL) {
                    BlockPos blueprintOffset = mirrored ? mirrorOffset(trigger.offset()) : trigger.offset();
                    BlockPos rotatedOffset = rotateOffset(blueprintOffset, facing);
                    BlockPos masterPos = clickedPos.subtract(rotatedOffset);

                    long key = masterPos.asLong() * 8 + facing.get2DDataValue() * 2 + (mirrored ? 1 : 0);
                    if (!tried.add(key)) continue;

                    if (validateStructure(level, masterPos, facing, mirrored)) {
                        Match match = new Match(masterPos, facing, mirrored);

                        // Determine preferred facing from trigger position relative to master
                        int dx = clickedPos.getX() - masterPos.getX();
                        int dz = clickedPos.getZ() - masterPos.getZ();
                        Direction preferred = (dx != 0 || dz != 0)
                                ? Direction.getNearest(dx, 0, dz)
                                : playerFacing;

                        if (facing == preferred && !mirrored && preferredMatch == null) {
                            preferredMatch = match;
                        }
                        if (anyMatch == null) {
                            anyMatch = match;
                        }
                        if (preferredMatch != null) break;
                    }
                }
                if (preferredMatch != null) break;
            }
            if (preferredMatch != null) break;
        }

        Match best = preferredMatch != null ? preferredMatch : anyMatch;
        if (best == null) return false;

        doAssemble(level, best.masterPos(), best.facing(), best.mirrored());
        level.playSound(null, best.masterPos(), sound, SoundSource.BLOCKS, soundVolume, soundPitch);
        return true;
    }

    // ---- Internal logic ----

    private boolean validateStructure(Level level, BlockPos masterPos, Direction facing, boolean mirrored) {
        for (PatternEntry entry : entries) {
            BlockPos localOffset = mirrored ? mirrorOffset(entry.offset()) : entry.offset();
            BlockPos rotated = rotateOffset(localOffset, facing);
            BlockPos worldPos = masterPos.offset(rotated);
            Predicate<BlockState> predicate = predicates.get(entry.symbol());
            if (!predicate.test(level.getBlockState(worldPos))) {
                return false;
            }
        }
        return true;
    }

    private static final BlockState AIR_STATE = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();

    private void doAssemble(Level level, BlockPos masterPos, Direction facing, boolean mirrored) {
        List<BlockTransform> transforms = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            PatternEntry entry = entries.get(i);
            // Skip air positions — validated but not assembled
            if (predicates.get(entry.symbol()).test(AIR_STATE)) {
                continue;
            }
            BlockPos localOffset = mirrored ? mirrorOffset(entry.offset()) : entry.offset();
            BlockPos rotated = rotateOffset(localOffset, facing);
            transforms.add(new BlockTransform(rotated, entry.isMaster(), i, entry.positionIndex()));
        }

        Block block = resultBlock.get();
        IntegerProperty positionProp = block instanceof com.cahcap.common.block.MultiblockPartBlock mb
                ? mb.getPositionProperty() : null;

        BlockState baseState = block.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(FORMED, true)
                .setValue(MIRRORED, mirrored);

        for (BlockTransform t : transforms) {
            BlockPos pos = t.worldPos(masterPos);
            BlockState oldState = level.getBlockState(pos);
            BlockState newState = positionProp != null
                    ? baseState.setValue(positionProp, t.positionIndex())
                    : baseState;

            level.setBlock(pos, newState, 0);

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MultiblockPartBlockEntity part) {
                part.setFacing(facing);
                part.setFormed(true);
                part.setMirrored(mirrored);
                part.setPosInMultiblock(t.posInMultiblock());
                part.setOffset(new int[]{
                        pos.getX() - masterPos.getX(),
                        pos.getY() - masterPos.getY(),
                        pos.getZ() - masterPos.getZ()
                });
                part.setOriginalBlockState(oldState);
                part.setChanged();
            }

            level.sendBlockUpdated(pos, oldState, newState, Block.UPDATE_ALL);
        }
    }

    /**
     * Mirror an offset on the X axis in blueprint-local space (flip left/right).
     */
    private static BlockPos mirrorOffset(BlockPos offset) {
        return new BlockPos(-offset.getX(), offset.getY(), offset.getZ());
    }

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

    // ---- Builder ----

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Character, Predicate<BlockState>> predicates = new HashMap<>();
        private final LinkedHashMap<Integer, String[]> layerPatterns = new LinkedHashMap<>();
        private char masterSymbol;
        private final Set<Character> triggerSymbols = new HashSet<>();
        private Supplier<Block> resultBlock;
        private boolean supportsMirroring = false;
        private SoundEvent sound;
        private float soundVolume = 1.0f;
        private float soundPitch = 1.0f;

        /**
         * Define a layer of the structure.
         * Each string is a row along the Z axis (first row = min Z).
         * Each character in a row represents the X axis (first char = min X).
         * The blueprint uses default facing NORTH (-Z = front).
         *
         * @param y    the Y level (relative, e.g. -1, 0, 1)
         * @param rows the pattern rows for this layer
         */
        public Builder layer(int y, String... rows) {
            layerPatterns.put(y, rows);
            return this;
        }

        /** Define a block predicate for a pattern symbol. */
        public Builder define(char symbol, Predicate<BlockState> predicate) {
            predicates.put(symbol, predicate);
            return this;
        }

        /** Mark which symbol represents the master block (exactly one in the pattern). */
        public Builder master(char symbol) {
            this.masterSymbol = symbol;
            return this;
        }

        /** Mark which symbols can trigger assembly when right-clicked. */
        public Builder trigger(char... symbols) {
            for (char s : symbols) triggerSymbols.add(s);
            return this;
        }

        /** The result block that all positions will be replaced with. */
        public Builder result(Supplier<Block> block) {
            this.resultBlock = block;
            return this;
        }

        /** Enable mirrored assembly (left/right flip). */
        public Builder mirrorable() {
            this.supportsMirroring = true;
            return this;
        }

        /** The sound to play on successful assembly. */
        public Builder sound(SoundEvent event, float volume, float pitch) {
            this.sound = event;
            this.soundVolume = volume;
            this.soundPitch = pitch;
            return this;
        }

        public Multiblock build() {
            // Find master position in the pattern grid
            int masterGx = -1, masterGy = -1, masterGz = -1;

            for (var entry : layerPatterns.entrySet()) {
                int y = entry.getKey();
                String[] rows = entry.getValue();
                for (int z = 0; z < rows.length; z++) {
                    for (int x = 0; x < rows[z].length(); x++) {
                        if (rows[z].charAt(x) == masterSymbol) {
                            masterGx = x;
                            masterGy = y;
                            masterGz = z;
                        }
                    }
                }
            }

            if (masterGx < 0) {
                throw new IllegalStateException("Master symbol '" + masterSymbol + "' not found in pattern");
            }

            // Build entries with offsets relative to master, compute model-space axis ranges
            int mdxMin = Integer.MAX_VALUE, mdxMax = Integer.MIN_VALUE;
            int mdyMin = Integer.MAX_VALUE, mdyMax = Integer.MIN_VALUE;
            int mdzMin = Integer.MAX_VALUE, mdzMax = Integer.MIN_VALUE;

            // First pass: compute axis ranges in model space (NORTH-oriented)
            for (var entry : layerPatterns.entrySet()) {
                int y = entry.getKey();
                String[] rows = entry.getValue();
                for (int z = 0; z < rows.length; z++) {
                    for (int x = 0; x < rows[z].length(); x++) {
                        int bpDx = x - masterGx, bpDy = y - masterGy, bpDz = z - masterGz;
                        int[] model = CustomVoxelShapes.blueprintToModel(bpDx, bpDy, bpDz);
                        mdxMin = Math.min(mdxMin, model[0]); mdxMax = Math.max(mdxMax, model[0]);
                        mdyMin = Math.min(mdyMin, model[1]); mdyMax = Math.max(mdyMax, model[1]);
                        mdzMin = Math.min(mdzMin, model[2]); mdzMax = Math.max(mdzMax, model[2]);
                    }
                }
            }
            int xSize = mdxMax - mdxMin + 1;
            int zSize = mdzMax - mdzMin + 1;

            // Second pass: build entries with model-space position indices
            List<PatternEntry> entries = new ArrayList<>();
            for (var entry : layerPatterns.entrySet()) {
                int y = entry.getKey();
                String[] rows = entry.getValue();
                for (int z = 0; z < rows.length; z++) {
                    for (int x = 0; x < rows[z].length(); x++) {
                        char symbol = rows[z].charAt(x);
                        if (!predicates.containsKey(symbol)) {
                            throw new IllegalStateException("Undefined symbol '" + symbol
                                    + "' at layer " + y + " row " + z + " col " + x);
                        }
                        int bpDx = x - masterGx, bpDy = y - masterGy, bpDz = z - masterGz;
                        int[] model = CustomVoxelShapes.blueprintToModel(bpDx, bpDy, bpDz);
                        boolean isMaster = (symbol == masterSymbol);
                        int positionIndex = (model[1] - mdyMin) * xSize * zSize
                                + (model[0] - mdxMin) * zSize
                                + (model[2] - mdzMin);
                        entries.add(new PatternEntry(
                                new BlockPos(bpDx, bpDy, bpDz), symbol, isMaster, positionIndex));
                    }
                }
            }

            return new Multiblock(entries, triggerSymbols, predicates,
                    resultBlock, supportsMirroring, sound, soundVolume, soundPitch);
        }
    }
}
