package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreateAcidic.MODID);

    public static final RegistryObject<Block> BUNSEN_BURNER = BLOCKS.register("bunsen_burner",
            () -> new BunsenBurnerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Block> ERLENMEYER_FLASK = BLOCKS.register("erlenmeyer_flask",
            () -> new ErlenmeyerFlaskBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<Block> CONDENSER_APPARATUS = BLOCKS.register("condenser_apparatus",
            () -> new CondenserApparatusBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.5f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));
    public static final RegistryObject<LiquidBlock> SULFURIC_ACID_BLOCK =
            BLOCKS.register("sulfuric_acid_block", () -> new SulfuricAcidLiquidBlock(
                    () -> (net.minecraft.world.level.material.FlowingFluid) ModFluids.SULFURIC_ACID_SOURCE.get(),
                    BlockBehaviour.Properties.copy(Blocks.WATER)
                            .noCollission()
                            .strength(100.0F)
                            .noLootTable()));
    public static final RegistryObject<LiquidBlock> HYDROCHLORIC_ACID_BLOCK =
            BLOCKS.register("hydrochloric_acid_block", () -> new HydrochloricAcidLiquidBlock(
                    () -> (net.minecraft.world.level.material.FlowingFluid) ModFluids.HYDROCHLORIC_ACID_SOURCE.get(),
                    BlockBehaviour.Properties.copy(Blocks.WATER)
                            .noCollission()
                            .strength(100.0F)
                            .noLootTable()));
    public static final RegistryObject<Block> SULFUR = BLOCKS.register("sulfur",
            () -> new Block(BlockBehaviour.Properties.of()
                    .sound(SoundType.GRAVEL)
                    .strength(0.6F)
                    .mapColor(MapColor.COLOR_YELLOW)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}