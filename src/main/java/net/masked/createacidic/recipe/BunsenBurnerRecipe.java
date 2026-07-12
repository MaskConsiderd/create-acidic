package net.masked.createacidic.recipe;

import net.masked.createacidic.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class BunsenBurnerRecipe implements Recipe<net.minecraft.world.Container> {

    private final ResourceLocation id;
    private final int heatRequirement;
    private final int processingTime;
    private final boolean requiresErlenmeyerFlask;
    private final NonNullList<Ingredient> ingredients;
    private final List<ItemStack> results;

    public BunsenBurnerRecipe(ResourceLocation id, int heatRequirement, int processingTime,
                              boolean requiresErlenmeyerFlask, NonNullList<Ingredient> ingredients,
                              List<ItemStack> results) {
        this.id = id;
        this.heatRequirement = heatRequirement;
        this.processingTime = processingTime;
        this.requiresErlenmeyerFlask = requiresErlenmeyerFlask;
        this.ingredients = ingredients;
        this.results = results;
    }

    public int getHeatRequirement() {
        return heatRequirement;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public boolean requiresErlenmeyerFlask() {
        return requiresErlenmeyerFlask;
    }

    public List<ItemStack> getResultStacks() {
        return results;
    }

    /** Checks a single input stack (Bunsen Burner only has one input slot) against this recipe's ingredient. */
    public boolean matches(ItemStack input) {
        if (ingredients.isEmpty()) return false;
        return ingredients.get(0).test(input);
    }

    @Override
    public boolean matches(net.minecraft.world.Container container, Level level) {
        if (container.getContainerSize() == 0) return false;
        return matches(container.getItem(0));
    }

    @Override
    public ItemStack assemble(net.minecraft.world.Container container, RegistryAccess registryAccess) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0).copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BUNSEN_BURNER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BUNSEN_BURNER_TYPE;
    }
}