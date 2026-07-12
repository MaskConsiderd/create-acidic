package net.masked.createacidic.block.entity;

import net.masked.createacidic.recipe.BunsenBurnerRecipe;
import net.masked.createacidic.menu.BunsenBurnerMenu;
import net.masked.createacidic.registry.ModBlockEntities;
import net.masked.createacidic.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BunsenBurnerBlockEntity extends BlockEntity implements MenuProvider {

    public static final float MAX_TEMP = 150f;
    public static final float TEMP_STEP_PER_CLICK = 25f;
    public static final float RAMP_RATE = 0.5f;

    public static final float FUEL_PER_LAVA_BUCKET = 5000f;

    private boolean lit = false;
    private float currentTemp = 0f;
    private float targetTemp = 0f;
    private float fuelPoints = 0f;

    private int brewProgress = 0;
    private boolean brewOutputReady = false;
    private List<ItemStack> brewOutputResults = new ArrayList<>();

    private final ItemStackHandler bottleSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    };

    // Fuel slot - accepts lava buckets only, instantly consumes into fuel points,
    // leaving a plain bucket behind in the same slot.
    private final ItemStackHandler fuelHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(Items.LAVA_BUCKET);
        }

        @Override
        protected void onContentsChanged(int slot) {
            ItemStack stack = getStackInSlot(0);
            if (stack.is(Items.LAVA_BUCKET)) {
                fuelPoints += FUEL_PER_LAVA_BUCKET;
                setStackInSlot(0, new ItemStack(Items.BUCKET));
            }
            setChanged();
            syncToClient();
        }
    };

    public BunsenBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BUNSEN_BURNER_BE.get(), pos, state);
    }

    public ItemStackHandler getBottleSlot() {
        return bottleSlot;
    }

    public ItemStackHandler getFuelHandler() {
        return fuelHandler;
    }

    public int getFuelPoints() {
        return (int) fuelPoints;
    }

    public boolean insertBottle(ItemStack stack) {
        if (!bottleSlot.getStackInSlot(0).isEmpty() || brewOutputReady) return false;

        ItemStack remainder = bottleSlot.insertItem(0, stack.copyWithCount(1), false);
        return remainder.isEmpty();
    }

    public boolean canAccept(ItemStack stack) {
        return !stack.isEmpty() && bottleSlot.getStackInSlot(0).isEmpty() && !brewOutputReady;
    }

    public boolean isBrewOutputReady() {
        return brewOutputReady;
    }

    public List<ItemStack> collectBrewOutput() {
        if (!brewOutputReady) return List.of();

        List<ItemStack> results = brewOutputResults;
        brewOutputReady = false;
        brewOutputResults = new ArrayList<>();
        setChanged();
        syncToClient();

        return results;
    }

    public int getBrewProgress() {
        return brewProgress;
    }

    public float getBrewPercent(BunsenBurnerRecipe recipe) {
        if (recipe == null) return 0f;
        return (float) brewProgress / recipe.getProcessingTime();
    }

    public void toggleLit() {
        this.lit = !this.lit;
        if (!lit) {
            this.targetTemp = 0f;
        }
        setChanged();
        syncToClient();
    }

    public void increaseTargetTemp() {
        if (!lit) return;
        this.targetTemp = Math.min(MAX_TEMP, this.targetTemp + TEMP_STEP_PER_CLICK);
        setChanged();
        syncToClient();
    }

    public void decreaseTargetTemp() {
        if (!lit) return;
        this.targetTemp = Math.max(0f, this.targetTemp - TEMP_STEP_PER_CLICK);
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private BunsenBurnerRecipe findMatchingRecipe(ItemStack input) {
        if (level == null || input.isEmpty()) return null;
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.BUNSEN_BURNER_TYPE)
                .stream()
                .filter(r -> r.matches(input))
                .findFirst()
                .orElse(null);
    }

    public void tick() {
        boolean dirty = false;

        // Doesn't start draining until lit AND a temp has actually ramped up above 0.
        if (lit && fuelPoints > 0f && currentTemp > 0f) {
            float usage = 0.2f * currentTemp;
            fuelPoints -= usage;
            dirty = true;

            if (fuelPoints <= 0f) {
                fuelPoints = 0f;
                lit = false;
                targetTemp = 0f;

                if (brewProgress != 0) {
                    brewProgress = 0;
                }
            }
        }

        float goal = (lit && fuelPoints > 0f) ? targetTemp : 0f;

        if (currentTemp < goal) {
            currentTemp = Math.min(goal, currentTemp + RAMP_RATE);
            dirty = true;
        } else if (currentTemp > goal) {
            currentTemp = Math.max(goal, currentTemp - RAMP_RATE);
            dirty = true;
        }

        if (level != null && !level.isClientSide()) {
            ItemStack bottle = bottleSlot.getStackInSlot(0);

            if (!bottle.isEmpty() && !brewOutputReady) {
                BunsenBurnerRecipe recipe = findMatchingRecipe(bottle);

                if (recipe != null && lit && currentTemp >= recipe.getHeatRequirement()) {
                    brewProgress++;
                    dirty = true;
                    if (brewProgress >= recipe.getProcessingTime()) {
                        completeBrew(recipe);
                    }
                } else if (brewProgress != 0) {
                    brewProgress = 0;
                    dirty = true;
                }
            } else if (bottle.isEmpty() && !brewOutputReady && brewProgress != 0) {
                brewProgress = 0;
                dirty = true;
            }
        }

        if (dirty) {
            setChanged();
            if (level != null && !level.isClientSide() && level.getGameTime() % 5 == 0) {
                syncToClient();
            }
        }
    }

    private void completeBrew(BunsenBurnerRecipe recipe) {
        bottleSlot.extractItem(0, 1, false);
        brewOutputReady = true;
        brewOutputResults = new ArrayList<>();
        for (ItemStack result : recipe.getResultStacks()) {
            brewOutputResults.add(result.copy());
        }
        brewProgress = 0;
        setChanged();
        syncToClient();

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 1.15;
            double z = worldPosition.getZ() + 0.5;

            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    x, y, z, 12, 0.2, 0.2, 0.2, 0.02);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                    x, y, z, 6, 0.15, 0.15, 0.15, 0.01);
        }
    }

    public List<ItemStack> getBrewOutputResults() {
        return brewOutputResults;
    }

    public boolean isLit() {
        return lit;
    }

    public float getCurrentTemp() {
        return currentTemp;
    }

    public float getTargetTemp() {
        return targetTemp;
    }

    public boolean isAtReactionTemp() {
        return currentTemp >= MAX_TEMP;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.acidic.bunsen_burner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BunsenBurnerMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Lit", lit);
        tag.putFloat("CurrentTemp", currentTemp);
        tag.putFloat("TargetTemp", targetTemp);
        tag.putFloat("FuelPoints", fuelPoints);
        tag.put("BottleSlot", bottleSlot.serializeNBT());
        tag.put("FuelSlot", fuelHandler.serializeNBT());
        tag.putInt("BrewProgress", brewProgress);
        tag.putBoolean("BrewOutputReady", brewOutputReady);

        net.minecraft.nbt.ListTag resultsList = new net.minecraft.nbt.ListTag();
        for (ItemStack stack : brewOutputResults) {
            resultsList.add(stack.save(new CompoundTag()));
        }
        tag.put("BrewOutputResults", resultsList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lit = tag.getBoolean("Lit");
        currentTemp = tag.getFloat("CurrentTemp");
        targetTemp = tag.getFloat("TargetTemp");
        fuelPoints = tag.getFloat("FuelPoints");
        bottleSlot.deserializeNBT(tag.getCompound("BottleSlot"));
        fuelHandler.deserializeNBT(tag.getCompound("FuelSlot"));
        brewProgress = tag.getInt("BrewProgress");
        brewOutputReady = tag.getBoolean("BrewOutputReady");

        brewOutputResults = new ArrayList<>();
        net.minecraft.nbt.ListTag resultsList = tag.getList("BrewOutputResults", 10);
        for (int i = 0; i < resultsList.size(); i++) {
            brewOutputResults.add(ItemStack.of(resultsList.getCompound(i)));
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