package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.block.entity.BunsenBurnerBlockEntity;
import net.masked.createacidic.block.entity.CondenserApparatusBlockEntity;
import net.masked.createacidic.block.entity.ErlenmeyerFlaskBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreateAcidic.MODID);

    public static final RegistryObject<BlockEntityType<BunsenBurnerBlockEntity>> BUNSEN_BURNER_BE =
            BLOCK_ENTITIES.register("bunsen_burner", () -> BlockEntityType.Builder.of(
                    BunsenBurnerBlockEntity::new,
                    ModBlocks.BUNSEN_BURNER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ErlenmeyerFlaskBlockEntity>> ERLENMEYER_FLASK_BE =
            BLOCK_ENTITIES.register("erlenmeyer_flask", () -> BlockEntityType.Builder.of(
                    ErlenmeyerFlaskBlockEntity::new,
                    ModBlocks.ERLENMEYER_FLASK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CondenserApparatusBlockEntity>> CONDENSER_APPARATUS_BE =
            BLOCK_ENTITIES.register("condenser_apparatus", () -> BlockEntityType.Builder.of(
                    CondenserApparatusBlockEntity::new,
                    ModBlocks.CONDENSER_APPARATUS.get()
            ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}