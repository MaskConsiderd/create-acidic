package net.masked.createacidic.event;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.registry.ModItems;
import net.masked.createacidic.util.VialFluidRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAcidic.MODID)
public class VialFluidHandlerEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack held = event.getItemStack();
        Level level = event.getLevel();

        BlockEntity be = level.getBlockEntity(event.getPos());
        if (be == null) return;

        // Skip our own flask - it has its own dedicated interaction logic in ErlenmeyerFlaskBlock
        if (be instanceof net.masked.createacidic.block.entity.ErlenmeyerFlaskBlockEntity) return;

        var handlerOpt = be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace());
        if (!handlerOpt.isPresent()) return;

        IFluidHandler handler = handlerOpt.orElse(null);
        if (handler == null) return;

        // Empty vial -> try draining 50mb of a known fluid from the target
        if (held.is(ModItems.GLASS_VIAL.get())) {
            FluidStack simulated = handler.drain(VialFluidRegistry.VIAL_AMOUNT_MB, IFluidHandler.FluidAction.SIMULATE);
            if (simulated.isEmpty() || simulated.getAmount() < VialFluidRegistry.VIAL_AMOUNT_MB) return;

            var vialItem = VialFluidRegistry.getVialForFluid(simulated.getFluid());
            if (vialItem == null) return; // unknown fluid, don't touch it

            if (level.isClientSide()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            handler.drain(VialFluidRegistry.VIAL_AMOUNT_MB, IFluidHandler.FluidAction.EXECUTE);
            held.shrink(1);
            event.getEntity().getInventory().placeItemBackInInventory(new ItemStack(vialItem));

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            return;
        }

        // Filled known vial -> try filling 50mb into the target
        Fluid vialFluid = VialFluidRegistry.getFluidForVial(held);
        if (vialFluid != null) {
            FluidStack toFill = new FluidStack(vialFluid, VialFluidRegistry.VIAL_AMOUNT_MB);
            int simulated = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
            if (simulated < VialFluidRegistry.VIAL_AMOUNT_MB) return;

            if (level.isClientSide()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            held.shrink(1);
            event.getEntity().getInventory().placeItemBackInInventory(new ItemStack(ModItems.GLASS_VIAL.get()));

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        }
    }
}