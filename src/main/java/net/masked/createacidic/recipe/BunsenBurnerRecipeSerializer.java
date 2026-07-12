package net.masked.createacidic.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class BunsenBurnerRecipeSerializer implements RecipeSerializer<BunsenBurnerRecipe> {

    @Override
    public BunsenBurnerRecipe fromJson(ResourceLocation id, JsonObject json) {
        int heatRequirement = GsonHelper.getAsInt(json, "heatRequirement", 50);
        int processingTime = GsonHelper.getAsInt(json, "processingTime", 200);
        boolean requiresFlask = GsonHelper.getAsBoolean(json, "requiresErlenmeyerFlask", false);

        JsonArray ingredientsJson = GsonHelper.getAsJsonArray(json, "ingredients");
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (JsonElement element : ingredientsJson) {
            ingredients.add(parseNbtIngredient(element.getAsJsonObject()));
        }

        JsonArray resultsJson = GsonHelper.getAsJsonArray(json, "results");
        List<ItemStack> results = new ArrayList<>();
        for (JsonElement element : resultsJson) {
            JsonObject obj = element.getAsJsonObject();
            ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(obj, "item"));
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            int count = GsonHelper.getAsInt(obj, "count", 1);
            results.add(new ItemStack(item, count));
        }

        return new BunsenBurnerRecipe(id, heatRequirement, processingTime, requiresFlask, ingredients, results);
    }

    /** Parses an ingredient entry, wrapping it in an NBT-matching Ingredient if "nbt" is present. */
    private Ingredient parseNbtIngredient(JsonObject obj) {
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(obj, "item"));
        Item item = ForgeRegistries.ITEMS.getValue(itemId);

        CompoundTag nbt = null;
        if (obj.has("nbt")) {
            try {
                nbt = TagParser.parseTag(obj.get("nbt").toString());
            } catch (Exception e) {
                throw new com.google.gson.JsonSyntaxException("Invalid NBT for ingredient " + itemId, e);
            }
        }

        return new NbtIngredient(item, nbt);
    }

    @Override
    public BunsenBurnerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        int heatRequirement = buffer.readVarInt();
        int processingTime = buffer.readVarInt();
        boolean requiresFlask = buffer.readBoolean();

        int ingredientCount = buffer.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (int i = 0; i < ingredientCount; i++) {
            ingredients.add(Ingredient.fromNetwork(buffer));
        }

        int resultCount = buffer.readVarInt();
        List<ItemStack> results = new ArrayList<>();
        for (int i = 0; i < resultCount; i++) {
            results.add(buffer.readItem());
        }

        return new BunsenBurnerRecipe(id, heatRequirement, processingTime, requiresFlask, ingredients, results);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, BunsenBurnerRecipe recipe) {
        buffer.writeVarInt(recipe.getHeatRequirement());
        buffer.writeVarInt(recipe.getProcessingTime());
        buffer.writeBoolean(recipe.requiresErlenmeyerFlask());

        buffer.writeVarInt(recipe.getIngredients().size());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.toNetwork(buffer);
        }

        buffer.writeVarInt(recipe.getResultStacks().size());
        for (ItemStack stack : recipe.getResultStacks()) {
            buffer.writeItem(stack);
        }
    }
}