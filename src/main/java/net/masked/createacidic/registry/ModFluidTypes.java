package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CreateAcidic.MODID);

    public static final ResourceLocation SULFURIC_STILL_TEXTURE =
            new ResourceLocation(CreateAcidic.MODID, "block/sulfuric_acid_still");
    public static final ResourceLocation SULFURIC_FLOW_TEXTURE =
            new ResourceLocation(CreateAcidic.MODID, "block/sulfuric_acid_flow");

    public static final ResourceLocation HYDROCHLORIC_STILL_TEXTURE =
            new ResourceLocation(CreateAcidic.MODID, "block/hydrochloric_acid_still");
    public static final ResourceLocation HYDROCHLORIC_FLOW_TEXTURE =
            new ResourceLocation(CreateAcidic.MODID, "block/hydrochloric_acid_flow");

    public static final RegistryObject<FluidType> SULFURIC_ACID_TYPE =
            FLUID_TYPES.register("sulfuric_acid", () -> new FluidType(
                    FluidType.Properties.create()
                            .density(1200)
                            .viscosity(1400)
                            .lightLevel(0)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                            .canConvertToSource(false)
            ) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return SULFURIC_STILL_TEXTURE;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return SULFURIC_FLOW_TEXTURE;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFFFFFFF;
                        }
                    });
                }
            });

    public static final RegistryObject<FluidType> HYDROCHLORIC_ACID_TYPE =
            FLUID_TYPES.register("hydrochloric_acid", () -> new FluidType(
                    FluidType.Properties.create()
                            .density(1200)
                            .viscosity(1400)
                            .lightLevel(0)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                            .canConvertToSource(false)
            ) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return HYDROCHLORIC_STILL_TEXTURE;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return HYDROCHLORIC_FLOW_TEXTURE;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFFFFFFF; // your texture carries the color
                        }
                    });
                }
            });
}