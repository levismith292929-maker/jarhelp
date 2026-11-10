package com.example.fasthoppers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FastHopperLogic {
	/** Same rhythm as a vanilla hopper. */
	private static final int INTERVAL = 8;

	private FastHopperLogic() {
	}

	public static void tick(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopper, HopperTier tier) {
		if (!state.getValue(HopperBlock.ENABLED)) {
			return; // locked by redstone
		}
		// stateless 8-tick cycle, offset per position so hoppers don't all fire on the same tick
		if (Math.floorMod(level.getGameTime() + pos.asLong(), (long) INTERVAL) != 0) {
			return;
		}

		boolean changed = eject(level, pos, state, hopper, tier);
		changed |= suck(level, pos, hopper, tier);
		if (changed) {
			hopper.setChanged();
		}
	}

	/** Push items from this hopper into the container it faces. */
	private static boolean eject(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopper, HopperTier tier) {
		if (hopper.isEmpty()) {
			return false;
		}
		Direction facing = state.getValue(HopperBlock.FACING);
		Container dest = HopperBlockEntity.getContainerAt(level, pos.relative(facing));
		if (dest == null) {
			return false;
		}
		Direction intoFace = facing.getOpposite();

		int budget = tier.transfer;
		int moved = 0;
		for (int slot = 0; slot < hopper.getContainerSize() && budget > 0; slot++) {
			ItemStack inSlot = hopper.getItem(slot);
			if (inSlot.isEmpty()) {
				continue;
			}
			ItemStack taken = hopper.removeItem(slot, Math.min(inSlot.getCount(), budget));
			int takenCount = taken.getCount();
			ItemStack remainder = HopperBlockEntity.addItem(hopper, dest, taken, intoFace);
			giveBack(hopper, slot, remainder);

			int transferred = takenCount - remainder.getCount();
			budget -= transferred;
			moved += transferred;
		}
		if (moved > 0) {
			dest.setChanged();
		}
		return moved > 0;
	}

	/** Pull items from the container above (or item entities above, if there is no container). */
	private static boolean suck(Level level, BlockPos pos, HopperBlockEntity hopper, HopperTier tier) {
		Container src = HopperBlockEntity.getContainerAt(level, pos.above());
		if (src == null) {
			// nothing above: fall back to vanilla behaviour for dropped item entities
			return HopperBlockEntity.suckInItems(level, hopper);
		}

		Direction face = Direction.DOWN;
		int[] slots;
		if (src instanceof WorldlyContainer worldly) {
			slots = worldly.getSlotsForFace(face);
		} else {
			slots = new int[src.getContainerSize()];
			for (int i = 0; i < slots.length; i++) {
				slots[i] = i;
			}
		}

		int budget = tier.pull;
		int moved = 0;
		for (int slot : slots) {
			if (budget <= 0) {
				break;
			}
			ItemStack inSlot = src.getItem(slot);
			if (inSlot.isEmpty()) {
				continue;
			}
			if (src instanceof WorldlyContainer worldly && !worldly.canTakeItemThroughFace(slot, inSlot, face)) {
				continue;
			}
			ItemStack taken = src.removeItem(slot, Math.min(inSlot.getCount(), budget));
			int takenCount = taken.getCount();
			ItemStack remainder = HopperBlockEntity.addItem(src, hopper, taken, null);
			giveBack(src, slot, remainder);

			int transferred = takenCount - remainder.getCount();
			budget -= transferred;
			moved += transferred;
		}
		if (moved > 0) {
			src.setChanged();
		}
		return moved > 0;
	}

	/** Put whatever couldn't be moved back where it came from. */
	private static void giveBack(Container container, int slot, ItemStack remainder) {
		if (remainder.isEmpty()) {
			return;
		}
		ItemStack current = container.getItem(slot);
		if (current.isEmpty()) {
			container.setItem(slot, remainder);
		} else {
			current.grow(remainder.getCount());
		}
	}
}
