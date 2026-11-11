package com.example.fasthoppers.mixin;

import com.example.fasthoppers.EnchantUpgrades;
import java.util.Optional;
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

/** Our custom anvil rules (Magical Tome, Wide Mining books, Disenchanted Books) - all cost 0 XP. */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Shadow
	private DataSlot cost;
	@Shadow
	private int repairItemCountCost;

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void fasthoppers$createResult(CallbackInfo ci) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		Optional<ItemStack> custom = EnchantUpgrades.anvilCustom(menu.getSlot(0).getItem(), menu.getSlot(1).getItem());
		if (custom.isEmpty()) {
			return; // not ours: vanilla handles it
		}
		ItemStack result = custom.get();
		menu.getSlot(2).set(result);
		this.cost.set(0);
		this.repairItemCountCost = result.isEmpty() ? 0 : 1; // use up exactly one of the second slot
		ci.cancel();
	}

	/** Vanilla refuses to hand over a result that costs 0 levels, so allow ours. */
	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true, require = 0)
	private void fasthoppers$mayPickup(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		if (!hasStack || this.cost.get() != 0) {
			return;
		}
		Optional<ItemStack> custom = EnchantUpgrades.anvilCustom(menu.getSlot(0).getItem(), menu.getSlot(1).getItem());
		if (custom.isPresent() && !custom.get().isEmpty()) {
			cir.setReturnValue(true);
		}
	}
}
