package net.masked.createacidic;

import com.mojang.logging.LogUtils;
import net.masked.createacidic.network.ModNetworking;
import net.masked.createacidic.registry.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateAcidic.MODID)
public class CreateAcidic
{
    public static final String MODID = "acidic";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateAcidic(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluidTypes.FLUID_TYPES.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModRecipeTypes.SERIALIZERS.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModNetworking.register();

        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener((net.minecraftforge.registries.RegisterEvent event) -> {
            event.register(net.minecraftforge.registries.ForgeRegistries.Keys.RECIPE_TYPES,
                    helper -> {
                        helper.register(new net.minecraft.resources.ResourceLocation(CreateAcidic.MODID, "bunsen_burner"),
                                ModRecipes.BUNSEN_BURNER_TYPE);
                        helper.register(new net.minecraft.resources.ResourceLocation(CreateAcidic.MODID, "erlenmeyer_flask"),
                                ModRecipeTypes.ERLENMEYER_FLASK_TYPE);
                    });
        });

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            com.simibubi.create.content.equipment.goggles.GogglesItem.addIsWearingPredicate(
                    player -> player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)
                            .getItem() instanceof net.masked.createacidic.item.ScienceGogglesItem);
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // no longer needed for our items — handled via ModCreativeTabs' displayItems
        // left in place in case you want to inject our items into a vanilla tab too later
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        net.masked.createacidic.registry.ModBlocks.CONDENSER_APPARATUS.get(),
                        net.minecraft.client.renderer.RenderType.cutoutMipped());

                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        net.masked.createacidic.registry.ModBlocks.ERLENMEYER_FLASK.get(),
                        net.minecraft.client.renderer.RenderType.cutoutMipped());

                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        net.masked.createacidic.registry.ModFluids.SULFURIC_ACID_SOURCE.get(),
                        net.minecraft.client.renderer.RenderType.translucent());

                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        net.masked.createacidic.registry.ModFluids.SULFURIC_ACID_FLOWING.get(),
                        net.minecraft.client.renderer.RenderType.translucent());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        net.masked.createacidic.registry.ModFluids.HYDROCHLORIC_ACID_SOURCE.get(),
                        net.minecraft.client.renderer.RenderType.translucent());

                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        net.masked.createacidic.registry.ModFluids.HYDROCHLORIC_ACID_FLOWING.get(),
                        net.minecraft.client.renderer.RenderType.translucent());

                net.minecraft.client.gui.screens.MenuScreens.register(
                        net.masked.createacidic.registry.ModMenuTypes.BUNSEN_BURNER_MENU.get(),
                        net.masked.createacidic.client.screen.BunsenBurnerScreen::new);
            });
        }

        @SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void onAddLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
            for (String skinName : event.getSkins()) {
                var renderer = event.getSkin(skinName);
                if (renderer == null) continue;

                net.minecraft.client.renderer.entity.LivingEntityRenderer<net.minecraft.client.player.AbstractClientPlayer, net.minecraft.client.model.HumanoidModel<net.minecraft.client.player.AbstractClientPlayer>> playerRenderer =
                        (net.minecraft.client.renderer.entity.LivingEntityRenderer<net.minecraft.client.player.AbstractClientPlayer, net.minecraft.client.model.HumanoidModel<net.minecraft.client.player.AbstractClientPlayer>>) renderer;

                playerRenderer.addLayer(new net.masked.createacidic.client.renderer.layer.ScienceGogglesLensLayer<>(playerRenderer));
            }
        }

        @SubscribeEvent
        public static void registerGuiOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
            event.registerAbove(
                    net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id(),
                    "science_goggle_info",
                    net.masked.createacidic.client.overlay.ScienceGogglesOverlayRenderer.OVERLAY);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerBlockEntityRenderer(
                    net.masked.createacidic.registry.ModBlockEntities.ERLENMEYER_FLASK_BE.get(),
                    net.masked.createacidic.client.renderer.ErlenmeyerFlaskRenderer::new);
            event.registerBlockEntityRenderer(
                    net.masked.createacidic.registry.ModBlockEntities.BUNSEN_BURNER_BE.get(),
                    net.masked.createacidic.client.renderer.BunsenBurnerRenderer::new);
        }
    }
}