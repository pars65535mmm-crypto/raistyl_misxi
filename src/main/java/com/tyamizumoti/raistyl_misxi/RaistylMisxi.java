package com.tyamizumoti.raistyl_misxi;

import com.tyamizumoti.raistyl_misxi.client.HaloRenderer;
import com.tyamizumoti.raistyl_misxi.item.HaloItem;
import com.tyamizumoti.raistyl_misxi.item.ChronoIngotItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Rarity;
import com.tyamizumoti.raistyl_misxi.registry.ModSchools;
import com.tyamizumoti.raistyl_misxi.registry.ModSpells;
import net.minecraftforge.fml.ModLoadingContext;  // ← これ！
import com.tyamizumoti.raistyl_misxi.item.IncompleteChronoIngotItem;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;  // ← これ！
import net.minecraftforge.client.event.ModelEvent;
import net.minecraft.resources.ResourceLocation;
import com.tyamizumoti.raistyl_misxi.recipe.ModRecipes;
import net.minecraft.world.inventory.MenuType;
import com.tyamizumoti.raistyl_misxi.screen.AetherFluidWorkbenchMenu;
import com.tyamizumoti.raistyl_misxi.block.entity.AetherFluidWorkbenchBlockEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraft.world.item.Items;
import com.tyamizumoti.raistyl_misxi.block.entity.ModBlockEntities;
import com.tyamizumoti.raistyl_misxi.screen.ModMenuTypes;
import com.tyamizumoti.raistyl_misxi.screen.TimeCollectorScreen;
import com.tyamizumoti.raistyl_misxi.screen.TimeCollectorMenu; // 念のため
import com.tyamizumoti.raistyl_misxi.CraftingPedestalBlockEntity;
import com.tyamizumoti.raistyl_misxi.MasterOutputBlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.world.level.block.LiquidBlock;
import com.tyamizumoti.raistyl_misxi.fluid.ModFluids;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.util.Optional;

@Mod(RaistylMisxi.MODID)
public class RaistylMisxi {
    public static final String MODID = "raistyl_misxi";
    private static final Logger LOGGER = LogManager.getLogger();



    // --- レジストリの準備 ---
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // 🛑 古い「BLOCK_ENTITIES」の二重定義を消し去り、正しい型で1つに統一！
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

// ⭕ これに丸ごと置き換えてくれ！
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> AETHER_FLUID_WORKBENCH = 
            BLOCKS.register("aether_fluid_workbench",
            () -> new com.tyamizumoti.raistyl_misxi.block.AetherFluidWorkbenchBlock(
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK).noOcclusion()
            ));

    // ⭕ アイテム登録（これも念のため安全なやつに直しておいた！）
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> AETHER_FLUID_WORKBENCH_ITEM = 
            ITEMS.register("aether_fluid_workbench",
            () -> new net.minecraft.world.item.BlockItem(AETHER_FLUID_WORKBENCH.get(), new net.minecraft.world.item.Item.Properties()));

    // ⭕ BlockEntityの登録
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<com.tyamizumoti.raistyl_misxi.block.entity.AetherFluidWorkbenchBlockEntity>> AETHER_FLUID_WORKBENCH_BE =
            BLOCK_ENTITIES.register("aether_fluid_workbench", 
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
                    com.tyamizumoti.raistyl_misxi.block.entity.AetherFluidWorkbenchBlockEntity::new, 
                    AETHER_FLUID_WORKBENCH.get()
            ).build(null));

    public static final RegistryObject<Item> INCOMPLETE_CHRONO_INGOT = 
    ITEMS.register("incomplete_chrono_ingot", () -> new IncompleteChronoIngotItem(new Item.Properties()));

    public static final RegistryObject<Item> UPGRALANK = 
    ITEMS.register("upgralank", () -> new Item(new Item.Properties().stacksTo(64))); 
              // --- 既存のアイテム登録 ---
    public static final RegistryObject<Item> ANGEL_HALO = ITEMS.register("angel_halo", 
        () -> new HaloItem(new Item.Properties().stacksTo(1), "民は言った、太陽は神であり天使は我々を守る者だと。"));
        
    public static final RegistryObject<Item> THE_WHITE_HALO = ITEMS.register("the_white_halo", 
        () -> new HaloItem(new Item.Properties().stacksTo(1), "可能性とは希望を作ることである"));

    public static final RegistryObject<Item> RPG936_RMV1 = ITEMS.register("rpg936rmv1",
        () -> new Rpg936Rmv1(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> ANNIHILATION_PAST_DAGGER = ITEMS.register("annihilation_past_dagger",
        () -> new AnnihilationPastDagger(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final RegistryObject<Item> DISASTROVA_SWORD = ITEMS.register("disastrova_sword",
        () -> new com.tyamizumoti.raistyl_misxi.item.DisastrovaSwordItem(
            new Item.Properties().stacksTo(1).fireResistant().rarity(net.minecraft.world.item.Rarity.EPIC)
        ));
    public static final RegistryObject<Item> UPGRADE_BASE = ITEMS.register("upgra",
        () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> UPGRADE_SPEED = ITEMS.register("upgraspped",
        () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CRUSH_WEAPON = ITEMS.register("crush_weapon",
        () -> new com.tyamizumoti.raistyl_misxi.item.CrashWeaponItem(
            new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)
        ));
        public static final RegistryObject<Item> CHRONO_INGOT = 
    ITEMS.register("chrono_ingot", () -> new ChronoIngotItem(new Item.Properties().rarity(Rarity.EPIC)));    public static final RegistryObject<Item> TIME_FLUID_BUCKET = ITEMS.register("time_fluid_bucket",
            () -> new BucketItem(ModFluids.SOURCE_TIME_FLUID, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    // --- 🔥 【新機能】マルチブロック用パーツの本体登録（ここが抜けてた！！） ---
    
    // 🧱 右クリックでアイテムを乗せられるように強化したペデスタル（インポートエラー対策完全版）
    public static final RegistryObject<Block> CRAFTING_PEDESTAL = BLOCKS.register("crafting_pedestal",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)) {
                @Override
                public net.minecraft.world.InteractionResult use(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
                    if (!level.isClientSide()) {
                        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof CraftingPedestalBlockEntity pedestal) {
                            ItemStack handItem = player.getItemInHand(hand);
                            
                            // ペデスタルが空っぽで、プレイヤーがアイテムを持っていたら乗せる
                            if (pedestal.getItem().isEmpty() && !handItem.isEmpty()) {
                                ItemStack toInsert = handItem.copy();
                                toInsert.setCount(1);
                                pedestal.setItem(toInsert);
                                if (!player.isCreative()) handItem.shrink(1);
                                return net.minecraft.world.InteractionResult.SUCCESS;
                            } 
                            // ペデスタルに既にアイテムが乗っていたら、プレイヤーの手元に戻す
                            else if (!pedestal.getItem().isEmpty()) {
                                player.addItem(pedestal.getItem().copy());
                                pedestal.setItem(ItemStack.EMPTY);
                                return net.minecraft.world.InteractionResult.SUCCESS;
                            }
                        }
                    }
                    return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
                }
            });
                public static final RegistryObject<Item> CRAFTING_PEDESTAL_ITEM = ITEMS.register("crafting_pedestal",
            () -> new BlockItem(CRAFTING_PEDESTAL.get(), new Item.Properties()));

    // 🧠 5x5の中央の真後ろに置くマスターコア（バニラの黒曜石をそのまま身代わりにする）
    public static final RegistryObject<Block> MASTER_OUTPUT_CORE = BLOCKS.register("master_output_core",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)));
    public static final RegistryObject<Item> MASTER_OUTPUT_CORE_ITEM = ITEMS.register("master_output_core",
            () -> new BlockItem(MASTER_OUTPUT_CORE.get(), new Item.Properties()));

    // ⚡ 電源供給ポート（バニラの金ブロックをそのまま身代わりにする）
    public static final RegistryObject<Block> POWER_INPUT_PORT = BLOCKS.register("power_input_port",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)));
    public static final RegistryObject<Item> POWER_INPUT_PORT_ITEM = ITEMS.register("power_input_port",
            () -> new BlockItem(POWER_INPUT_PORT.get(), new Item.Properties()));
            


    // ジャンプキーの連打判定用（簡易的なマルチジャンプ用内部フラグ）
    private static boolean lastJumpPressed = false;

public RaistylMisxi() {
    // 1. イベントバスの取得とログ出力（最初にやるべきこと）
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            LOGGER.info("Raistyl_Misxi Mod 初期化開始）");

    // 2. 基本レジストリの登録（重複を排除し、1回ずつに統一）
    BLOCKS.register(modEventBus);
    ITEMS.register(modEventBus);
    BLOCK_ENTITIES.register(modEventBus);

    // 3. カスタムクラス側のレジストリ登録（中身が重複していないか要確認）
    com.tyamizumoti.raistyl_misxi.effect.ModEffects.register(modEventBus);
    ModBlocks.register(modEventBus);
    ModFluids.register(modEventBus);
    ModBlockEntities.register(modEventBus);
    ModMenuTypes.register(modEventBus);
    ModRecipes.register(modEventBus);
    ModCreativeModeTabs.register(modEventBus);

    ModSchools.register(modEventBus);
    ModSpells.register(modEventBus);

    // 4. イベントリスナーの登録
    modEventBus.addListener(this::commonSetup);
    
    if (FMLEnvironment.dist == Dist.CLIENT) {
        modEventBus.addListener(this::clientSetup);
    }
    

    // 5. 鍛冶場（Forge）のグローバルイベントバスへの登録
    MinecraftForge.EVENT_BUS.register(this);
}


    // コンストラクタ（public RaistylMisxi() { ... }）の「外側」にこれを追加
private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
    // ここはとりあえず空っぽでOK！
}

private void clientSetup(final FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
        net.minecraft.client.gui.screens.MenuScreens.register(
            com.tyamizumoti.raistyl_misxi.screen.ModMenuTypes.AETHER_FLUID_WORKBENCH_MENU.get(), 
            com.tyamizumoti.raistyl_misxi.screen.AetherFluidWorkbenchScreen::new
        );
        CuriosRendererRegistry.register(ANGEL_HALO.get(), HaloRenderer::new);
        CuriosRendererRegistry.register(THE_WHITE_HALO.get(), HaloRenderer::new);
    });

    // 🎨 Javaの力技でバニラモデルをそのまま染色する（JSONの追加・変更は一切不要！）
    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
    event.enqueueWork(() -> {
        mc.getBlockColors().register((state, level, pos, tintIndex) -> 0x550077, CRAFTING_PEDESTAL.get());
        mc.getBlockColors().register((state, level, pos, tintIndex) -> 0x00FFDD, MASTER_OUTPUT_CORE.get());
        mc.getBlockColors().register((state, level, pos, tintIndex) -> 0xFF3300, POWER_INPUT_PORT.get());

        // ✅ ここを DistExecutor で保護する！
        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
            net.minecraftforge.api.distmarker.Dist.CLIENT,
            () -> com.tyamizumoti.raistyl_misxi.block.entity.ModBlockEntities::registerRenderers
        );
    });
}

// ==================== ⚡️ ここから性能・イベント処理 ⚡️ ====================

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;

        boolean hasAngelHalo = CuriosApi.getCuriosHelper().findFirstCurio(player, ANGEL_HALO.get()).isPresent();
        boolean hasWhiteHalo = CuriosApi.getCuriosHelper().findFirstCurio(player, THE_WHITE_HALO.get()).isPresent();

        if (hasAngelHalo) {
            if (!player.level().isClientSide()) {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(2.0D)).forEach(target -> {
                    if (target != player) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3, 254, false, false, false));
                    }
                });
            }

            player.getAbilities().mayfly = true;

            if (player.getAbilities().flying) {
                if (!player.level().isClientSide() && player.tickCount % 20 == 0) {
                    player.hurt(player.damageSources().onFire(), 1.0F);
                }

                if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.CLOUD,
                        player.getX(), player.getY() + 0.1, player.getZ(),
                        2, 0.1, 0.1, 0.1, 0.02
                    );
                }
            }
        } else {
            if (!player.isCreative() && !player.isSpectator()) {
                if (player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
            }
        }

        if (hasWhiteHalo && !player.level().isClientSide()) {
            if (player.tickCount % 20 == 0) {
                net.minecraft.world.phys.HitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnViewVector(
                    player, entity -> !entity.isSpectator() && entity.isPickable(), 20.0D
                );

                if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
                    if (entityHit.getEntity() instanceof LivingEntity target && target != player) {
                        
                        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
                        for (double yOffset = 0; yOffset < target.getBbHeight(); yOffset += 0.3) {
                            serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.END_ROD,
                                target.getX(), target.getY() + yOffset, target.getZ(),
                                3, 0.1, 0.0, 0.1, 0.01
                            );
                        }

                        target.hurt(player.damageSources().magic(), 5.0F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!event.getSource().is(DamageTypes.FALL)) return;

        boolean hasAngelHalo = CuriosApi.getCuriosHelper().findFirstCurio(player, ANGEL_HALO.get()).isPresent();

        if (hasAngelHalo) {
            if (event.getAmount() >= player.getHealth()) {
                event.setCanceled(true); 
                player.die(player.damageSources().fall()); 
            } else {
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, ANGEL_HALO.get()).ifPresent(result -> {
            DamageSource source = event.getSource();

            if (source.is(DamageTypes.ON_FIRE)) {
                event.setCanceled(true); 
                
                Component msg = Component.literal(player.getGameProfile().getName() + "は太陽に夢を奪われた");
                player.getServer().getPlayerList().broadcastSystemMessage(msg, false);

                CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
                    handler.getStacksHandler("head").ifPresent(stacksHandler -> {
                        stacksHandler.getStacks().setStackInSlot(0, new ItemStack(THE_WHITE_HALO.get()));
                    });
                });
                
                player.die(source);

            } else if (source.is(DamageTypes.FALL)) {
                event.setCanceled(true);
                
                Component msg = Component.literal(player.getGameProfile().getName() + "は夢と現実を間違えた");
                player.getServer().getPlayerList().broadcastSystemMessage(msg, false);
                
                player.die(source);
            }
        });
    }

    @SubscribeEvent
    public void onLivingAttack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        net.minecraft.world.entity.LivingEntity target = event.getEntity();
        if (target != null && target.getTags().contains("annihilation:quarantined")) {
            target.setInvulnerable(false);
        }
    }

    @Mod.EventBusSubscriber(modid = "raistyl_misxi", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CraftingEventHandler {
        @SubscribeEvent
        public static void onItemCrafted(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
            ItemStack craftedStack = event.getCrafting();
            if (craftedStack.getItem() instanceof Rpg936Rmv1) {
                boolean hasWhiteHalo = false;
                for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
                    ItemStack slotStack = event.getInventory().getItem(i);
                    if (slotStack.getItem() == RaistylMisxi.THE_WHITE_HALO.get()) {
                        hasWhiteHalo = true;
                        break;
                    }
                }
                if (hasWhiteHalo) {
                    net.minecraft.nbt.CompoundTag tag = craftedStack.getOrCreateTag();
                    tag.putBoolean("IsWhiteCollapse", true);
                    craftedStack.setTag(tag);
                }
            }
        }
    }
    @Mod.EventBusSubscriber(modid = "raistyl_misxi", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(
                ModMenuTypes.TIME_COLLECTOR_MENU.get(), TimeCollectorScreen::new
            );
        });
    }
    @SubscribeEvent
        public static void registerItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
            // ⭕ 1の時流流体バケツの色（あのアマランスパープル 0xFF9900FF）を
            // アイテムの「レイヤー1（液体の部分）」に強制的に流し込む呪文
            event.register((stack, tintIndex) -> {
                // tintIndexが1（中身の液体の場所）のときだけ、あの怪しい紫を返す
                return tintIndex == 1 ? 0xFF9900FF : -1;
            }, RaistylMisxi.TIME_FLUID_BUCKET.get());
        }
}

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public static class AttributeModificationHandler {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // 時空魔法の属性を取得（君のMODの属性IDに合わせてね）
        var timePower = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("raistyl_misxi", "time_spell_power"));
        var timeResist = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("raistyl_misxi", "time_magic_resist"));
        
        if (timePower != null && timeResist != null) {
            // すべてのエンティティタイプに追加
            for (var entityType : event.getTypes()) {
                event.add(entityType, timePower);
                event.add(entityType, timeResist);
            }
        } else {
            RaistylMisxi.LOGGER.warn("時空魔法の属性が見つかりませんでした！");
        }
    }
}
}