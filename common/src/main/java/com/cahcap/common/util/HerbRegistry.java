package com.cahcap.common.util;

import com.cahcap.common.registry.ModRegistries;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Central registry for all herb types.
 * Eliminates duplicated herb lists across HerbCabinetBlockEntity, HerbVaultBlockEntity,
 * FlowweaveRingItem, HerbBoxItem, etc.
 */
public final class HerbRegistry {

    private static final String[] HERB_KEYS = {
        "scaleplate",
        "dewpetal",
        "zephyr_blossom",
        "cryst_spine",
        "pyro_node",
        "stellar_mote"
    };

    @SuppressWarnings("unchecked")
    private static final Supplier<Item>[] HERB_SUPPLIERS = new Supplier[] {
        () -> ModRegistries.SCALEPLATE.get(),
        () -> ModRegistries.DEWPETAL.get(),
        () -> ModRegistries.ZEPHYR_BLOSSOM.get(),
        () -> ModRegistries.CRYST_SPINE.get(),
        () -> ModRegistries.PYRO_NODE.get(),
        () -> ModRegistries.STELLAR_MOTE.get()
    };

    private HerbRegistry() {}

    public static int getHerbCount() {
        return HERB_KEYS.length;
    }

    public static Item[] getAllHerbItems() {
        Item[] herbs = new Item[HERB_SUPPLIERS.length];
        for (int i = 0; i < HERB_SUPPLIERS.length; i++) {
            herbs[i] = HERB_SUPPLIERS[i].get();
        }
        return herbs;
    }

    public static boolean isHerb(Item item) {
        for (Supplier<Item> supplier : HERB_SUPPLIERS) {
            if (supplier.get() == item) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOverworldHerb(Item item) {
        return item == HERB_SUPPLIERS[0].get() || // scaleplate
               item == HERB_SUPPLIERS[1].get() || // dewpetal
               item == HERB_SUPPLIERS[2].get();   // zephyr_blossom
    }

    public static boolean isNetherOrEndHerb(Item item) {
        return item == HERB_SUPPLIERS[3].get() || // cryst_spine
               item == HERB_SUPPLIERS[4].get() || // pyro_node
               item == HERB_SUPPLIERS[5].get();   // stellar_mote
    }

    public static int getHerbIndex(Item herb) {
        for (int i = 0; i < HERB_SUPPLIERS.length; i++) {
            if (HERB_SUPPLIERS[i].get() == herb) {
                return i;
            }
        }
        return -1;
    }

    public static String getKeyForHerb(Item herb) {
        int index = getHerbIndex(herb);
        return index >= 0 ? HERB_KEYS[index] : null;
    }

    public static Item getHerbByKey(String key) {
        for (int i = 0; i < HERB_KEYS.length; i++) {
            if (HERB_KEYS[i].equals(key)) {
                return HERB_SUPPLIERS[i].get();
            }
        }
        return null;
    }

    public static Item getHerbByKeyContains(String key) {
        for (int i = 0; i < HERB_KEYS.length; i++) {
            if (key.contains(HERB_KEYS[i])) {
                return HERB_SUPPLIERS[i].get();
            }
        }
        return null;
    }

    public static String[] getAllHerbKeys() {
        return HERB_KEYS.clone();
    }

    public static String getHerbKey(int index) {
        if (index < 0 || index >= HERB_KEYS.length) {
            return null;
        }
        return HERB_KEYS[index];
    }

    public static Item getHerbByIndex(int index) {
        if (index < 0 || index >= HERB_SUPPLIERS.length) {
            return null;
        }
        return HERB_SUPPLIERS[index].get();
    }

    /**
     * Transfer all herbs from a player's inventory into a storage.
     * Used by HerbCabinetBlock and HerbVaultBlock double-click batch add.
     *
     * @param player the player whose inventory to scan
     * @param addFunction accepts (Item herb, int amount) and returns the number actually added
     * @return total number of herbs transferred
     */
    public static int transferAllHerbsFromInventory(net.minecraft.world.entity.player.Player player,
                                                     java.util.function.BiFunction<Item, Integer, Integer> addFunction) {
        int totalAdded = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack invStack = player.getInventory().getItem(i);
            if (!invStack.isEmpty() && isHerb(invStack.getItem())) {
                int added = addFunction.apply(invStack.getItem(), invStack.getCount());
                invStack.shrink(added);
                totalAdded += added;
                if (invStack.isEmpty()) {
                    player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                }
            }
        }
        return totalAdded;
    }
}
