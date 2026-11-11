package com.example.fasthoppers;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Wide Mining: breaking a block with a Wide Mining pickaxe also breaks a cube that starts at the
 * face you hit and extends INTO the block (the mined block is the centre of the cube's near face).
 * Level I = 3x3x3, II = 4x4x4, III = 6x6x6, IV = 8x8x8.
 */
public final class WideMining {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final ResourceKey<Enchantment> WIDE_MINING =
			ResourceKey.create(Registries.ENCHANTMENT, FastHoppers.id("wide_mining"));

	private static final int[] SIZES = {0, 3, 4, 6, 8};
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

	/** Wide Mining level on a tool (read straight from the item, no registry lookup needed). */
	private static int levelOn(ItemStack tool) {
		int level = 0;
		for (var entry : tool.getEnchantments().entrySet()) {
			if (entry.getKey().is(WIDE_MINING)) {
				level = entry.getIntValue();
			}
		}
		return level;
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (active || !(player instanceof ServerPlayer serverPlayer)) {
				return;
			}
			int enchLevel = levelOn(player.getMainHandItem());
			if (enchLevel <= 0) {
				return;
			}
			int size = SIZES[Math.min(enchLevel, SIZES.length - 1)];
			Direction face = hitFace(player, pos);
			LOGGER.info("[fasthoppers] Wide Mining level {} ({}x{}x{}), face {}, at {}", enchLevel, size, size, size, face, pos);
			active = true;
			try {
				mineCube(serverPlayer, level, pos, face, size);
			} finally {
				active = false;
			}
		});
	}

	/** Which face of the (already broken) block was the player looking at? */
	private static Direction hitFace(Player player, BlockPos pos) {
		Vec3 from = player.getEyePosition();
		Vec3 to = from.add(player.getViewVector(1.0F).scale(8.0));
		BlockHitResult hit = AABB.clip(List.of(new AABB(pos)), from, to, pos);
		if (hit != null) {
			return hit.getDirection();
		}
		return player.getDirection().getOpposite(); // fallback
	}

	private static void mineCube(ServerPlayer player, Level level, BlockPos origin, Direction face, int size) {
		Direction inward = face.getOpposite();
		Direction.Axis[] lateral = laterals(face.getAxis());
		int lo = -((size - 1) / 2);
		int hi = lo + size - 1;
		int broken = 0;

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
					BlockState targetState = level.getBlockState(target);
					if (targetState.isAir()
							|| targetState.getDestroySpeed(level, target) < 0
							|| !targetState.is(BlockTags.MINEABLE_WITH_PICKAXE)
							|| !tool.isCorrectToolForDrops(targetState)) {
						continue;
					}
					if (player.gameMode.destroyBlock(target)) {
						broken++;
					}
				}
			}
		}
		LOGGER.info("[fasthoppers] Wide Mining broke {} extra blocks", broken);
	}

	private static Direction.Axis[] laterals(Direction.Axis axis) {
		return switch (axis) {
			case X -> new Direction.Axis[] {Direction.Axis.Y, Direction.Axis.Z};
			case Y -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z};
			case Z -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Y};
		};
	}
}
