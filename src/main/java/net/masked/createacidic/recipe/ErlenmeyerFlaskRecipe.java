package net.masked.createacidic.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.masked.createacidic.registry.ModRecipeTypes;
import net.minecraftforge.fluids.FluidStack;

public class ErlenmeyerFlaskRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final boolean requiresCondenser;
    private final int heatRequirement;

    private final Ingredient itemIngredient;
    private final int itemCount;

    private final FluidStack fluidIngredient; // required fluid + amount

    private final FluidStack fluidResult;

    public ErlenmeyerFlaskRecipe(ResourceLocation id, boolean requiresCondenser, int heatRequirement,
                                 Ingredient itemIngredient, int itemCount,
                                 FluidStack fluidIngredient, FluidStack fluidResult) {
        this.id = id;
        this.requiresCondenser = requiresCondenser;
        this.heatRequirement = heatRequirement;
        this.itemIngredient = itemIngredient;
        this.itemCount = itemCount;
        this.fluidIngredient = fluidIngredient;
        this.fluidResult = fluidResult;
    }

    /** Custom matcher - the flask isn't a vanilla Container, so the BE calls this directly. */
    public boolean matches(ItemStack solidInput, FluidStack tankFluid) {
        if (!itemIngredient.test(solidInput) || solidInput.getCount() < itemCount) return false;
        if (tankFluid.isEmpty() || tankFluid.getFluid() != fluidIngredient.getFluid()) return false;
        return tankFluid.getAmount() >= fluidIngredient.getAmount();
    }

    public boolean requiresCondenser() {
        return requiresCondenser;
    }

    public int getHeatRequirement() {
        return heatRequirement;
    }

    public Ingredient getItemIngredient() {
        return itemIngredient;
    }

    public int getItemCount() {
        return itemCount;
    }

    public FluidStack getFluidIngredient() {
        return fluidIngredient;
    }

    public FluidStack getFluidResult() {
        return fluidResult.copy();
    }

    // --- Required Recipe<Container> plumbing (unused - matching is manual) ---

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ERLENMEYER_FLASK_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ERLENMEYER_FLASK_TYPE;
    }
}