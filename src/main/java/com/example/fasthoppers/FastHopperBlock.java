package com.example.fasthoppers;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** A vanilla hopper (same block entity, same GUI) with its own transfer logic. */
public class FastHopperBlock extends HopperBlock {
	private final HopperTier tier;

	public FastHopperBlock(Properties properties, HopperTier tier) {
		super(properties);
		this.tier = tier;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return createTickerHelper(type, BlockEntityType.HOPPER,
				(lvl, pos, st, hopper) -> FastHopperLogic.tick(lvl, pos, st, hopper, this.tier));
	}
}
