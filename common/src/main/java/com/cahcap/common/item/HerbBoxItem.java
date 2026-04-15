package com.cahcap.common.item;

import com.cahcap.common.block.HerbCabinetBlock;
import com.cahcap.common.block.HerbVaultBlock;
import com.cahcap.common.blockentity.HerbCabinetBlockEntity;
import com.cahcap.common.blockentity.HerbVaultBlockEntity;
import com.cahcap.common.registry.ModRegistries;
import com.cahcap.common.util.HerbRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Herb Box item - a portable herb storage container
 * Uses custom model rendering
 */
public class HerbBoxItem extends Item {
    
    private static final int MAX_CAPACITY = 512;
    
    // Herb registry keys delegated to HerbRegistry
    
    public HerbBoxItem(Properties properties) {
        super(properties);
    }
    
    public static int getHerbAmount(ItemStack stack, String herbKey) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0;
        CompoundTag tag = customData.copyTag();
        return tag.getInt("herb_" + herbKey);
    }
    
    public static void setHerbAmount(ItemStack stack, String herbKey, int amount) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putInt("herb_" + herbKey, Math.max(0, Math.min(amount, MAX_CAPACITY)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    public static int addHerb(ItemStack stack, String herbKey, int amount) {
        int current = getHerbAmount(stack, herbKey);
        int canAdd = Math.min(amount, MAX_CAPACITY - current);
        if (canAdd > 0) {
            setHerbAmount(stack, herbKey, current + canAdd);
        }
        return canAdd;
    }
    
    public static int removeHerb(ItemStack stack, String herbKey, int amount) {
        int current = getHerbAmount(stack, herbKey);
        int canRemove = Math.min(amount, current);
        if (canRemove > 0) {
            setHerbAmount(stack, herbKey, current - canRemove);
        }
        return canRemove;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        boolean shift = player.isShiftKeyDown();
        // Pre-check so we only swing when something will actually move.
        if (shift ? !boxHasAnyHerb(stack) : !inventoryHasAnyHerb(player)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (shift) {
            extractHerbsToInventory(stack, player);
        } else {
            collectHerbsFromInventory(stack, player);
        }

        return InteractionResultHolder.success(stack);
    }

    private static boolean boxHasAnyHerb(ItemStack box) {
        for (int i = 0; i < HerbRegistry.getHerbCount(); i++) {
            if (getHerbAmount(box, HerbRegistry.getHerbKey(i)) > 0) return true;
        }
        return false;
    }

    private static boolean inventoryHasAnyHerb(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && HerbRegistry.isHerb(s.getItem())) return true;
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof HerbCabinetBlockEntity cabinet) {
            if (!cabinet.isFormed()) {
                return InteractionResult.PASS;
            }

            var blockState = level.getBlockState(pos);
            var facing = blockState.getValue(HerbCabinetBlock.FACING);
            var clickedFace = context.getClickedFace();

            if (clickedFace != facing) return InteractionResult.PASS;
            int herbIndex = cabinet.getHerbIndexForBlock();
            if (!HerbCabinetBlock.isHitInGridCell(new net.minecraft.world.phys.BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()), pos, facing, herbIndex)) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (player != null && player.isShiftKeyDown()) {
                // Shift + Right Click Cabinet: Transfer herbs from box to cabinet
                transferToCabinet(stack, cabinet, player);
            } else {
                // Right Click Cabinet: Fill box from cabinet
                fillFromCabinet(stack, cabinet, player);
            }

            return InteractionResult.SUCCESS;
        }

        // Herb Vault interaction (same logic as cabinet)
        if (blockEntity instanceof HerbVaultBlockEntity vault) {
            if (!vault.isFormed()) {
                return InteractionResult.PASS;
            }

            var blockState = level.getBlockState(pos);
            var facing = blockState.getValue(HerbVaultBlock.FACING);
            var clickedFace = context.getClickedFace();

            if (clickedFace != facing || !HerbVaultBlock.isFrontRow(vault)) {
                return InteractionResult.PASS;
            }
            int herbIndex = vault.getHerbIndexForBlock();
            if (!HerbVaultBlock.isHitInGridCell(new net.minecraft.world.phys.BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()), pos, facing, herbIndex)) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (player != null && player.isShiftKeyDown()) {
                transferToVault(stack, vault, player);
            } else {
                fillFromVault(stack, vault, player);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
    
    private void collectHerbsFromInventory(ItemStack box, Player player) {
        for (int i = 0; i < HerbRegistry.getHerbCount(); i++) {
            collectHerb(box, player, HerbRegistry.getHerbByIndex(i), HerbRegistry.getHerbKey(i));
        }
    }
    
    private void collectHerb(ItemStack box, Player player, Item herb, String herbKey) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.isEmpty() || !invStack.is(herb)) continue;
            
            int toAdd = Math.min(invStack.getCount(), MAX_CAPACITY - getHerbAmount(box, herbKey));
            if (toAdd > 0) {
                addHerb(box, herbKey, toAdd);
                invStack.shrink(toAdd);
            }
            
            if (getHerbAmount(box, herbKey) >= MAX_CAPACITY) break;
        }
    }
    
    private void extractHerbsToInventory(ItemStack box, Player player) {
        for (int i = 0; i < HerbRegistry.getHerbCount(); i++) {
            extractHerb(box, player, HerbRegistry.getHerbByIndex(i), HerbRegistry.getHerbKey(i));
        }
    }
    
    private void extractHerb(ItemStack box, Player player, Item herb, String herbKey) {
        int amount = getHerbAmount(box, herbKey);
        while (amount > 0) {
            // Ensure we never create a stack larger than the max stack size (64)
            int stackSize = Math.min(amount, herb.getDefaultMaxStackSize());
            ItemStack herbStack = new ItemStack(herb, stackSize);
            if (player.getInventory().add(herbStack)) {
                int added = stackSize - herbStack.getCount();
                amount -= added;
            } else {
                break;
            }
        }
        setHerbAmount(box, herbKey, amount);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        
        for (int i = 0; i < HerbRegistry.getHerbCount(); i++) {
            String key = HerbRegistry.getHerbKey(i);
            addHerbTooltip(tooltip, stack, key, "item.herbalcurative." + key);
        }
    }
    
    private void addHerbTooltip(List<Component> tooltip, ItemStack stack, String herbKey, String translationKey) {
        int amount = getHerbAmount(stack, herbKey);
        if (amount > 0) {
            tooltip.add(Component.translatable(translationKey)
                    .append(": " + amount)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * Fill the box from the herb cabinet
     */
    private void fillFromCabinet(ItemStack box, HerbCabinetBlockEntity cabinet, Player player) {
        boolean anyTransferred = false;
        
        for (String key : HerbRegistry.getAllHerbKeys()) {
            anyTransferred |= fillHerbFromCabinet(box, cabinet, key);
        }
        cabinet.setChanged();
        
        // Play pickup sound if any herbs were transferred
        if (anyTransferred && player != null) {
            player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.4F,
                ((player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F
            );
        }
    }

    private boolean fillHerbFromCabinet(ItemStack box, HerbCabinetBlockEntity cabinet, String herbKey) {
        int boxCurrent = getHerbAmount(box, herbKey);
        int needed = MAX_CAPACITY - boxCurrent;

        if (needed > 0) {
            int cabinetAmount = cabinet.getHerbAmount(herbKey);
            int toTake = Math.min(needed, cabinetAmount);

            if (toTake > 0) {
                cabinet.removeHerb(herbKey, toTake);
                addHerb(box, herbKey, toTake);
                return true; // Herbs were transferred
            }
        }
        return false; // No herbs transferred
    }

    /**
     * Transfer herbs from box to cabinet
     */
    private void transferToCabinet(ItemStack box, HerbCabinetBlockEntity cabinet, Player player) {
        boolean anyTransferred = false;
        
        for (String key : HerbRegistry.getAllHerbKeys()) {
            anyTransferred |= transferHerbToCabinet(box, cabinet, key);
        }
        cabinet.setChanged();
        
        // Play place sound (lower pitch than pickup) if any herbs were transferred
        if (anyTransferred && player != null) {
            player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.4F,
                0.8F  // Lower pitch (0.8) for placing items, vs higher pitch (2.0) for picking up
            );
        }
    }

    private boolean transferHerbToCabinet(ItemStack box, HerbCabinetBlockEntity cabinet, String herbKey) {
        int boxAmount = getHerbAmount(box, herbKey);

        if (boxAmount > 0) {
            int added = cabinet.addHerb(herbKey, boxAmount);

            if (added > 0) {
                removeHerb(box, herbKey, added);
                return true;
            }
        }
        return false;
    }

    // ==================== Herb Vault interaction ====================

    private void fillFromVault(ItemStack box, HerbVaultBlockEntity vault, Player player) {
        boolean anyTransferred = false;
        for (String key : HerbRegistry.getAllHerbKeys()) {
            int boxCurrent = getHerbAmount(box, key);
            int needed = MAX_CAPACITY - boxCurrent;
            if (needed > 0) {
                int available = vault.getHerbAmount(key);
                int toTake = Math.min(needed, available);
                if (toTake > 0) {
                    vault.removeHerb(key, toTake);
                    addHerb(box, key, toTake);
                    anyTransferred = true;
                }
            }
        }
        vault.setChanged();
        if (anyTransferred && player != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F,
                    ((player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }

    private void transferToVault(ItemStack box, HerbVaultBlockEntity vault, Player player) {
        boolean anyTransferred = false;
        for (String key : HerbRegistry.getAllHerbKeys()) {
            int boxAmount = getHerbAmount(box, key);
            if (boxAmount > 0) {
                int added = vault.addHerb(key, boxAmount);
                if (added > 0) {
                    removeHerb(box, key, added);
                    anyTransferred = true;
                }
            }
        }
        vault.setChanged();
        if (anyTransferred && player != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 0.8F);
        }
    }
}

