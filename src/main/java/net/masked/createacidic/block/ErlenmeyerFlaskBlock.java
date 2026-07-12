package net.masked.createacidic.block;

import net.masked.createacidic.block.entity.ErlenmeyerFlaskBlockEntity;
import net.masked.createacidic.registry.ModItems;
import net.masked.createacidic.util.VialFluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class ErlenmeyerFlaskBlock extends Block implements EntityBlock {

    private static final VoxelShape BASE = Block.box(4, 0, 4, 12, 9, 12);
    private static final VoxelShape NECK = Block.box(6, 9, 6, 10, 16, 10);
    private static final VoxelShape SHAPE = Shapes.or(BASE, NECK);

    public ErlenmeyerFlaskBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ErlenmeyerFlaskBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof ErlenmeyerFlaskBlockEntity flask) {
                flask.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ErlenmeyerFlaskBlockEntity flask)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        ItemStackHandler inv = flask.getInventory();

        // Empty hand -> pop last inserted solid item back out
        if (held.isEmpty()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            ItemStack removed = flask.removeLastInserted();
            if (!removed.isEmpty()) {
                player.getInventory().placeItemBackInInventory(removed);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Empty glass vial -> drain 50mb from the tank into a filled vial
        if (held.is(ModItems.GLASS_VIAL.get())) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            FluidStack drained = flask.drainToVial();
            if (!drained.isEmpty()) {
                var vialItem = VialFluidRegistry.getVialForFluid(drained.getFluid());
                if (vialItem != null) {
                    held.shrink(1);
                    player.getInventory().placeItemBackInInventory(new ItemStack(vialItem));
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.PASS;
        }

        // Filled vial of a known fluid -> pour 50mb into the tank, give back an empty vial
        Fluid vialFluid = VialFluidRegistry.getFluidForVial(held);
        if (vialFluid != null) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            if (flask.fillFromVial(vialFluid)) {
                held.shrink(1);
                player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.GLASS_VIAL.get()));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Any other item -> drop it into slot 0 for realism (won't react unless it's actually sodium chloride)
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack remainder = inv.insertItem(0, held.copyWithCount(1), false);
        if (remainder.isEmpty()) {
            held.shrink(1);
            flask.markInserted(0);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ErlenmeyerFlaskBlockEntity flask) {
                ItemStackHandler inv = flask.getInventory();
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}