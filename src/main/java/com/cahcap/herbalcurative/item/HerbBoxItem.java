package com.cahcap.herbalcurative.item;

import com.cahcap.herbalcurative.blockentity.HerbCabinetBlockEntity;
import com.cahcap.herbalcurative.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class HerbBoxItem extends Item {
    
    private static final int MAX_CAPACITY = 1024;
    
    // Herb registry names for storage
    private static final String[] HERB_KEYS = {
        "scaleplate",
        "dewpetal_shard",
        "golden_lilybell",
        "cryst_spine",
        "burnt_node",
        "heart_of_stardream"
    };
    
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
        
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        
        if (player.isShiftKeyDown()) {
            // Shift + Right Click: Extract all herbs to inventory
            extractHerbsToInventory(stack, player);
        } else {
            // Right Click: Collect all herbs from inventory
            collectHerbsFromInventory(stack, player);
        }
        
        return InteractionResultHolder.success(stack);
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

            // Check if player is clicking on the front face of the cabinet
            var blockState = level.getBlockState(pos);
            var facing = blockState.getValue(com.cahcap.herbalcurative.block.HerbCabinetBlock.FACING);
            var clickedFace = context.getClickedFace();
            
            if (clickedFace != facing) {
                // Not clicking on front face, pass
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

        return InteractionResult.PASS;
    }
    
    private void collectHerbsFromInventory(ItemStack box, Player player) {
        collectHerb(box, player, ModItems.SCALEPLATE.get(), "scaleplate");
        collectHerb(box, player, ModItems.DEWPETAL_SHARD.get(), "dewpetal_shard");
        collectHerb(box, player, ModItems.GOLDEN_LILYBELL.get(), "golden_lilybell");
        collectHerb(box, player, ModItems.CRYST_SPINE.get(), "cryst_spine");
        collectHerb(box, player, ModItems.BURNT_NODE.get(), "burnt_node");
        collectHerb(box, player, ModItems.HEART_OF_STARDREAM.get(), "heart_of_stardream");
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
        extractHerb(box, player, ModItems.SCALEPLATE.get(), "scaleplate");
        extractHerb(box, player, ModItems.DEWPETAL_SHARD.get(), "dewpetal_shard");
        extractHerb(box, player, ModItems.GOLDEN_LILYBELL.get(), "golden_lilybell");
        extractHerb(box, player, ModItems.CRYST_SPINE.get(), "cryst_spine");
        extractHerb(box, player, ModItems.BURNT_NODE.get(), "burnt_node");
        extractHerb(box, player, ModItems.HEART_OF_STARDREAM.get(), "heart_of_stardream");
    }
    
    private void extractHerb(ItemStack box, Player player, Item herb, String herbKey) {
        int amount = getHerbAmount(box, herbKey);
        while (amount > 0) {
            ItemStack herbStack = new ItemStack(herb, Math.min(amount, 64));
            if (player.getInventory().add(herbStack)) {
                int added = Math.min(amount, 64) - herbStack.getCount();
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
        
        addHerbTooltip(tooltip, stack, "scaleplate", "item.herbalcurative.scaleplate");
        addHerbTooltip(tooltip, stack, "dewpetal_shard", "item.herbalcurative.dewpetal_shard");
        addHerbTooltip(tooltip, stack, "golden_lilybell", "item.herbalcurative.golden_lilybell");
        addHerbTooltip(tooltip, stack, "cryst_spine", "item.herbalcurative.cryst_spine");
        addHerbTooltip(tooltip, stack, "burnt_node", "item.herbalcurative.burnt_node");
        addHerbTooltip(tooltip, stack, "heart_of_stardream", "item.herbalcurative.heart_of_stardream");
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
        fillHerbFromCabinet(box, cabinet, "scaleplate");
        fillHerbFromCabinet(box, cabinet, "dewpetal_shard");
        fillHerbFromCabinet(box, cabinet, "golden_lilybell");
        fillHerbFromCabinet(box, cabinet, "cryst_spine");
        fillHerbFromCabinet(box, cabinet, "burnt_node");
        fillHerbFromCabinet(box, cabinet, "heart_of_stardream");
        cabinet.setChanged();
    }

    private void fillHerbFromCabinet(ItemStack box, HerbCabinetBlockEntity cabinet, String herbKey) {
        int boxCurrent = getHerbAmount(box, herbKey);
        int needed = MAX_CAPACITY - boxCurrent;

        if (needed > 0) {
            int cabinetAmount = cabinet.getHerbAmount(herbKey);
            int toTake = Math.min(needed, cabinetAmount);

            if (toTake > 0) {
                cabinet.removeHerb(herbKey, toTake);
                addHerb(box, herbKey, toTake);
            }
        }
    }

    /**
     * Transfer herbs from box to cabinet
     */
    private void transferToCabinet(ItemStack box, HerbCabinetBlockEntity cabinet, Player player) {
        transferHerbToCabinet(box, cabinet, "scaleplate");
        transferHerbToCabinet(box, cabinet, "dewpetal_shard");
        transferHerbToCabinet(box, cabinet, "golden_lilybell");
        transferHerbToCabinet(box, cabinet, "cryst_spine");
        transferHerbToCabinet(box, cabinet, "burnt_node");
        transferHerbToCabinet(box, cabinet, "heart_of_stardream");
        cabinet.setChanged();
    }

    private void transferHerbToCabinet(ItemStack box, HerbCabinetBlockEntity cabinet, String herbKey) {
        int boxAmount = getHerbAmount(box, herbKey);

        if (boxAmount > 0) {
            int added = cabinet.addHerb(herbKey, boxAmount);

            if (added > 0) {
                removeHerb(box, herbKey, added);
            }
        }
    }
}
