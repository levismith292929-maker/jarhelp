package com.example.fasthoppers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

public class FastHoppers implements ModInitializer {
	public static final String MOD_ID = "fasthoppers";

	/** The running (integrated or dedicated) server, or null. */
	public static volatile MinecraftServer SERVER;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModContent.initialize();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);
		WideMining.register();
	}
}
