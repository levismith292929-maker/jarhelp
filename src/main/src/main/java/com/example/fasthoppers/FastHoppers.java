package com.example.fasthoppers;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class FastHoppers implements ModInitializer {
	public static final String MOD_ID = "fasthoppers";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModContent.initialize();
	}
}
