package com.example.fasthoppers.mixin;

import com.example.fasthoppers.BulkCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Crafting table: the 5x32 "plus" recipes (see BulkCrafting).
 * Hooks AbstractContainerMenu (used by every menu), so it doesn't depend on crafting-table method names.
 * (File keeps its old name; the class name doesn't matter to Mixin.)
 */
@Mixin(AbstractContainerMenu.class)
public abstract class CraftingMenuMixin {

	/** Every tick the server syncs the open menu: show our result when the grid matches. */
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

	/**
	 * Clicking our result: we hand over the item and take exactly 32 from each slot ourselves,
	 * and cancel vanilla's click so its "give the ingredients back" behaviour can't run.
	 */
	@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
	private void fasthoppers$clicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
		Object self = this;
		if (!(self instanceof CraftingMenu) || slotId != 0) {
			return;
		}
		AbstractContainerMenu menu = (AbstractContainerMenu) self;
		ItemStack result = menu.getSlot(0).getItem();
		if (!BulkCrafting.isResult(result)) {
			return; // a normal vanilla recipe: leave it alone
		}
		ci.cancel(); // from here on, vanilla never sees clicks on our result
		if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
			return;
		}
		if (BulkCrafting.match(menu).isEmpty()) {
			return; // stale result, nothing to craft
		}
		ItemStack out = result.copy();
		if (clickType == ClickType.PICKUP) {
			ItemStack carried = menu.getCarried();
			if (carried.isEmpty()) {
				menu.setCarried(out);
			} else if (ItemStack.isSameItemSameComponents(carried, out)
					&& carried.getCount() + out.getCount() <= carried.getMaxStackSize()) {
				carried.grow(out.getCount());
			} else {
				return; // hands full of something else
			}
		} else if (!player.getInventory().add(out)) {
			return; // inventory full
		}
		BulkCrafting.consumeAll(menu);
		menu.getSlot(0).set(BulkCrafting.match(menu)); // offer another one if there's still enough
	}
}
