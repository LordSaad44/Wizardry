package com.teamwizardry.wizardry.common.block;

import com.teamwizardry.librarianlib.common.base.ModCreativeTab;
import com.teamwizardry.librarianlib.common.base.block.BlockModContainer;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.block.IManaSink;
import com.teamwizardry.wizardry.common.tile.TileStaff;
import com.teamwizardry.wizardry.init.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Saad on 5/7/2016.
 */
public class BlockStaff extends BlockModContainer implements IManaSink {

	public BlockStaff() {
		super("staff_block", Material.ROCK);
		setCreativeTab(Wizardry.tab);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileStaff te = getTE(world, pos);

			if ((te.pearl == null) && (heldItem != null) && (heldItem.getItem() == ModItems.MANA_ORB || heldItem.getItem() == ModItems.PEARL_NACRE)) {
				te.pearl = heldItem.copy();
				te.pearl.stackSize = 1;
				heldItem.stackSize--;

			} else if ((te.pearl != null)) {
				ItemStack stack = te.pearl.copy();
				te.pearl = null;
				if (player.inventory.addItemStackToInventory(stack)) player.openContainer.detectAndSendChanges();
				else {
					EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY() + 1, pos.getZ(), stack);
					world.spawnEntity(entityItem);
				}
			}
			te.markDirty();
		}
		return true;
	}

	private TileStaff getTE(World world, BlockPos pos) {
		return (TileStaff) world.getTileEntity(pos);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState iBlockState) {
		return new TileStaff();
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Nullable
	@Override
	public ModCreativeTab getCreativeTab() {
		return Wizardry.tab;
	}
}