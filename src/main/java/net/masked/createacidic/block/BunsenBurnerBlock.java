package net.masked.createacidic.block;

import net.masked.createacidic.block.entity.BunsenBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class BunsenBurnerBlock extends Block implements EntityBlock {

    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    private static final VoxelShape BASE = Block.box(2.5, 0, 2.5, 13.5, 2, 13.5);
    private static final VoxelShape STEM = Block.box(6.5, 2, 6.5, 9.5, 12, 9.5);
    private static final VoxelShape TOP_PLATE = Block.box(3, 15, 3, 13, 15.5, 13);

    private static final VoxelShape SHAPE = Shapes.or(BASE, STEM, TOP_PLATE);

    public BunsenBurnerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
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
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 5 : 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BunsenBurnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof BunsenBurnerBlockEntity burner) {
                burner.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BunsenBurnerBlockEntity burner)) {
            return InteractionResult.PASS;
        }

        boolean clickedTop = hit.getDirection() == Direction.UP;
        ItemStack held = player.getItemInHand(hand);

        if (clickedTop) {
            // ... unchanged, same as before (place item / collect brew / adjust temp) ...
            if (burner.canAccept(held)) {
                if (level.isClientSide()) return InteractionResult.SUCCESS;

                if (burner.insertBottle(held)) {
                    held.shrink(1);
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Placed item on burner"),
                            true);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }

            if (held.isEmpty()) {
                if (burner.isBrewOutputReady()) {
                    if (level.isClientSide()) return InteractionResult.SUCCESS;

                    java.util.List<ItemStack> results = burner.collectBrewOutput();
                    for (ItemStack result : results) {
                        player.getInventory().placeItemBackInInventory(result);
                    }
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Collected brew results!"),
                            true);
                    return InteractionResult.CONSUME;
                }

                if (level.isClientSide()) return InteractionResult.SUCCESS;

                if (player.isShiftKeyDown()) {
                    burner.decreaseTargetTemp();
                } else {
                    burner.increaseTargetTemp();
                }
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Target temp: " + burner.getTargetTemp() + "°C"),
                        true);
                return InteractionResult.CONSUME;
            }

            if (level.isClientSide()) return InteractionResult.SUCCESS;

            if (player.isShiftKeyDown()) {
                burner.decreaseTargetTemp();
            } else {
                burner.increaseTargetTemp();
            }
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "Target temp: " + burner.getTargetTemp() + "°C"),
                    true);
            return InteractionResult.CONSUME;
        }

        // Not clicking the top:
        if (player.isShiftKeyDown()) {
            // Shift-right-click the base -> open fuel GUI
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, burner, pos);
            }
            return InteractionResult.CONSUME;
        }

        // Plain right-click the base -> toggle lit (unchanged)
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        burner.toggleLit();
        level.setBlock(pos, state.setValue(LIT, burner.isLit()), 3);

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        burner.isLit() ? "Bunsen Burner: ON" : "Bunsen Burner: OFF"),
                true);

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BunsenBurnerBlockEntity burner) {
                ItemStackHandler inv = burner.getBottleSlot();
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