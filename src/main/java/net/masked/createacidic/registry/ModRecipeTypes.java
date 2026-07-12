package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.recipe.ErlenmeyerFlaskRecipe;
import net.masked.createacidic.recipe.ErlenmeyerFlaskRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateAcidic.MODID);

    public static final RegistryObject<RecipeSerializer<ErlenmeyerFlaskRecipe>> ERLENMEYER_FLASK_SERIALIZER =
            SERIALIZERS.register("erlenmeyer_flask", ErlenmeyerFlaskRecipeSerializer::new);

    // No longer created via RecipeType.register() in a static field - that runs too late.
    // Instead, this holds a manually-constructed RecipeType, and the actual vanilla
    // registration happens via RegisterEvent in CreateAcidic's constructor (see below).
    public static final RecipeType<ErlenmeyerFlaskRecipe> ERLENMEYER_FLASK_TYPE =
            new RecipeType<ErlenmeyerFlaskRecipe>() {
                @Override
                public String toString() {
                    return CreateAcidic.MODID + ":erlenmeyer_flask";
                }
            };
}