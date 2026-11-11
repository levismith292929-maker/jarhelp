package com.example.fasthoppers;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public final class ModContent {
	/** Sugar model (see assets/.../items/hopper_core.json) + enchantment glint. */
	public static final Item HOPPER_CORE = registerCore("hopper_core");

	/** Anvil upgrader. Looks like an enchanted book (see items/magical_tome.json). */
	public static final Item MAGICAL_TOME = registerTome("magical_tome");
	/** Smithing-table template. Looks like the netherite upgrade template + glint. */
	public static final Item ENCHANTED_FORGE = registerForge("enchanted_forge");

	public static final Item ENCHANTED_LAPIS = registerGlint("enchanted_lapis", Rarity.UNCOMMON);
	public static final Item ENCHANTED_LAPIS_BLOCK = registerGlint("enchanted_lapis_block", Rarity.RARE);
	public static final Item ABSOLUTE_LAPIS_BLOCK = registerGlint("absolute_lapis_block", Rarity.EPIC);
	public static final Item TESSELLATED_LAPIS_BLOCK = registerGlint("tessellated_lapis_block", Rarity.EPIC);
	/** Holds a (stored) Wide Mining level; re-enchant it with the matching lapis block in an anvil. */
	public static final Item DISENCHANTED_BOOK = registerDisenchanted("disenchanted_book");

	public static final Block BETTER_HOPPER = registerHopper("better_hopper", HopperTier.BETTER);
	public static final Block SUPER_HOPPER = registerHopper("super_hopper", HopperTier.SUPER);
	public static final Block MEGA_HOPPER = registerHopper("mega_hopper", HopperTier.MEGA);
	public static final Block INSTA_HOPPER = registerHopper("insta_hopper", HopperTier.INSTA);

	private ModContent() {
	}

	private static Item registerCore(String name) {
		Identifier id = FastHoppers.id(name);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		Item.Properties props = new Item.Properties()
				.setId(key)
				.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(props));
	}

	private static Item registerTome(String name) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, FastHoppers.id(name));
		Item.Properties props = new Item.Properties()
				.setId(key)
				.rarity(Rarity.EPIC)
				.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
				.component(DataComponents.LORE, new ItemLore(List.of(
						lore("Inked in a hand that is not quite human,"),
						lore("its pages whisper of levels no mortal"),
						lore("was ever meant to climb."),
						lore("Press it to a tome at its limit,"),
						lore("and the ancient letters will grow one rune stronger."))));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(props));
	}

	private static Component lore(String text) {
		return Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
	}

	private static Item registerGlint(String name, Rarity rarity) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, FastHoppers.id(name));
		Item.Properties props = new Item.Properties()
				.setId(key)
				.rarity(rarity)
				.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(props));
	}

	private static Item registerDisenchanted(String name) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, FastHoppers.id(name));
		Item.Properties props = new Item.Properties()
				.setId(key)
				.stacksTo(1)
				.rarity(Rarity.RARE)
				.component(DataComponents.LORE, new ItemLore(List.of(
						lore("The magic has drained from its pages."),
						lore("Re-enchant it in an anvil with a lapis block:"),
						lore("Enchanted (II), Absolute (III), Tessellated (IV)."))));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(props));
	}

	private static Item registerForge(String name) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, FastHoppers.id(name));
		Item.Properties props = new Item.Properties()
				.setId(key)
				.rarity(Rarity.EPIC)
				.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(props));
	}

	private static Block registerHopper(String name, HopperTier tier) {
		Identifier id = FastHoppers.id(name);

		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
		Block block = new FastHopperBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER).setId(blockKey), tier);
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Item.Properties itemProps = new Item.Properties()
				.useBlockDescriptionPrefix()
				.setId(itemKey)
				.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, itemProps));

		// These blocks use the vanilla hopper block entity, so it has to accept them
		// (otherwise they'd lose their inventory on chunk load).
		new HopperBlockEntity(BlockPos.ZERO, Blocks.HOPPER.defaultBlockState()).getType().addValidBlock(block);
		return block;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> {
			tab.accept(MAGICAL_TOME);
			tab.accept(ENCHANTED_FORGE);
			tab.accept(ENCHANTED_LAPIS);
			tab.accept(ENCHANTED_LAPIS_BLOCK);
			tab.accept(ABSOLUTE_LAPIS_BLOCK);
			tab.accept(TESSELLATED_LAPIS_BLOCK);
			tab.accept(DISENCHANTED_BOOK);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(tab -> {
			tab.accept(HOPPER_CORE);
			tab.accept(BETTER_HOPPER.asItem());
			tab.accept(SUPER_HOPPER.asItem());
			tab.accept(MEGA_HOPPER.asItem());
			tab.accept(INSTA_HOPPER.asItem());
		});
	}
}
