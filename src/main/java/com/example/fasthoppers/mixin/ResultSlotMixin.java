package com.example.fasthoppers.mixin;

import com.example.fasthoppers.BulkCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/** After taking a bulk-crafted item, remove the other 31 from each of the 5 slots (32 total). */
@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {
	@Inject(method = "onTake", at = @At("TAIL"), require = 0)
	private void fasthoppers$bulkConsume(Player player, ItemStack stack, CallbackInfo ci) {
		if (BulkCrafting.isResult(stack)) {
			BulkCrafting.consumeExtra(player.containerMenu);
		}
	}
}
