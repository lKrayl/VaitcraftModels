package com.vaitcraft.vaitmodels.registry;

import com.vaitcraft.vaitmodels.VaitModels;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VaitModels.MODID);

    /** Huevo de spawn: genera el NPC con el modelo por defecto ("example"). */
    public static final DeferredHolder<Item, DeferredSpawnEggItem> NPC_SPAWN_EGG =
            ITEMS.register("npc_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModEntities.NPC, 0x3B4252, 0xEBCB8B, new Item.Properties()));

    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(NPC_SPAWN_EGG.get());
        }
    }

    private ModItems() {}
}
