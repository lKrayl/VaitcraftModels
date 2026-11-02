package com.vaitcraft.vaitmodels.client;

import com.vaitcraft.vaitmodels.VaitModels;
import com.vaitcraft.vaitmodels.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Solo se carga en el cliente: aqui van los renderers. */
@Mod(value = VaitModels.MODID, dist = Dist.CLIENT)
public class VaitModelsClient {
    public VaitModelsClient(IEventBus modEventBus) {
        modEventBus.addListener(VaitModelsClient::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NPC.get(), ModelNpcRenderer::new);
    }
}
