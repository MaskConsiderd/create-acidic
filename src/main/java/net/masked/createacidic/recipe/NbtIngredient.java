package net.masked.createacidic.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Matches an item AND a specific NBT compound (subset match — every key/value in `requiredNbt`
 * must be present and equal on the stack's tag; extra tags on the stack are ignored).
 * If requiredNbt is null, behaves like a normal item-only Ingredient.
 */
public class NbtIngredient extends Ingredient {

    private final Item item;
    private final CompoundTag requiredNbt;

    protected NbtIngredient(Item item, CompoundTag requiredNbt) {
        super(java.util.stream.Stream.of(new ItemValue(new ItemStack(item))));
        this.item = item;
        this.requiredNbt = requiredNbt;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(item)) return false;
        if (requiredNbt == null) return true;

        CompoundTag stackTag = stack.getTag();
        if (stackTag == null) return false;

        return matchesSubset(requiredNbt, stackTag);
    }

    private boolean matchesSubset(CompoundTag required, CompoundTag actual) {
        for (String key : required.getAllKeys()) {
            if (!actual.contains(key)) return false;
            // simple value comparison; works for strings, most primitive NBT types
            if (!actual.get(key).equals(required.get(key))) return false;
        }
        return true;
    }
}