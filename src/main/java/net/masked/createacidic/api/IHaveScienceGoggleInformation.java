package net.masked.createacidic.api;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Implement this on a BlockEntity to contribute lines to the Science Goggles overlay.
 * Mirrors Create's IHaveGoggleInformation, but gated on our own goggles item.
 */
public interface IHaveScienceGoggleInformation {

    /**
     * Called every frame while the player looks at this block entity wearing the Science Goggles.
     *
     * @param tooltip      mutable list to append Components to (first line renders as a title)
     * @param isSneaking   whether the player is sneaking (use for expanded/condensed info)
     * @return true if info was added and the overlay should show; false to suppress it
     */
    boolean addToScienceGoggleTooltip(List<Component> tooltip, boolean isSneaking);
}