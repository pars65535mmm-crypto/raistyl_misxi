package com.tyamizumoti.raistyl_misxi;

import com.tyamizumoti.raistyl_misxi.block.entity.TimeCollectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class TimeCollectorBlock extends BaseEntityBlock {
    public TimeCollectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // これ書かないと置いたとき透明ブロックになる
    }

    // 🖱️ 右クリックされたときの処理
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof TimeCollectorBlockEntity timeCollectorEntity) {
                // Forge独自のメソッドでGUI（MenuProvider）を開く（コンテナデータも自動同期される）
                NetworkHooks.openScreen(((ServerPlayer) player), timeCollectorEntity, pos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimeCollectorBlockEntity(pos, state);
    }

    // 毎tickの処理（BlockEntityのtickを呼び出す呪文）
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, 
                com.tyamizumoti.raistyl_misxi.block.entity.ModBlockEntities.TIME_COLLECTOR.get(), 
                TimeCollectorBlockEntity::tick);
    }
}