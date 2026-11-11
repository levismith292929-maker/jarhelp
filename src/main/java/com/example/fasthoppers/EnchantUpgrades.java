package com.example.fasthoppers;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
}
