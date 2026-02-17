package com.cahcap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point for Herbal Curative mod.
 * This class contains platform-independent initialization code.
 */
public class HerbalCurativeCommon {
    
    public static final String MOD_ID = "herbalcurative";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    /**
     * Called during mod initialization on all platforms.
     */
    public static void init() {
        LOGGER.info("Herbal Curative is initializing...");
    }
    
    /**
     * Called during common setup phase.
     */
    public static void commonSetup() {
        LOGGER.info("Herbal Curative common setup complete");
    }
}

