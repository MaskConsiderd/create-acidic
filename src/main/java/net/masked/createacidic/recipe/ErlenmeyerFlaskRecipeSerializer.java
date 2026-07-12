package net.masked.createacidic.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ErlenmeyerFlaskRecipeSerializer implements RecipeSerializer<ErlenmeyerFlaskRecipe> {

    @Override
    public ErlenmeyerFlaskRecipe fromJson(ResourceLocation id, JsonObject json) {
        boolean requiresCondenser = Boolean.parseBoolean(GsonHelper.getAsString(json, "condenserApparatus", "false"));
        int heatRequirement = Integer.parseInt(GsonHelper.getAsString(json, "heatRequirement", "0"));

        Ingredient itemIngredient = Ingredient.EMPTY;
        int itemCount = 0;
        FluidStack fluidIngredient = FluidStack.EMPTY;

        for (var element : GsonHelper.getAsJsonArray(json, "ingredients")) {
            JsonObject entry = element.getAsJsonObject();
            if (entry.has("item")) {
                itemIngredient = Ingredient.of(ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation(GsonHelper.getAsString(entry, "item"))));
                itemCount = GsonHelper.getAsInt(entry, "count", 1);
            } else if (entry.has("fluid")) {
                var fluid = ForgeRegistries.FLUIDS.getValue(
                        new ResourceLocation(GsonHelper.getAsString(entry, "fluid")));
                int amount = GsonHelper.getAsInt(entry, "amount", 0);
                fluidIngredient = new FluidStack(fluid, amount);
            }
        }

        FluidStack fluidResult = FluidStack.EMPTY;
        for (var element : GsonHelper.getAsJsonArray(json, "results")) {
            JsonObject entry = element.getAsJsonObject();
            if (entry.has("fluid")) {
                var fluid = ForgeRegistries.FLUIDS.getValue(
                        new ResourceLocation(GsonHelper.getAsString(entry, "fluid")));
                int amount = GsonHelper.getAsInt(entry, "amount", 0);
                fluidResult = new FluidStack(fluid, amount);
            }
        }

        return new ErlenmeyerFlaskRecipe(id, requiresCondenser, heatRequirement,
                itemIngredient, itemCount, fluidIngredient, fluidResult);
    }

    @Override
    public ErlenmeyerFlaskRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        boolean requiresCondenser = buf.readBoolean();
        int heatRequirement = buf.readVarInt();
        Ingredient itemIngredient = Ingredient.fromNetwork(buf);
        int itemCount = buf.readVarInt();
        FluidStack fluidIngredient = FluidStack.readFromPacket(buf);
        FluidStack fluidResult = FluidStack.readFromPacket(buf);
        return new ErlenmeyerFlaskRecipe(id, requiresCondenser, heatRequirement,
                itemIngredient, itemCount, fluidIngredient, fluidResult);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ErlenmeyerFlaskRecipe recipe) {
        buf.writeBoolean(recipe.requiresCondenser());
        buf.writeVarInt(recipe.getHeatRequirement());
        recipe.getItemIngredient().toNetwork(buf);
        buf.writeVarInt(recipe.getItemCount());
        recipe.getFluidIngredient().writeToPacket(buf);
        recipe.getFluidResult().writeToPacket(buf);
    }
}