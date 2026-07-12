package net.masked.createacidic.item;

import net.minecraft.world.item.ArmorItem;

public class ScienceGogglesItem extends ArmorItem {

    public ScienceGogglesItem(Properties properties) {
        super(ScientistArmorMaterial.SCIENTIST_WEAR, ArmorItem.Type.HELMET, properties);
    }

    private static final java.util.List<java.util.function.Predicate<net.minecraft.world.entity.player.Player>> IS_WEARING_PREDICATES = new java.util.ArrayList<>();

    static {
        IS_WEARING_PREDICATES.add(player ->
                player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof ScienceGogglesItem);
    }

    public static boolean isWearingGoggles(net.minecraft.world.entity.player.Player player) {
        for (var predicate : IS_WEARING_PREDICATES) {
            if (predicate.test(player)) return true;
        }
        return false;
    }
}