package com.torr.terracraft.client;

import com.torr.terracraft.config.ElevationMappingMode;
import com.torr.terracraft.config.FloraPlacementMode;
import com.torr.terracraft.config.HorizontalScaleOption;
import com.torr.terracraft.terracraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = terracraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CreateWorldPlanetEarthUi
{
    private static final int BUTTON_WIDTH = 204;
    private static final int BUTTON_HEIGHT = 20;

    private CreateWorldPlanetEarthUi()
    {
    }

    @SubscribeEvent
    public static void onCreateWorldScreenInit(ScreenEvent.Init.Post event)
    {
        if (!(event.getScreen() instanceof CreateWorldScreen screen))
        {
            return;
        }

        WorldCreationUiState uiState = screen.getUiState();
        PlanetEarthSettings initialSettings = PlanetEarthSettings.read(uiState.getSettings());

        CycleButton<FloraPlacementMode> floraButton = CycleButton.<FloraPlacementMode>builder(CreateWorldPlanetEarthUi::floraLabel)
                .withValues(FloraPlacementMode.HISTORICAL, FloraPlacementMode.BIOME)
                .withInitialValue(initialSettings.floraPlacement())
                .create(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, fieldLabel("terracraft.create_world.flora_placement"),
                        (button, value) -> applySettings(uiState, currentSettings(uiState, value, null, null)));

        CycleButton<HorizontalScaleOption> scaleButton = CycleButton.<HorizontalScaleOption>builder(HorizontalScaleOption::label)
                .withValues(HorizontalScaleOption.values())
                .withInitialValue(initialSettings.horizontalScale())
                .create(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, fieldLabel("terracraft.create_world.horizontal_scale"),
                        (button, value) -> applySettings(uiState, currentSettings(uiState, null, value, null)));

        CycleButton<ElevationMappingMode> elevationButton = CycleButton.<ElevationMappingMode>builder(CreateWorldPlanetEarthUi::elevationLabel)
                .withValues(ElevationMappingMode.COASTAL_LOG, ElevationMappingMode.LINEAR)
                .withInitialValue(initialSettings.elevationMapping())
                .create(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, fieldLabel("terracraft.create_world.elevation_mapping"),
                        (button, value) -> applySettings(uiState, currentSettings(uiState, null, null, value)));

        Runnable reposition = () -> {
            int x = screen.width / 2 - BUTTON_WIDTH / 2;
            int y = screen.height / 2 - 10;
            floraButton.setPosition(x, y);
            scaleButton.setPosition(x, y + 24);
            elevationButton.setPosition(x, y + 48);
        };

        Runnable refreshVisibility = () -> {
            boolean visible = isPlanetEarthSelected(uiState);
            floraButton.visible = visible;
            scaleButton.visible = visible;
            elevationButton.visible = visible;
            if (visible)
            {
                PlanetEarthSettings settings = PlanetEarthSettings.read(uiState.getSettings());
                floraButton.setValue(settings.floraPlacement());
                scaleButton.setValue(settings.horizontalScale());
                elevationButton.setValue(settings.elevationMapping());
            }
            reposition.run();
        };

        uiState.addListener(ignored -> refreshVisibility.run());
        refreshVisibility.run();

        event.addListener(floraButton);
        event.addListener(scaleButton);
        event.addListener(elevationButton);
    }

    private static Component fieldLabel(String key)
    {
        return Component.translatable(key);
    }

    private static Component floraLabel(FloraPlacementMode mode)
    {
        return Component.translatable(mode == FloraPlacementMode.BIOME
                ? "terracraft.create_world.flora.biome"
                : "terracraft.create_world.flora.historical");
    }

    private static Component elevationLabel(ElevationMappingMode mode)
    {
        return Component.translatable(mode == ElevationMappingMode.LINEAR
                ? "terracraft.create_world.elevation.linear"
                : "terracraft.create_world.elevation.coastal_log");
    }

    private static PlanetEarthSettings currentSettings(
            WorldCreationUiState uiState,
            FloraPlacementMode floraOverride,
            HorizontalScaleOption scaleOverride,
            ElevationMappingMode elevationOverride
    )
    {
        PlanetEarthSettings current = PlanetEarthSettings.read(uiState.getSettings());
        return new PlanetEarthSettings(
                floraOverride != null ? floraOverride : current.floraPlacement(),
                scaleOverride != null ? scaleOverride : current.horizontalScale(),
                elevationOverride != null ? elevationOverride : current.elevationMapping()
        );
    }

    private static void applySettings(WorldCreationUiState uiState, PlanetEarthSettings settings)
    {
        if (!isPlanetEarthSelected(uiState))
        {
            return;
        }

        uiState.updateDimensions((registries, dimensions) ->
                PlanetEarthDimensionsUpdater.apply(settings, dimensions, registries));
    }

    private static boolean isPlanetEarthSelected(WorldCreationUiState uiState)
    {
        return uiState.getWorldType().preset().is(PlanetEarthPresets.PLANET_EARTH);
    }
}
