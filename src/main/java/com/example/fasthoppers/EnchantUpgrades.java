package com.example.fasthoppers;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** The rules of the Magical Tome / Enchanted Forge system. */
public final class EnchantUpgrades {
	/** How far above the vanilla max level an enchantment can be pushed. */
	public static final int EXTRA_LEVELS = 10;

	private EnchantUpgrades() {
	}

	/** The single (enchantment, level) stored on an enchanted book, or null if it isn't exactly one. */
	private static Holder<Enchantment> singleEnchantment(ItemStack book) {
		if (!book.is(Items.ENCHANTED_BOOK)) {
			return null;
		}
		ItemEnchantments stored = book.get(DataComponents.STORED_ENCHANTMENTS);
		if (stored == null || stored.size() != 1) {
			return null;
		}
		for (var entry : stored.entrySet()) {
			return entry.getKey();
		}
		return null;
	}

	private static int storedLevel(ItemStack book, Holder<Enchantment> enchantment) {
		ItemEnchantments stored = book.get(DataComponents.STORED_ENCHANTMENTS);
		return stored == null ? 0 : stored.getLevel(enchantment);
	}

	/**
	 * ANVIL step: a book with ONE enchantment at (or above) its vanilla max level + a Magical Tome
	 * -> the same book, one level higher (up to vanilla max + 10). Returns EMPTY if it doesn't apply.
	 */
	public static ItemStack anvilResult(ItemStack book) {
		Holder<Enchantment> enchantment = singleEnchantment(book);
		if (enchantment == null) {
			return ItemStack.EMPTY;
		}
		int max = enchantment.value().getMaxLevel();
		int level = storedLevel(book, enchantment);
		if (level < max || level >= max + EXTRA_LEVELS) {
			return ItemStack.EMPTY;
		}
		ItemStack result = book.copy();
		result.setCount(1);
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(enchantment, level + 1);
		result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
		return result;
	}

	/**
	 * SMITHING step: item that already has the enchantment, + a single-enchantment book that is exactly
	 * ONE level higher (and above vanilla max) -> the item with that enchantment raised one level.
	 * No skipping. Returns EMPTY if it doesn't apply.
	 */
	public static ItemStack smithingResult(ItemStack base, ItemStack book) {
		if (base.isEmpty()) {
			return ItemStack.EMPTY;
		}
		Holder<Enchantment> enchantment = singleEnchantment(book);
		if (enchantment == null) {
			return ItemStack.EMPTY;
		}
		int max = enchantment.value().getMaxLevel();
		int bookLevel = storedLevel(book, enchantment);
		if (bookLevel <= max || bookLevel > max + EXTRA_LEVELS) {
			return ItemStack.EMPTY;
		}
		int baseLevel = base.getEnchantments().getLevel(enchantment);
		if (baseLevel != bookLevel - 1) {
			return ItemStack.EMPTY; // item must already be exactly one level below the book
		}
		ItemStack result = base.copy();
		result.setCount(1);
		EnchantmentHelper.updateEnchantments(result, mutable -> mutable.set(enchantment, bookLevel));
		return result;
	}

	/**
	 * SMITHING step: Forge + a book carrying Fortune 8+, Efficiency 10+, Mending and Unbreaking 8+
	 * + a Diamond Block -> a book of Wide Mining I. Returns EMPTY if it doesn't apply.
	 */
	public static ItemStack wideMiningBook(ItemStack book) {
		if (!book.is(Items.ENCHANTED_BOOK)) {
			return ItemStack.EMPTY;
		}
		ItemEnchantments stored = book.get(DataComponents.STORED_ENCHANTMENTS);
		if (stored == null) {
			return ItemStack.EMPTY;
		}
		int fortune = 0, efficiency = 0, mending = 0, unbreaking = 0;
		for (var entry : stored.entrySet()) {
			Holder<Enchantment> holder = entry.getKey();
			int level = entry.getIntValue();
			if (holder.is(Enchantments.FORTUNE)) fortune = level;
			else if (holder.is(Enchantments.EFFICIENCY)) efficiency = level;
			else if (holder.is(Enchantments.MENDING)) mending = level;
			else if (holder.is(Enchantments.UNBREAKING)) unbreaking = level;
		}
		if (fortune < 8 || efficiency < 10 || mending < 1 || unbreaking < 8) {
			return ItemStack.EMPTY;
		}
		Holder<Enchantment> wide = WideMining.holder();
		if (wide == null) {
			return ItemStack.EMPTY;
		}
		ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(wide, 1);
		result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
		return result;
	}

	// ---------------- Wide Mining anvil rules ----------------

	/** Level of Wide Mining stored on a single-enchantment book (0 if none). */
	private static int storedWide(ItemStack stack) {
		ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
		if (stored == null || stored.size() != 1) {
			return 0;
		}
		for (var entry : stored.entrySet()) {
			if (entry.getKey().is(WideMining.WIDE_MINING)) {
				return entry.getIntValue();
			}
		}
		return 0;
	}

	/** Level of Wide Mining on a tool (0 if none). */
	private static int toolWide(ItemStack stack) {
		for (var entry : stack.getEnchantments().entrySet()) {
			if (entry.getKey().is(WideMining.WIDE_MINING)) {
				return entry.getIntValue();
			}
		}
		return 0;
	}

	private static ItemStack bookWith(Item item, int level) {
		Holder<Enchantment> wide = WideMining.holder();
		if (wide == null) {
			return ItemStack.EMPTY;
		}
		ItemStack result = new ItemStack(item);
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(wide, level);
		result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
		return result;
	}

	private static Item lapisFor(int level) {
		return switch (level) {
			case 2 -> ModContent.ENCHANTED_LAPIS_BLOCK;
			case 3 -> ModContent.ABSOLUTE_LAPIS_BLOCK;
			case 4 -> ModContent.TESSELLATED_LAPIS_BLOCK;
			default -> Items.AIR;
		};
	}

	/**
	 * All our custom anvil rules in one place.
	 * Optional.empty()        = not one of our rules, let vanilla handle it
	 * Optional.of(EMPTY)      = our rules apply but this combination is not allowed (no result)
	 * Optional.of(someStack)  = the result (always costs 0 XP, uses up the second slot by 1)
	 */
	public static Optional<ItemStack> anvilCustom(ItemStack left, ItemStack right) {
		if (left.isEmpty() || right.isEmpty()) {
			return Optional.empty();
		}

		// Magical Tome: raise a max-level single-enchant book by one level
		if (right.is(ModContent.MAGICAL_TOME)) {
			ItemStack result = anvilResult(left);
			return result.isEmpty() ? Optional.empty() : Optional.of(result);
		}

		// Disenchanted Book + the matching lapis block -> Wide Mining book of that level
		if (left.is(ModContent.DISENCHANTED_BOOK)) {
			int level = storedWide(left);
			if (level >= 2 && level <= 4 && right.is(lapisFor(level))) {
				return Optional.of(bookWith(Items.ENCHANTED_BOOK, level));
			}
			return Optional.of(ItemStack.EMPTY);
		}
		if (right.is(ModContent.DISENCHANTED_BOOK)) {
			return Optional.of(ItemStack.EMPTY); // can't be applied to anything directly
		}

		boolean leftBook = left.is(Items.ENCHANTED_BOOK);
		boolean rightBook = right.is(Items.ENCHANTED_BOOK);
		int leftWide = leftBook ? storedWide(left) : toolWide(left);
		int rightWide = rightBook ? storedWide(right) : toolWide(right);

		// Two Wide Mining books of the same level -> Disenchanted Book one level higher
		if (leftBook && rightBook && leftWide > 0 && rightWide > 0) {
			if (leftWide == rightWide && leftWide < 4) {
				return Optional.of(bookWith(ModContent.DISENCHANTED_BOOK, leftWide + 1));
			}
			return Optional.of(ItemStack.EMPTY);
		}
		// A Wide Mining book may not "level up" a tool that already has that level or higher
		if (rightBook && rightWide > 0 && !leftBook && leftWide >= rightWide) {
			return Optional.of(ItemStack.EMPTY);
		}
		// Two tools with Wide Mining can't be merged either
		if (!leftBook && !rightBook && leftWide > 0 && rightWide > 0) {
			return Optional.of(ItemStack.EMPTY);
		}
		return Optional.empty();
	}
}
