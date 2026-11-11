package com.example.fasthoppers;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * "Skyblock style" crafting: put the ingredient in a plus shape on the crafting table
 * (top, left, middle, right, bottom), at least 32 in each of the 5 slots = 160 total.
 * Crafting slots in a CraftingMenu: 0 = result, 1..9 = grid (row by row).
 */
public final class BulkCrafting {
	public static final int PER_SLOT = 32;
	private static final int[] PLUS = {2, 4, 5, 6, 8};
	private static final int[] CORNERS = {1, 3, 7, 9};

	private BulkCrafting() {
	}

	private static Item[][] recipes() {
		// { ingredient, result }
		return new Item[][] {
				{Items.LAPIS_LAZULI, ModContent.ENCHANTED_LAPIS},
				{ModContent.ENCHANTED_LAPIS, ModContent.ENCHANTED_LAPIS_BLOCK},
				{ModContent.ENCHANTED_LAPIS_BLOCK, ModContent.ABSOLUTE_LAPIS_BLOCK},
				{ModContent.ABSOLUTE_LAPIS_BLOCK, ModContent.TESSELLATED_LAPIS_BLOCK}
		};
	}

	/** The result for what's currently on the crafting grid, or EMPTY if it isn't one of our recipes. */
	public static ItemStack match(AbstractContainerMenu menu) {
		for (int corner : CORNERS) {
			if (!menu.getSlot(corner).getItem().isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		for (Item[] recipe : recipes()) {
			boolean ok = true;
			for (int slot : PLUS) {
				ItemStack stack = menu.getSlot(slot).getItem();
				if (!stack.is(recipe[0]) || stack.getCount() < PER_SLOT) {
					ok = false;
					break;
				}
			}
			if (ok) {
				return new ItemStack(recipe[1]);
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean isResult(ItemStack stack) {
		for (Item[] recipe : recipes()) {
			if (stack.is(recipe[1])) {
				return true;
			}
		}
		return false;
	}

	/** Vanilla already took 1 from each slot; take the other 31 so each slot loses 32 in total. */
	public static void consumeExtra(AbstractContainerMenu menu) {
		for (int slot : PLUS) {
			Slot s = menu.getSlot(slot);
			ItemStack stack = s.getItem();
			if (!stack.isEmpty()) {
				s.remove(Math.min(PER_SLOT - 1, stack.getCount()));
			}
		}
	}

	/** Take the full 32 from each of the 5 plus-shaped slots (we handle the whole craft ourselves). */
	public static void consumeAll(AbstractContainerMenu menu) {
		for (int slot : PLUS) {
			Slot s = menu.getSlot(slot);
			ItemStack stack = s.getItem();
			if (!stack.isEmpty()) {
				s.remove(Math.min(PER_SLOT, stack.getCount()));
			}
		}
	}
}
