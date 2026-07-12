package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateAcidic.MODID);

    public static final RegistryObject<CreativeModeTab> ACIDIC_TAB = CREATIVE_MODE_TABS.register("acidic_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.acidic.acidic_tab"))
                    .icon(() -> new ItemStack(ModItems.VIAL_OF_HYDROCHLORIC_ACID.get()))
                    .displayItems((parameters, output) -> {
                        // Raw materials
                        output.accept(ModItems.SALT.get());
                        output.accept(ModItems.SODIUM_CHLORIDE.get());
                        output.accept(ModItems.CRUDE_BRINE.get());
                        output.accept(ModItems.PURIFIED_BRINE.get());

                        // Glassware
                        output.accept(ModItems.GLASS_VIAL.get());

                        // Filled vials
                        output.accept(ModItems.VIAL_OF_HYDROCHLORIC_ACID.get());
                        output.accept(ModItems.VIAL_OF_SULFURIC_ACID.get());

                        // Buckets
                        output.accept(ModItems.BUCKET_OF_SULFURIC_ACID.get());
                        output.accept(ModItems.BUCKET_OF_HYDROCHLORIC_ACID.get());

                        // Wearables
                        output.accept(ModItems.SCIENCE_GOGGLES.get());
                        output.accept(ModItems.LAB_COAT.get());

                        // Blocks
                        output.accept(ModBlockItems.BUNSEN_BURNER_ITEM.get());
                        output.accept(ModBlockItems.ERLENMEYER_FLASK_ITEM.get());
                        output.accept(ModBlockItems.CONDENSER_APPARATUS_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}