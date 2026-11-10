package com.example.fasthoppers.mixin;

import com.example.fasthoppers.EnchantUpgrades;
import com.example.fasthoppers.ModContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

/** Anvil: enchanted book (one max-level enchant) + Magical Tome -> book one level higher, 0 XP. */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Shadow
	private DataSlot cost;
	@Shadow
	private int repairItemCountCost;

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void fasthoppers$createResult(CallbackInfo ci) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		ItemStack left = menu.getSlot(0).getItem();
		ItemStack right = menu.getSlot(1).getItem();
		if (!right.is(ModContent.MAGICAL_TOME)) {
			return;
		}
		ItemStack result = EnchantUpgrades.anvilResult(left);
		if (result.isEmpty()) {
			return; // not a valid upgrade: let vanilla treat it as "no result"
		}
		menu.getSlot(2).set(result);
		this.cost.set(0);
		this.repairItemCountCost = 1; // consume exactly one Tome
		ci.cancel();
	}

	/** Vanilla refuses to hand over a result that costs 0 levels, so allow ours. */
	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true, require = 0)
	private void fasthoppers$mayPickup(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		if (hasStack
				&& menu.getSlot(1).getItem().is(ModContent.MAGICAL_TOME)
				&& !EnchantUpgrades.anvilResult(menu.getSlot(0).getItem()).isEmpty()) {
			cir.setReturnValue(true);
		}
	}
}
