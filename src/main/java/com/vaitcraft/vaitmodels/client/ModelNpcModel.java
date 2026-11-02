package com.vaitcraft.vaitmodels.client;

import com.vaitcraft.vaitmodels.entity.ModelNpcEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/**
 * Resuelve las rutas segun el ModelId de cada entidad:
 *   assets/<ns>/geo/entity/<id>.geo.json
 *   assets/<ns>/animations/entity/<id>.animation.json
 *   assets/<ns>/textures/entity/<id>.png
 */
public class ModelNpcModel extends GeoModel<ModelNpcEntity> {
    public static final String HEAD_BONE = "head";

    @Override
    public ResourceLocation getModelResource(ModelNpcEntity npc) {
        return asset(npc, "geo/entity/%s.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ModelNpcEntity npc) {
        return asset(npc, "textures/entity/%s.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ModelNpcEntity npc) {
        return asset(npc, "animations/entity/%s.animation.json");
    }

    private static ResourceLocation asset(ModelNpcEntity npc, String pattern) {
        ResourceLocation id = npc.getModelLocation();
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), pattern.formatted(id.getPath()));
    }

    /** Hace que el hueso "head" siga hacia donde mira la entidad (te mira a ti gracias a LookAtPlayerGoal). */
    @Override
    public void setCustomAnimations(ModelNpcEntity animatable, long instanceId, AnimationState<ModelNpcEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone(HEAD_BONE);
        if (head == null) {
            return; // el modelo no tiene hueso "head": simplemente no gira la cabeza
        }
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(data.headPitch() * Mth.DEG_TO_RAD);
        head.setRotY(data.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
