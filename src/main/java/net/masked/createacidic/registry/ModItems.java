package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.item.ScientistArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateAcidic.MODID);

    // --- Raw materials ---
    public static final RegistryObject<Item> SALT = ITEMS.register("salt",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SODIUM_CHLORIDE = ITEMS.register("sodium_chloride",
            () -> new Item(new Item.Properties()));

    // --- Fluids-as-items (bucket-style, no fluid system yet) ---
    public static final RegistryObject<Item> CRUDE_BRINE = ITEMS.register("crude_brine",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PURIFIED_BRINE = ITEMS.register("purified_brine",
            () -> new Item(new Item.Properties()));

    // --- Glassware / apparatus items ---
    public static final RegistryObject<Item> GLASS_VIAL = ITEMS.register("glass_vial",
            () -> new Item(new Item.Properties()));

    // --- Filled vials ---
    public static final RegistryObject<Item> VIAL_OF_HYDROCHLORIC_ACID = ITEMS.register("vial_of_hydrochloric_acid",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VIAL_OF_SULFURIC_ACID = ITEMS.register("vial_of_sulfuric_acid",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VIAL_OF_CONCENTRATED_SULFURIC_ACID = ITEMS.register("vial_of_concentrated_sulfuric_acid",
            () -> new Item(new Item.Properties()));

    // --- Wearables ---
    public static final RegistryObject<Item> SCIENCE_GOGGLES = ITEMS.register("science_goggles",
            () -> new net.masked.createacidic.item.ScienceGogglesItem(new Item.Properties()));

    public static final RegistryObject<Item> LAB_COAT = ITEMS.register("lab_coat",
            () -> new ArmorItem(ScientistArmorMaterial.SCIENTIST_WEAR, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()));
    public static final RegistryObject<Item> BUCKET_OF_SULFURIC_ACID =
            ITEMS.register("bucket_of_sulfuric_acid", () -> new BucketItem(
                    ModFluids.SULFURIC_ACID_SOURCE,
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> BUCKET_OF_HYDROCHLORIC_ACID =
            ITEMS.register("bucket_of_hydrochloric_acid", () -> new BucketItem(
                    ModFluids.HYDROCHLORIC_ACID_SOURCE,
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}