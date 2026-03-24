package com.cahcap.common.item.flowweavering;

import java.util.ArrayList;
import java.util.List;

public final class RingInteractionRegistry {
    private static final List<RingBlockInteraction> interactions = new ArrayList<>();

    private RingInteractionRegistry() {}

    public static void register(RingBlockInteraction interaction) {
        interactions.add(interaction);
    }

    public static List<RingBlockInteraction> getInteractions() {
        return interactions;
    }

    /** Call during mod initialization to register all interactions */
    public static void init() {
        register(new MultiblockFormationInteraction());
        register(new CauldronInteraction());
        register(new HerbalBlendingInteraction());
        register(new WorkbenchCraftInteraction());
        register(new HerbBasketInteraction());
        register(new HerbPotInteraction());
        register(new IncenseBurnerInteraction());
    }
}
