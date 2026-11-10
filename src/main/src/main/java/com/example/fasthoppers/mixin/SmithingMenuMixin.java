package com.example.fasthoppers.mixin;

import com.example.fasthoppers.EnchantUpgrades;
import com.example.fasthoppers.ModContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Smithing table: Enchanted Forge (template) + item (base) + upgraded book (addition)
 * -> the item with that enchantment one level higher.
 * Slots: 0 template, 1 base, 2 addition, 3 result.
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {
	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void fasthoppers$createResult(CallbackInfo ci) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		if (!menu.getSlot(0).getItem().is(ModContent.ENCHANTED_FORGE)) {
			return;
		}
		// With a Forge in the template slot, WE decide the result (empty if the combination is invalid)
		ItemStack result = EnchantUpgrades.smithingResult(menu.getSlot(1).getItem(), menu.getSlot(2).getItem());
		menu.getSlot(3).set(result);
		ci.cancel();
	}

	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void fasthoppers$mayPickup(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		if (hasStack
				&& menu.getSlot(0).getItem().is(ModContent.ENCHANTED_FORGE)
				&& !menu.getSlot(3).getItem().isEmpty()) {
			cir.setReturnValue(true);
		}
	}
}
