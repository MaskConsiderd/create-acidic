package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CreateAcidic.MODID);

    public static final RegistryObject<SoundEvent> CHEMICAL_FIZZING = SOUND_EVENTS.register(
            "chemical_fizzing",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(CreateAcidic.MODID, "chemical_fizzing")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}