package com.example.fasthoppers;

/**
 * transfer = max items pushed OUT per cycle (spread over all 5 slots, in slot order)
 * pull     = max items pulled IN from the container above per cycle
 * Vanilla hopper: 1 item every 8 ticks.
 */
public enum HopperTier {
	BETTER(4, 16),
	SUPER(16, 32),
	MEGA(64, 64),
	INSTA(Integer.MAX_VALUE, Integer.MAX_VALUE); // everything, all slots (lag warning!)

	public final int transfer;
	public final int pull;

	HopperTier(int transfer, int pull) {
		this.transfer = transfer;
		this.pull = pull;
	}
}
