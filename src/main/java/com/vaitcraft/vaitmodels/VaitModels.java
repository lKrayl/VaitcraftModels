package com.vaitcraft.vaitmodels;

import com.vaitcraft.vaitmodels.registry.ModEntities;
import com.vaitcraft.vaitmodels.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clase principal (lado comun: cliente + servidor). */
@Mod(VaitModels.MODID)
public class VaitModels {
    public static final String MODID = "vaitmodels";

    public VaitModels(IEventBus modEventBus) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModItems::addToCreativeTab);
    }
}
