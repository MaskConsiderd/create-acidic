package net.masked.createacidic.block.entity;

import net.masked.createacidic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class CondenserApparatusBlockEntity extends BlockEntity {

    private FluidStack pendingResult = FluidStack.EMPTY;
    private boolean readyForCollection = false;

    public CondenserApparatusBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDENSER_APPARATUS_BE.get(), pos, state);
    }

    /** Called by the Erlenmeyer Flask below once its reaction completes. */
    public void startProducing(FluidStack result) {
        this.pendingResult = result.copy();
        this.readyForCollection = true;
        setChanged();
        syncToClient();
    }

    public boolean isReadyForCollection() {
        return readyForCollection && !pendingResult.isEmpty();
    }

    public FluidStack getPendingResult() {
        return pendingResult;
    }

    /** Called when a player (or deployer) successfully collects the result into a vial. */
    public void collect() {
        this.pendingResult = FluidStack.EMPTY;
        this.readyForCollection = false;
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        if (isReadyForCollection() && level instanceof ServerLevel serverLevel) {
            // spawn particles around the condenser as a visual "ready" signal
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.75;
            double z = worldPosition.getZ() + 0.5;

            serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 2, 0.15, 0.15, 0.15, 0.01);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("PendingResult", pendingResult.writeToNBT(new CompoundTag()));
        tag.putBoolean("ReadyForCollection", readyForCollection);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("PendingResult")) {
            pendingResult = FluidStack.loadFluidStackFromNBT(tag.getCompound("PendingResult"));
        } else {
            pendingResult = FluidStack.EMPTY;
        }
        readyForCollection = tag.getBoolean("ReadyForCollection");
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