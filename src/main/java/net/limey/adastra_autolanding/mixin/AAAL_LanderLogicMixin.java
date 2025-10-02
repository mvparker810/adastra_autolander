package net.limey.adastra_autolanding.mixin;

import earth.terrarium.adastra.client.utils.SoundUtils;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import earth.terrarium.adastra.common.entities.vehicles.Lander;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import earth.terrarium.adastra.common.entities.vehicles.Lander;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import earth.terrarium.adastra.common.entities.vehicles.Lander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Lander.class)
public abstract class AAAL_LanderLogicMixin extends Entity {
    public AAAL_LanderLogicMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private double remap_value(double value, double fromMin, double fromMax, double toMin, double toMax) {

        if (fromMax - fromMin == 0) {
           return value;
        }
        return toMin + (value - fromMin) * (toMax - toMin) / (fromMax - fromMin);
    }

    //Disable lander explosions
    @Overwrite(remap = false)
    public void explode() {}



    @Shadow(remap = false)
    protected abstract void spawnLanderParticles();

    @Shadow(remap = false) private float speed;
    @Shadow(remap = false) public boolean startedRocketSound;




    /*
    @Shadow
    public abstract Level level();
    @Shadow
    public abstract BlockPos blockPosition();
    */

    @Inject(remap = false, method = "flightTick", at = @At("HEAD"), cancellable = true)
    private void overrideFlightTick(CallbackInfo ci) {
        // Call the shadowed method



        Lander lander = (Lander) (Object) this;
        int lander_yPos = (int)lander.getY();

        Level level = lander.level();

        int ground = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, lander.blockPosition()).getY();
        int distance = Math.max(0, lander_yPos - ground);

        float stopping_thrust = 0.029F;
        int distance_offset = 1;
        float G = -0.01F;
        float timeToImpact = (-speed + (float)Math.sqrt(speed * speed + 2 * G * (distance - distance_offset))) / G;
        float requiredDeceleration = (speed - 0.5f) / timeToImpact;
        float requiredThrust = (requiredDeceleration + G);

        boolean counteract = requiredThrust <= stopping_thrust && requiredThrust > 0 && speed < 0;

        if (counteract && distance > 0) {

            //todo why wont sounds play
            SoundUtils.playLanderSound(lander);

            speed += stopping_thrust;
            fallDistance *= 0.9f;

            spawnLanderParticles();
            if (level.isClientSide() && !lander.startedRocketSound) {
                lander.startedRocketSound = true;
                SoundUtils.playLanderSound(lander);
            }
        }

    }

}
