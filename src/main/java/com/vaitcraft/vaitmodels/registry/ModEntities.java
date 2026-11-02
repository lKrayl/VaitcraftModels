package com.vaitcraft.vaitmodels.registry;

import com.vaitcraft.vaitmodels.VaitModels;
import com.vaitcraft.vaitmodels.entity.ModelNpcEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, VaitModels.MODID);

    /** Entidad unica: el modelo que muestra depende de su dato "ModelId". */
    public static final DeferredHolder<EntityType<?>, EntityType<ModelNpcEntity>> NPC =
            ENTITY_TYPES.register("npc", () -> EntityType.Builder
                    .of(ModelNpcEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("npc"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(NPC.get(), ModelNpcEntity.createAttributes().build());
    }

    private ModEntities() {}
}
