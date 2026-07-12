package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateAcidic.MODID);

    public static final RegistryObject<Item> BUNSEN_BURNER_ITEM = ITEMS.register("bunsen_burner",
            () -> new BlockItem(ModBlocks.BUNSEN_BURNER.get(), new Item.Properties()));

    public static final RegistryObject<Item> ERLENMEYER_FLASK_ITEM = ITEMS.register("erlenmeyer_flask",
            () -> new BlockItem(ModBlocks.ERLENMEYER_FLASK.get(), new Item.Properties()));

    public static final RegistryObject<Item> CONDENSER_APPARATUS_ITEM = ITEMS.register("condenser_apparatus",
            () -> new BlockItem(ModBlocks.CONDENSER_APPARATUS.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}