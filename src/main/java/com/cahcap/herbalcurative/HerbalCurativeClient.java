package com.cahcap.herbalcurative;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = HerbalCurative.MODID, dist = Dist.CLIENT)
public class HerbalCurativeClient {
    
    public HerbalCurativeClient() {
        HerbalCurative.LOGGER.info("Herbal Curative client initializing");
    }

    @EventBusSubscriber(modid = HerbalCurative.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            HerbalCurative.LOGGER.info("Herbal Curative client setup complete");
        }
    }
}
