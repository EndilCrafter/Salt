package io.github.mortuusars.salt;

import io.github.mortuusars.salt.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Random;

public class Dissolving {

    /**
     * Performs necessary checks and if all passes - dissolves a block.
     * @return True if block was dissolved.
     */
    public static boolean maybeDissolveInRain(BlockState dissolvedState, ServerLevel level, BlockPos pos) {
        if (Configuration.DISSOLVING_ENABLED.get()
                && Configuration.DISSOLVING_IN_RAIN.get()
                && level.isRainingAt(pos.above())
                && level.random.nextDouble() < Configuration.DISSOLVING_IN_RAIN_CHANCE.get()) {
            dissolve(dissolvedState, level, pos, Fluids.EMPTY, false);
            return true;
        }
        return false;
    }

    /**
     * Performs necessary checks and if all passes - dissolves a block.
     * @return True if block was dissolved.
     */
    public static boolean maybeDissolve(BlockState dissolvedState, BlockPos dissolvedPos, BlockState adjacentState, BlockPos adjacentPos, ServerLevel level) {
        if (Configuration.DISSOLVING_ENABLED.get()
                && adjacentState.is(Salt.BlockTags.SALT_DISSOLVABLES) && level.random.nextDouble() < Configuration.DISSOLVING_CHANCE.get()) {
            FluidState fluidState = adjacentState.getFluidState();
            if (dissolvedPos.getY() > adjacentPos.getY())
                fluidState = Fluids.EMPTY.defaultFluidState();
            dissolve(dissolvedState, level, dissolvedPos, fluidState.getType(), fluidState.isSource());
            return true;
        }
        return false;
    }

    public static void dissolve(BlockState dissolvedState, ServerLevel level, BlockPos pos, Fluid fluid, boolean isSource) {
        if (dissolvedState.isAir()) {
            if (isSource && Configuration.DISSOLVING_FLUID_SOURCE_CONVERSION.get())
                level.setBlockAndUpdate(pos, fluid.defaultFluidState().createLegacyBlock());
            else
                level.removeBlock(pos, false);
        }
        else
            level.setBlockAndUpdate(pos, dissolvedState);

        Vec3 center = Vec3.atCenterOf(pos);
        Random random = level.getRandom();

        level.playSound(null, center.x, center.y, center.z, Salt.Sounds.SALT_DISSOLVE.get(), SoundSource.BLOCKS, 1f, random.nextFloat() * 0.2f + 0.9f);

        for (int i = 0; i < 6; i++) {
            level.sendParticles(ParticleTypes.SPIT, center.x + random.nextGaussian() * 0.35f,
                    center.y + random.nextGaussian() * 0.35f, center.z + random.nextGaussian() * 0.35f,
                    1, 0f, 0f, 0f, 0f);
        }
    }
}