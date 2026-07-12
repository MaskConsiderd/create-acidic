package net.masked.createacidic.util;

import net.masked.createacidic.registry.ModFluids;
import net.masked.createacidic.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

/** Maps filled-vial items <-> the fluid (and amount) they represent. Extend this as new fluids are added. */
public class VialFluidRegistry {

    public static final int VIAL_AMOUNT_MB = 50;

    private static final Map<Item, java.util.function.Supplier<Fluid>> VIAL_TO_FLUID = new HashMap<>();
    private static final Map<java.util.function.Supplier<Fluid>, java.util.function.Supplier<Item>> FLUID_TO_VIAL = new HashMap<>();

    static {
        register(ModItems.VIAL_OF_SULFURIC_ACID, ModFluids.SULFURIC_ACID_SOURCE);
        register(ModItems.VIAL_OF_HYDROCHLORIC_ACID, ModFluids.HYDROCHLORIC_ACID_SOURCE);
    }

    private static void register(net.minecraftforge.registries.RegistryObject<Item> vialItem,
                                 net.minecraftforge.registries.RegistryObject<Fluid> fluid) {
        VIAL_TO_FLUID.put(vialItem.get(), fluid);
        FLUID_TO_VIAL.put(fluid, vialItem);
    }

    /** Returns the fluid a filled vial item represents, or null if this item isn't a known filled vial. */
    public static Fluid getFluidForVial(ItemStack stack) {
        for (Map.Entry<Item, java.util.function.Supplier<Fluid>> entry : VIAL_TO_FLUID.entrySet()) {
            if (stack.is(entry.getKey())) return entry.getValue().get();
        }
        return null;
    }

    /** Returns the filled-vial item that represents this fluid, or null if unmapped. */
    public static Item getVialForFluid(Fluid fluid) {
        for (Map.Entry<java.util.function.Supplier<Fluid>, java.util.function.Supplier<Item>> entry : FLUID_TO_VIAL.entrySet()) {
            if (entry.getKey().get() == fluid) return entry.getValue().get();
        }
        return null;
    }

    public static boolean isKnownFluid(Fluid fluid) {
        return getVialForFluid(fluid) != null;
    }
}