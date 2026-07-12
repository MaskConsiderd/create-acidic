package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.fluid.SulfuricAcidFluid;
import net.masked.createacidic.fluid.HydrochloricAcidFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, CreateAcidic.MODID);

    public static final RegistryObject<Fluid> SULFURIC_ACID_SOURCE =
            FLUIDS.register("sulfuric_acid_source", SulfuricAcidFluid.Source::new);

    public static final RegistryObject<Fluid> SULFURIC_ACID_FLOWING =
            FLUIDS.register("sulfuric_acid_flowing", SulfuricAcidFluid.Flowing::new);

    public static final RegistryObject<Fluid> HYDROCHLORIC_ACID_SOURCE =
            FLUIDS.register("hydrochloric_acid_source", HydrochloricAcidFluid.Source::new);

    public static final RegistryObject<Fluid> HYDROCHLORIC_ACID_FLOWING =
            FLUIDS.register("hydrochloric_acid_flowing", HydrochloricAcidFluid.Flowing::new);
}