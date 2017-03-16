package com.teamwizardry.wizardry.common.block.wisdomwood;

import com.teamwizardry.librarianlib.common.base.ModCreativeTab;
import com.teamwizardry.librarianlib.common.base.block.BlockModTrapdoor;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.init.ModBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * Created by LordSaad.
 */
public class BlockWisdomWoodTrapdoor extends BlockModTrapdoor {

	public BlockWisdomWoodTrapdoor() {
		super("wisdom_wood_trapdoor", ModBlocks.WISDOM_WOOD_PLANKS.getDefaultState());
	}

	@Nullable
	@Override
	public ModCreativeTab getCreativeTab() {
		return Wizardry.tab;
	}
}