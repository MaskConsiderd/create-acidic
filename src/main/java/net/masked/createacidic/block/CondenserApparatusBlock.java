package net.masked.createacidic.block;

import net.masked.createacidic.block.entity.CondenserApparatusBlockEntity;
import net.masked.createacidic.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
import org.jetbrains.annotations.Nullable;

public class CondenserApparatusBlock extends Block implements EntityBlock {

    // Base ring
    private static final VoxelShape BASE = Block.box(6, 0, 6, 10, 1, 10);
    // Main vertical tube
    private static final VoxelShape TUBE = Block.box(7, 1, 7, 9, 10, 9);
    // Top ring/cap
    private static final VoxelShape TOP_RING = Block.box(6, 10, 6, 10, 12, 10);

    private static final VoxelShape SHAPE = Shapes.or(BASE, TUBE, TOP_RING);

    public CondenserApparatusBlock(Properties properties) {
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
        return new CondenserApparatusBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof CondenserApparatusBlockEntity condenser) {
                condenser.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        if (!(level.getBlockEntity(pos) instanceof CondenserApparatusBlockEntity condenser)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.is(ModItems.GLASS_VIAL.get()) && condenser.isReadyForCollection()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            var vialItem = net.masked.createacidic.util.VialFluidRegistry
                    .getVialForFluid(condenser.getPendingResult().getFluid());

            if (vialItem != null) {
                ItemStack result = new ItemStack(vialItem);
                held.shrink(1);
                player.getInventory().placeItemBackInInventory(result);
                condenser.collect();
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}