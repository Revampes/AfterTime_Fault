package com.aftertime.ratallofyou.modules.SkyBlock.StorageOverview;

import net.minecraftforge.common.MinecraftForge;

public class StorageOverviewModule {
    private final StorageOverviewData data = new StorageOverviewData();

    public StorageOverviewModule() {
        // Load saved state
        data.loadStorages();
        // Register data listener and renderer
        MinecraftForge.EVENT_BUS.register(data);
        StorageOverviewRender.registerEvents(data);
    }
}

