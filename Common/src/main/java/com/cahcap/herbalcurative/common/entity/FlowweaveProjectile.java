package com.cahcap.herbalcurative.common.entity;

import com.cahcap.herbalcurative.common.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Projectile entity for Flowweave Ring's BURST and LINGERING casting modes.
 * 
 * When it hits something:
 * - BURST mode: Create explosion effect (visual only), apply buff to all entities in TNT explosion range
 * - LINGERING mode: Create explosion effect (visual only), spawn lingering effect cloud
 * 
 * Supports multiple effects (e.g., Speed + Jump Boost)
 */
public class FlowweaveProjectile extends ThrowableProjectile {
    
    // Synced data for rendering
    private static final EntityDataAccessor<Integer> DATA_COLOR = 
            SynchedEntityData.defineId(FlowweaveProjectile.class, EntityDataSerializers.INT);
    
    // Effect data (server-side only) - now supports multiple effects
    private List<Holder<MobEffect>> effects = new ArrayList<>();
    private int duration = 600;  // 30 seconds default
    private int amplifier = 0;
    private boolean lingering = false;
    private boolean instant = false;  // For instant effects like heal/harm
    
    // NBT tags
    private static final String TAG_EFFECTS = "Effects";  // List of effect IDs
    private static final String TAG_EFFECT = "Effect";    // Legacy single effect
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_AMPLIFIER = "Amplifier";
    private static final String TAG_INSTANT = "Instant";
    private static final String TAG_LINGERING = "Lingering";
    private static final String TAG_COLOR = "Color";
    
    // TNT explosion radius (approximately 4 blocks)
    private static final double EXPLOSION_RADIUS = 4.0;
    
    // Maximum flight distance (~100 blocks at speed 3.0)
    private static final int MAX_FLIGHT_TICKS = 35;
    private int flightTicks = 0;

    public FlowweaveProjectile(EntityType<? extends FlowweaveProjectile> type, Level level) {
        super(type, level);
    }
    
    /**
     * No gravity - projectile flies in a straight line
     */
    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }
    
    public FlowweaveProjectile(Level level, LivingEntity shooter) {
        super(ModRegistries.FLOWWEAVE_PROJECTILE_TYPE.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COLOR, 0x3F76E4);  // Default water blue
    }
    
    // ==================== Setters ====================
    
    /**
     * Set multiple effects for this projectile
     */
    public void setEffects(List<Holder<MobEffect>> effects, int duration, int amplifier) {
        this.effects = new ArrayList<>(effects);
        this.duration = duration;
        this.amplifier = amplifier;
    }
    
    /**
     * Set single effect (backwards compatible)
     */
    public void setEffect(Holder<MobEffect> effect, int duration, int amplifier) {
        this.effects.clear();
        this.effects.add(effect);
        this.duration = duration;
        this.amplifier = amplifier;
    }
    
    public void setColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }
    
    public void setLingering(boolean lingering) {
        this.lingering = lingering;
    }
    
    public void setInstant(boolean instant) {
        this.instant = instant;
    }
    
    // ==================== Getters ====================
    
    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }
    
    // ==================== Tick ====================
    
    @Override
    public void tick() {
        super.tick();
        
        flightTicks++;
        
        // Remove if flown too far
        if (flightTicks > MAX_FLIGHT_TICKS) {
            this.discard();
            return;
        }
        
        // Spawn dark green shockwave trail particles
        if (level().isClientSide) {
            // Dark green color for the shockwave
            Vector3f greenColor = new Vector3f(0.1F, 0.5F, 0.15F);
            DustParticleOptions greenDust = new DustParticleOptions(greenColor, 1.5F);
            
            // Spawn multiple particles for shockwave effect
            for (int i = 0; i < 3; i++) {
                double offsetX = (level().random.nextDouble() - 0.5) * 0.3;
                double offsetY = (level().random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (level().random.nextDouble() - 0.5) * 0.3;
                
                level().addParticle(
                        greenDust,
                        this.getX() + offsetX, 
                        this.getY() + offsetY, 
                        this.getZ() + offsetZ,
                        0, 0, 0
                );
            }
            
            // Add some smaller trailing particles
            DustParticleOptions smallGreenDust = new DustParticleOptions(greenColor, 0.8F);
            level().addParticle(
                    smallGreenDust,
                    this.xo, this.yo, this.zo,  // Previous position for trail
                    0, 0, 0
            );
        }
    }
    
    // ==================== Hit Handling ====================
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (level().isClientSide) {
            return;
        }
        
        Vec3 hitPos = result.getLocation();
        
        // Create explosion effect (visual and sound only, no damage or block destruction)
        createExplosionEffect(hitPos);
        
        if (lingering) {
            // LINGERING mode: Spawn area effect cloud
            spawnLingeringCloud(hitPos);
        } else {
            // BURST mode: Apply effect to all entities in range
            applyEffectToEntitiesInRange(hitPos);
        }
        
        this.discard();
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Don't apply extra damage, just trigger the explosion effect
        // The onHit method handles the actual effect application
    }
    
    /**
     * Create visual explosion effect (particles and sound, no actual explosion)
     */
    private void createExplosionEffect(Vec3 pos) {
        Level level = level();
        
        // Play explosion sound
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // Spawn explosion particles (server side - will be synced to clients)
        if (level instanceof ServerLevel serverLevel) {
            // Dark green dust particles for shockwave explosion
            Vector3f greenColor = new Vector3f(0.1F, 0.5F, 0.15F);
            DustParticleOptions greenDust = new DustParticleOptions(greenColor, 2.0F);
            
            // Spawn green shockwave particles in a sphere pattern
            for (int i = 0; i < 60; i++) {
                double angle1 = level.random.nextDouble() * Math.PI * 2;
                double angle2 = level.random.nextDouble() * Math.PI;
                double radius = EXPLOSION_RADIUS * (0.5 + level.random.nextDouble() * 0.5);
                
                double offsetX = Math.sin(angle2) * Math.cos(angle1) * radius;
                double offsetY = Math.cos(angle2) * radius;
                double offsetZ = Math.sin(angle2) * Math.sin(angle1) * radius;
                
                serverLevel.sendParticles(greenDust,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1, 0, 0, 0, 0);
            }
            
            // Add some explosion embers for extra effect
            for (int i = 0; i < 10; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * EXPLOSION_RADIUS;
                double offsetY = (level.random.nextDouble() - 0.5) * EXPLOSION_RADIUS;
                double offsetZ = (level.random.nextDouble() - 0.5) * EXPLOSION_RADIUS;
                
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1, 0, 0, 0, 0);
            }
        }
    }
    
    /**
     * Apply all effects to all living entities in explosion range (BURST mode)
     */
    private void applyEffectToEntitiesInRange(Vec3 pos) {
        if (effects.isEmpty()) return;
        
        Level level = level();
        AABB box = new AABB(
                pos.x - EXPLOSION_RADIUS, pos.y - EXPLOSION_RADIUS, pos.z - EXPLOSION_RADIUS,
                pos.x + EXPLOSION_RADIUS, pos.y + EXPLOSION_RADIUS, pos.z + EXPLOSION_RADIUS
        );
        
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity entity : entities) {
            // Check if entity is within sphere (not just box)
            double distSq = entity.position().distanceToSqr(pos);
            if (distSq <= EXPLOSION_RADIUS * EXPLOSION_RADIUS) {
                // Apply all effects to this entity
                for (Holder<MobEffect> effect : effects) {
                    boolean isEffectInstant = effect.value().isInstantenous();
                    if (isEffectInstant) {
                        // Apply instant effect directly
                        entity.addEffect(new MobEffectInstance(effect, 1, amplifier, false, true));
                    } else {
                        entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
                    }
                }
            }
        }
    }
    
    /**
     * Spawn lingering effect cloud (LINGERING mode)
     * Adds all effects to the cloud
     */
    private void spawnLingeringCloud(Vec3 pos) {
        if (effects.isEmpty()) return;
        
        Level level = level();
        
        AreaEffectCloud cloud = new AreaEffectCloud(level, pos.x, pos.y, pos.z);
        cloud.setRadius((float) EXPLOSION_RADIUS);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(600);  // 30 seconds
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        
        // Add all effects to the cloud
        for (Holder<MobEffect> effect : effects) {
            boolean isEffectInstant = effect.value().isInstantenous();
            if (isEffectInstant) {
                // Instant effects in lingering clouds use 1 tick duration
                cloud.addEffect(new MobEffectInstance(effect, 1, amplifier));
            } else {
                cloud.addEffect(new MobEffectInstance(effect, duration / 4, amplifier));  // 1/4 duration like vanilla
            }
        }
        
        // Color is automatically set based on the effects added
        
        level.addFreshEntity(cloud);
    }
    
    // ==================== Save/Load ====================
    
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        
        // Save effects as a list
        if (!effects.isEmpty()) {
            net.minecraft.nbt.ListTag effectsList = new net.minecraft.nbt.ListTag();
            for (Holder<MobEffect> effect : effects) {
                ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
                if (effectId != null) {
                    effectsList.add(net.minecraft.nbt.StringTag.valueOf(effectId.toString()));
                }
            }
            tag.put(TAG_EFFECTS, effectsList);
        }
        tag.putInt(TAG_DURATION, duration);
        tag.putInt(TAG_AMPLIFIER, amplifier);
        tag.putBoolean(TAG_LINGERING, lingering);
        tag.putBoolean(TAG_INSTANT, instant);
        tag.putInt(TAG_COLOR, getColor());
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        
        this.effects.clear();
        
        // Load effects - support both new list format and legacy single effect
        if (tag.contains(TAG_EFFECTS)) {
            net.minecraft.nbt.ListTag effectsList = tag.getList(TAG_EFFECTS, net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < effectsList.size(); i++) {
                ResourceLocation effectId = ResourceLocation.tryParse(effectsList.getString(i));
                if (effectId != null) {
                    MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(effectId);
                    if (mobEffect != null) {
                        this.effects.add(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect));
                    }
                }
            }
        } else if (tag.contains(TAG_EFFECT)) {
            // Legacy single effect format
            ResourceLocation effectId = ResourceLocation.tryParse(tag.getString(TAG_EFFECT));
            if (effectId != null) {
                MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(effectId);
                if (mobEffect != null) {
                    this.effects.add(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect));
                }
            }
        }
        
        this.duration = tag.getInt(TAG_DURATION);
        this.amplifier = tag.getInt(TAG_AMPLIFIER);
        this.lingering = tag.getBoolean(TAG_LINGERING);
        this.instant = tag.getBoolean(TAG_INSTANT);
        if (tag.contains(TAG_COLOR)) {
            setColor(tag.getInt(TAG_COLOR));
        }
    }
}
