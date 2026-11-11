package com.example.fasthoppers.mixin;

import com.example.fasthoppers.BulkCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Crafting table: recognise the 5x32 "plus" recipes (see BulkCrafting).
 * Hooks AbstractContainerMenu.broadcastChanges(), which the server runs every tick for the
 * menu a player has open, so it doesn't depend on any crafting-table-specific method name.
 * (File keeps its old name; the class name doesn't matter to Mixin.)
 */
@Mixin(AbstractContainerMenu.class)
public abstract class CraftingMenuMixin {
	@Inject(method = "broadcastChanges", at = @At("HEAD"))
	private void fasthoppers$bulkCraft(CallbackInfo ci) {
		Object self = this;
		if (!(self instanceof CraftingMenu)) {
			return;
		}
		AbstractContainerMenu menu = (AbstractContainerMenu) self;
		ItemStack match = BulkCrafting.match(menu);
		ItemStack current = menu.getSlot(0).getItem();
		if (!match.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(current, match) || current.getCount() != match.getCount()) {
				menu.getSlot(0).set(match);
			}
		} else if (BulkCrafting.isResult(current)) {
			menu.getSlot(0).set(ItemStack.EMPTY); // grid no longer matches: clear our stale result
		}
	}
}
