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

public final class ModContent {
	/** Sugar model (see assets/.../items/hopper_core.json) + enchantment glint. */
	public static final Item HOPPER_CORE = registerCore("hopper_core");

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
		BlockEntityType.HOPPER.addSupportedBlock(block);
		return block;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(tab -> {
			tab.accept(HOPPER_CORE);
			tab.accept(BETTER_HOPPER.asItem());
			tab.accept(SUPER_HOPPER.asItem());
			tab.accept(MEGA_HOPPER.asItem());
			tab.accept(INSTA_HOPPER.asItem());
		});
	}
}
