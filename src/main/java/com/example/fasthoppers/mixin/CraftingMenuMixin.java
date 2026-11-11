package com.example.fasthoppers.mixin;

import com.example.fasthoppers.BulkCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

/** Crafting table: recognise the 5x32 "plus" recipes (see BulkCrafting). */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
	@Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true, require = 0)
	private void fasthoppers$slotsChanged(Container container, CallbackInfo ci) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		ItemStack result = BulkCrafting.match(menu);
		if (result.isEmpty()) {
			return; // not ours: vanilla recipes as normal
		}
		menu.getSlot(0).set(result);
		ci.cancel();
	}
}
