package net.masked.createacidic.fluid;

import net.masked.createacidic.registry.ModBlocks;
import net.masked.createacidic.registry.ModFluidTypes;
import net.masked.createacidic.registry.ModFluids;
import net.masked.createacidic.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class SulfuricAcidFluid extends ForgeFlowingFluid {

    protected SulfuricAcidFluid(Properties properties) {
        super(properties);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 7; // water is 5; higher = slower spread
    }

    @Override
    public int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    public static class Source extends SulfuricAcidFluid {
        public Source() {
            super(buildProperties());
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends SulfuricAcidFluid {
        public Flowing() {
            super(buildProperties());
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }

    private static Properties buildProperties() {
        return new Properties(
                ModFluidTypes.SULFURIC_ACID_TYPE,
                ModFluids.SULFURIC_ACID_SOURCE,
                ModFluids.SULFURIC_ACID_FLOWING)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(2)
                .block(() -> (LiquidBlock) ModBlocks.SULFURIC_ACID_BLOCK.get())
                .bucket(() -> (Item) ModItems.BUCKET_OF_SULFURIC_ACID.get());
    }
}