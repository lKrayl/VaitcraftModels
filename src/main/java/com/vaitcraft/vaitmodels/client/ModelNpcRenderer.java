package com.vaitcraft.vaitmodels.client;

import com.vaitcraft.vaitmodels.entity.ModelNpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ModelNpcRenderer extends GeoEntityRenderer<ModelNpcEntity> {
    public ModelNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelNpcModel());
        this.shadowRadius = 0.4F;
    }
}
