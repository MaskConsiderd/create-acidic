package net.masked.createacidic.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public enum ScientistArmorMaterial implements ArmorMaterial {
    SCIENTIST_WEAR;

    private static final EnumMap<ArmorItem.Type, Integer> DEFENSE = new EnumMap<>(ArmorItem.Type.class);
    static {
        DEFENSE.put(ArmorItem.Type.HELMET, 1);
        DEFENSE.put(ArmorItem.Type.CHESTPLATE, 2);
        DEFENSE.put(ArmorItem.Type.LEGGINGS, 0);
        DEFENSE.put(ArmorItem.Type.BOOTS, 0);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return 15; // cosmetic-ish, low durability is fine for lab wear
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return DEFENSE.getOrDefault(type, 0);
    }

    @Override
    public int getEnchantmentValue() {
        return 9;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(net.masked.createacidic.registry.ModItems.SODIUM_CHLORIDE.get());
        // swap for whatever repair material makes sense
    }

    @Override
    public String getName() {
        return "acidic:scientist_wear";
    }

    @Override
    public float getToughness() {
        return 0f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0f;
    }
}