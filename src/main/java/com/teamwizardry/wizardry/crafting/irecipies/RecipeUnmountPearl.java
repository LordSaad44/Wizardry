package com.teamwizardry.wizardry.crafting.irecipies;

import com.teamwizardry.wizardry.api.spell.SpellUtils;
import com.teamwizardry.wizardry.init.ModItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

/**
 * Created by Demoniaque on 8/30/2016.
 */
public class RecipeUnmountPearl extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
		boolean foundStaff = false;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() == ModItems.STAFF) {

				if (stack.getItemDamage() == 1) {
					if (foundStaff) return false;
					foundStaff = true;
				}
			}
		}
		return foundStaff;
	}

	@Override
	@Nonnull
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		ItemStack foundStaff = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() == ModItems.STAFF) {

				if (stack.getItemDamage() == 1) {
					foundStaff = stack;
					break;
				}
			}
		}

		if (foundStaff.isEmpty()) return ItemStack.EMPTY;

		ItemStack infusedPearl = new ItemStack(ModItems.PEARL_NACRE);
		SpellUtils.copySpell(foundStaff, infusedPearl);

		return infusedPearl;
	}

	@Override
	public boolean canFit(int width, int height) {
		return true;
	}

	@Override
	@Nonnull
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
		NonNullList<ItemStack> remainingItems = ForgeHooks.defaultRecipeGetRemainingItems(inv);

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() == ModItems.STAFF) {
				remainingItems.set(i, new ItemStack(ModItems.STAFF));
			}
		}

		return remainingItems;
	}
}
