package net.masked.createacidic.event;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = CreateAcidic.MODID)
public class BrushStoneHandler {

    private static final Random RANDOM = new Random();
    private static final float SALT_CHANCE = 0.05F; // 5%

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack held = event.getItemStack();
        if (!held.is(Items.BRUSH)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(BlockTags.BASE_STONE_OVERWORLD)) return;

        FluidState fluidState = level.getFluidState(pos);
        boolean underwater = fluidState.is(FluidTags.WATER);
        if (!underwater) {
            underwater = level.getFluidState(pos.above()).is(FluidTags.WATER);
        }

        if (!underwater) return;

        if (level.isClientSide()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (RANDOM.nextFloat() < SALT_CHANCE) {
            ItemStack salt = new ItemStack(ModItems.SALT.get());
            if (event.getEntity() instanceof ServerPlayer player) {
                if (!player.getInventory().add(salt)) {
                    player.drop(salt, false);
                }
            }
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }
}