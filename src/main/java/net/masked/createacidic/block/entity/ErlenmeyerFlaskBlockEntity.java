package net.masked.createacidic.block.entity;

import net.masked.createacidic.network.FizzSoundPacket;
import net.masked.createacidic.network.ModNetworking;
import net.masked.createacidic.recipe.ErlenmeyerFlaskRecipe;
import net.masked.createacidic.registry.ModBlockEntities;
import net.masked.createacidic.registry.ModItems;
import net.masked.createacidic.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.masked.createacidic.api.IHaveScienceGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;

public class ErlenmeyerFlaskBlockEntity extends BlockEntity implements IHaveScienceGoggleInformation {

    public static final int REQUIRED_SODIUM_CHLORIDE = 5;
    public static final int REQUIRED_SULFURIC_ACID_MB = 50;
    public static final int REACTION_TIME_TICKS = 20 * 20; // 20 seconds
    public static final int TANK_CAPACITY_MB = 1000;
    private static final int REACTION_DECAY_PER_TICK = 1;

    // Slot 0 = solid input (accepts any item, chemistry checked separately)
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true; // realism: accept anything, only real chemicals react
        }
    };

    // Holds any fluid poured/vialed in (sulfuric acid, hydrochloric acid, water, etc.)
    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY_MB) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClient();
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandlerOptional = LazyOptional.of(() -> fluidTank);

    private int reactionProgress = 0;
    private boolean reacting = false;

    // Tracks whether the flask was reacting on the previous tick, so the fizzing
    // sound start/stop packet only fires on the transition, not every tick.
    private boolean wasReacting = false;

    // Tracks item insertion order so right-click-empty-hand can pop items off LIFO
    private final Deque<Integer> insertionHistory = new ArrayDeque<>();

    public ErlenmeyerFlaskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ERLENMEYER_FLASK_BE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandlerOptional.invalidate();
    }

    public boolean hasRequiredInputs() {
        ItemStack slot0 = inventory.getStackInSlot(0);
        return slot0.is(ModItems.SODIUM_CHLORIDE.get()) && slot0.getCount() >= REQUIRED_SODIUM_CHLORIDE
                && fluidTank.getFluid().getFluid() == net.masked.createacidic.registry.ModFluids.SULFURIC_ACID_SOURCE.get()
                && fluidTank.getFluidAmount() >= REQUIRED_SULFURIC_ACID_MB;
    }

    public void markInserted(int slot) {
        insertionHistory.push(slot);
        setChanged();
        syncToClient();
    }

    /** Pops the last inserted solid item (slot 0) back out. */
    public ItemStack removeLastInserted() {
        ItemStack result = ItemStack.EMPTY;

        if (!insertionHistory.isEmpty()) {
            insertionHistory.pop();
            ItemStack extracted = inventory.extractItem(0, 1, false);
            if (!extracted.isEmpty()) {
                result = extracted;
            }
        }

        if (!result.isEmpty()) {
            setChanged();
            syncToClient();
        }
        return result;
    }

    /** Fills VIAL_AMOUNT_MB of the given fluid into the tank. Returns true on success. */
    public boolean fillFromVial(net.minecraft.world.level.material.Fluid fluid) {
        FluidStack toFill = new FluidStack(fluid, net.masked.createacidic.util.VialFluidRegistry.VIAL_AMOUNT_MB);
        int filled = fluidTank.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
        if (filled < net.masked.createacidic.util.VialFluidRegistry.VIAL_AMOUNT_MB) return false;

        fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        setChanged();
        syncToClient();
        return true;
    }

    /** Drains VIAL_AMOUNT_MB from the tank. Returns the drained FluidStack, or empty if not enough present. */
    public FluidStack drainToVial() {
        FluidStack drained = fluidTank.drain(net.masked.createacidic.util.VialFluidRegistry.VIAL_AMOUNT_MB, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty() || drained.getAmount() < net.masked.createacidic.util.VialFluidRegistry.VIAL_AMOUNT_MB) {
            return FluidStack.EMPTY;
        }

        FluidStack result = fluidTank.drain(net.masked.createacidic.util.VialFluidRegistry.VIAL_AMOUNT_MB, IFluidHandler.FluidAction.EXECUTE);
        setChanged();
        syncToClient();
        return result;
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private BunsenBurnerBlockEntity getBurnerBelow() {
        if (level == null) return null;
        BlockPos below = worldPosition.below();
        if (level.getBlockEntity(below) instanceof BunsenBurnerBlockEntity burner) {
            return burner;
        }
        return null;
    }

    private CondenserApparatusBlockEntity getCondenserAbove() {
        if (level == null) return null;
        BlockPos above = worldPosition.above();
        if (level.getBlockEntity(above) instanceof CondenserApparatusBlockEntity condenser) {
            return condenser;
        }
        return null;
    }

    /** Tells nearby clients to start this flask's fizzing sound, keyed to this exact BlockPos. */
    private void startFizzingSound() {
        if (level instanceof ServerLevel serverLevel) {
            ModNetworking.CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverLevel.getChunkAt(worldPosition)),
                    new FizzSoundPacket(worldPosition, true));
        }
    }

    /** Tells nearby clients to stop this flask's fizzing sound instance specifically - never affects other flasks. */
    private void stopFizzingSound() {
        if (level instanceof ServerLevel serverLevel) {
            ModNetworking.CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverLevel.getChunkAt(worldPosition)),
                    new FizzSoundPacket(worldPosition, false));
        }
    }

    /** Spawns happy-villager particles inside the flask body while it's actively reacting. Mirrors the condenser's server-side sendParticles pattern. */
    private void spawnReactionParticles(ServerLevel serverLevel) {
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.55;
        double z = worldPosition.getZ() + 0.5;

        if (serverLevel.getGameTime() % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0.06, 0.05, 0.06, 0.0);
        }
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        BunsenBurnerBlockEntity burner = getBurnerBelow();
        CondenserApparatusBlockEntity condenser = getCondenserAbove();

        ItemStack solidInput = inventory.getStackInSlot(0);
        FluidStack tankFluid = fluidTank.getFluid();

        ErlenmeyerFlaskRecipe recipe = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ERLENMEYER_FLASK_TYPE)
                .stream()
                .filter(r -> r.matches(solidInput, tankFluid))
                .findFirst()
                .orElse(null);

        // The condenser is always required: completeReaction() always hands the fluid
        // result off to it, regardless of what recipe.requiresCondenser() says. Checking
        // that flag here instead of requiring condenser != null unconditionally was the
        // bug — a recipe with requiresCondenser=false could complete with condenser==null
        // and NPE on the line below in completeReaction().
        boolean conditionsMet = recipe != null
                && burner != null
                && burner.isLit()
                && burner.getCurrentTemp() >= recipe.getHeatRequirement()
                && condenser != null;

        if (conditionsMet) {
            if (!wasReacting) {
                startFizzingSound();
            }

            reacting = true;
            reactionProgress++;
            setChanged();

            if (level instanceof ServerLevel serverLevel) {
                spawnReactionParticles(serverLevel);
            }

            if (reactionProgress >= REACTION_TIME_TICKS) {
                completeReaction(condenser, recipe);
            }
        } else if (reactionProgress > 0) {
            if (wasReacting) {
                stopFizzingSound();
            }
            reactionProgress = Math.max(0, reactionProgress - REACTION_DECAY_PER_TICK);
            reacting = false;
            setChanged();
        } else if (reacting) {
            if (wasReacting) {
                stopFizzingSound();
            }
            reacting = false;
            setChanged();
        }

        wasReacting = conditionsMet;
    }

    private void completeReaction(CondenserApparatusBlockEntity condenser, ErlenmeyerFlaskRecipe recipe) {
        inventory.extractItem(0, recipe.getItemCount(), false);
        fluidTank.drain(recipe.getFluidIngredient().getAmount(), IFluidHandler.FluidAction.EXECUTE);

        insertionHistory.clear();

        condenser.startProducing(recipe.getFluidResult());

        stopFizzingSound();
        wasReacting = false;

        if (level != null) {
            level.playSound(null, worldPosition,
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS,
                    1.0F, 1.0F);
        }

        reactionProgress = 0;
        reacting = false;
        setChanged();
        syncToClient();
    }

    public boolean isReacting() {
        return reacting;
    }

    public int getReactionProgress() {
        return reactionProgress;
    }

    public float getReactionPercent() {
        return (float) reactionProgress / REACTION_TIME_TICKS;
    }

    @Override
    public boolean addToScienceGoggleTooltip(List<Component> tooltip, boolean isSneaking) {
        tooltip.add(Component.literal("Erlenmeyer Flask")
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));

        if (!reacting) {
            tooltip.add(Component.literal("Idle").withStyle(ChatFormatting.DARK_GRAY));
            return true;
        }

        int ticksLeft = Math.max(0, REACTION_TIME_TICKS - reactionProgress);
        int seconds = ticksLeft / 20;

        tooltip.add(Component.literal("Reacting: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(seconds + "s remaining")
                        .withStyle(ChatFormatting.AQUA)));

        int percent = (int) (getReactionPercent() * 100f);
        tooltip.add(Component.literal("Progress: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(percent + "%")
                        .withStyle(ChatFormatting.GREEN)));

        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.put("FluidTank", fluidTank.writeToNBT(new CompoundTag()));
        tag.putInt("ReactionProgress", reactionProgress);
        tag.putBoolean("Reacting", reacting);
        tag.putIntArray("InsertionHistory", insertionHistory.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        fluidTank.readFromNBT(tag.getCompound("FluidTank"));
        reactionProgress = tag.getInt("ReactionProgress");
        reacting = tag.getBoolean("Reacting");

        insertionHistory.clear();
        int[] history = tag.getIntArray("InsertionHistory");
        for (int slot : history) {
            insertionHistory.addLast(slot);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}