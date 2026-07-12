package net.masked.createacidic.registry;

import net.masked.createacidic.CreateAcidic;
import net.masked.createacidic.recipe.BunsenBurnerRecipe;
import net.masked.createacidic.recipe.BunsenBurnerRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateAcidic.MODID);

    public static final RegistryObject<BunsenBurnerRecipeSerializer> BUNSEN_BURNER_SERIALIZER =
            SERIALIZERS.register("bunsen_burner", BunsenBurnerRecipeSerializer::new);

    public static final RecipeType<BunsenBurnerRecipe> BUNSEN_BURNER_TYPE =
            new RecipeType<BunsenBurnerRecipe>() {
                @Override
                public String toString() {
                    return CreateAcidic.MODID + ":bunsen_burner";
                }
            };

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }

    public static void registerRecipeType() {
        net.minecraftforge.registries.ForgeRegistries.RECIPE_TYPES.register(
                CreateAcidic.MODID + ":bunsen_burner",
                BUNSEN_BURNER_TYPE);
    }
}