package net.masked.createacidic.client.sound;

import net.masked.createacidic.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;

/** Tracks one fizzing sound instance per flask BlockPos, so each flask's sound can be stopped independently. */
public class FizzSoundManager {

    private static final Map<BlockPos, SimpleSoundInstance> ACTIVE = new HashMap<>();

    public static void handle(BlockPos pos, boolean start) {
        if (start) {
            start(pos);
        } else {
            stop(pos);
        }
    }

    private static void start(BlockPos pos) {
        BlockPos key = pos.immutable();
        stop(key); // guard against double-start leaving an orphaned instance

        SimpleSoundInstance instance = new SimpleSoundInstance(
                ModSounds.CHEMICAL_FIZZING.get().getLocation(),
                SoundSource.BLOCKS,
                1.0F, 1.0F,
                RandomSource.create(),
                false, 0,
                SoundInstance.Attenuation.LINEAR,
                key.getX() + 0.5, key.getY() + 0.5, key.getZ() + 0.5,
                false);

        Minecraft.getInstance().getSoundManager().play(instance);
        ACTIVE.put(key, instance);
    }

    private static void stop(BlockPos pos) {
        SimpleSoundInstance instance = ACTIVE.remove(pos.immutable());
        if (instance != null) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
    }
}