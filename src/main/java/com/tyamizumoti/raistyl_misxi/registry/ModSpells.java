package com.tyamizumoti.raistyl_misxi.registry;

import com.tyamizumoti.raistyl_misxi.RaistylMisxi;
import com.tyamizumoti.raistyl_misxi.spell.TemporalStasisSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, RaistylMisxi.MODID);

    public static final RegistryObject<AbstractSpell> TEMPORAL_STASIS =
            SPELLS.register("temporal_stasis", TemporalStasisSpell::new);

    public static void register(IEventBus bus) {
        SPELLS.register(bus);
    }
}