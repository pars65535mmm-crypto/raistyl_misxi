package com.tyamizumoti.raistyl_misxi.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, "raistyl_misxi");

    public static final RegistryObject<MenuType<TimeCollectorMenu>> TIME_COLLECTOR_MENU =
            MENUS.register("time_collector_menu", () -> IForgeMenuType.create(TimeCollectorMenu::new));
    public static final RegistryObject<MenuType<AetherFluidWorkbenchMenu>> AETHER_FLUID_WORKBENCH_MENU =
            MENUS.register("aether_fluid_workbench_menu", () -> IForgeMenuType.create(AetherFluidWorkbenchMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}