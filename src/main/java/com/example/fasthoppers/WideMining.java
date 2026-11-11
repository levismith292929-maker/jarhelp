package com.example.fasthoppers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Wide Mining: breaking a block with a Wide Mining pickaxe also breaks a cube that starts at the
 * face you hit and extends INTO the block (the mined block is the centre of the cube's near face).
 * Level I = 3x3x3, II = 4x4x4, III = 6x6x6, IV = 8x8x8.
 */
public final class WideMining {
	public static final ResourceKey<Enchantment> WIDE_MINING =
			ResourceKey.create(Registries.ENCHANTMENT, FastHoppers.id("wide_mining"));

	private static final int[] SIZES = {0, 3, 4, 6, 8};
	private static final Map<UUID, Direction> FACES = new HashMap<>();
	private static boolean active = false; // stops our own breaking from triggering more Wide Mining

	private WideMining() {
	}

	public static Holder<Enchantment> holder() {
		MinecraftServer server = FastHoppers.SERVER;
		if (server == null) {
			return null;
		}
		return server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(WIDE_MINING);
	}

	public static void register() {
		// BEFORE the block breaks, remember which face the player is hitting
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (!active && player instanceof ServerPlayer) {
				HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
				if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
					FACES.put(player.getUUID(), blockHit.getDirection());
				}
			}
			return true;
		});

		// AFTER it broke, break the rest of the cube
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (active || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel)) {
				return;
			}
			Direction face = FACES.remove(player.getUUID());
			if (face == null) {
				return;
			}
			Holder<Enchantment> wide = holder();
			if (wide == null) {
				return;
			}
			int enchLevel = EnchantmentHelper.getItemEnchantmentLevel(wide, player.getMainHandItem());
			if (enchLevel <= 0) {
				return;
			}
			int size = SIZES[Math.min(enchLevel, SIZES.length - 1)];
			active = true;
			try {
				mineCube(serverPlayer, level, pos, face, size);
			} finally {
				active = false;
			}
		});
	}

	private static void mineCube(ServerPlayer player, net.minecraft.world.level.Level level, BlockPos origin, Direction face, int size) {
		Direction inward = face.getOpposite();
		Direction.Axis[] lateral = laterals(face.getAxis());
		int lo = -((size - 1) / 2);
		int hi = lo + size - 1;

		for (int depth = 0; depth < size; depth++) {
			for (int i = lo; i <= hi; i++) {
				for (int j = lo; j <= hi; j++) {
					if (depth == 0 && i == 0 && j == 0) {
						continue; // the block the player already broke
					}
					int[] v = {origin.getX(), origin.getY(), origin.getZ()};
					v[0] += inward.getStepX() * depth;
					v[1] += inward.getStepY() * depth;
					v[2] += inward.getStepZ() * depth;
					v[lateral[0].ordinal()] += i;
					v[lateral[1].ordinal()] += j;
					BlockPos target = new BlockPos(v[0], v[1], v[2]);

					ItemStack tool = player.getMainHandItem();
					if (tool.isEmpty()) {
						return; // tool broke
					}
					BlockState target_state = level.getBlockState(target);
					if (target_state.isAir()
							|| target_state.getDestroySpeed(level, target) < 0
							|| !target_state.is(BlockTags.MINEABLE_WITH_PICKAXE)
							|| !tool.isCorrectToolForDrops(target_state)) {
						continue;
					}
					player.gameMode.destroyBlock(target);
				}
			}
		}
	}

	private static Direction.Axis[] laterals(Direction.Axis axis) {
		return switch (axis) {
			case X -> new Direction.Axis[] {Direction.Axis.Y, Direction.Axis.Z};
			case Y -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z};
			case Z -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Y};
		};
	}
}
