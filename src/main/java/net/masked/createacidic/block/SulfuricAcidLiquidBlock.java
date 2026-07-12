package net.masked.createacidic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import java.util.function.Supplier;

public class SulfuricAcidLiquidBlock extends LiquidBlock {

    public SulfuricAcidLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (level.isClientSide()) return;

        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 11, false, false));
            // amplifier 11 = "Nausea XII" (amplifier is 0-indexed), duration refreshed each tick (~2s)
        }

        if (level.getGameTime() % 20 == 0) {
            entity.hurt(level.damageSources().generic(), 4.0F); // 2 hearts every second
        }
    }
}