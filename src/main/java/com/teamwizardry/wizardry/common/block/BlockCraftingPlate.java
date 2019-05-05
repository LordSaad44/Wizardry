package com.teamwizardry.wizardry.common.block;

import com.teamwizardry.librarianlib.features.base.block.tile.BlockModContainer;
import com.teamwizardry.librarianlib.features.helpers.ItemNBTHelper;
import com.teamwizardry.librarianlib.features.network.PacketHandler;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.block.IStructure;
import com.teamwizardry.wizardry.api.block.WizardryStructureRenderCompanion;
import com.teamwizardry.wizardry.api.item.IInfusableItem;
import com.teamwizardry.wizardry.api.spell.SpellBuilder;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.SpellUtils;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.common.network.PacketAddItemCraftingPlate;
import com.teamwizardry.wizardry.common.network.PacketExplode;
import com.teamwizardry.wizardry.common.network.PacketRemoveItemCraftingPlate;
import com.teamwizardry.wizardry.common.tile.TileCraftingPlate;
import com.teamwizardry.wizardry.init.ModBlocks;
import com.teamwizardry.wizardry.init.ModItems;
import com.teamwizardry.wizardry.init.ModSounds;
import com.teamwizardry.wizardry.init.ModStructures;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Created by Demoniaque on 6/10/2016.
 */
public class BlockCraftingPlate extends BlockModContainer implements IStructure {

	private static final AxisAlignedBB AABB_CRAFTING_PLATE = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0);

	public BlockCraftingPlate() {
		super("crafting_plate", Material.WOOD);
		setHardness(2.0F);
		setResistance(15.0f);
		setSoundType(SoundType.WOOD);
	}

	@Override
	public int getLightValue(@Nonnull IBlockState state, IBlockAccess world, @Nonnull BlockPos pos) {
		return 15;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState iBlockState) {
		return new TileCraftingPlate();
	}

	private TileCraftingPlate getTE(IBlockAccess world, BlockPos pos) {
		return (TileCraftingPlate) world.getTileEntity(pos);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;

		ItemStack heldItem = playerIn.getHeldItem(hand);

		if (testStructure(worldIn, pos).isEmpty()) {
			TileCraftingPlate plate = getTE(worldIn, pos);
			if (!plate.inputPearl.getHandler().getStackInSlot(0).isEmpty()) return false;
			if (!heldItem.isEmpty()) {
				if (heldItem.getItem() == ModItems.BOOK && playerIn.isCreative()) {
					ItemStack pearl = new ItemStack(ModItems.PEARL_NACRE);

					NBTTagList moduleList = ItemNBTHelper.getList(heldItem, Constants.NBT.SPELL, net.minecraftforge.common.util.Constants.NBT.TAG_STRING);
					if (moduleList == null) return false;

					SpellBuilder builder = new SpellBuilder(SpellUtils.getSpellItems(SpellUtils.deserializeModuleList(moduleList)));

					NBTTagList list = new NBTTagList();
					for (SpellRing spellRing : builder.getSpell()) {
						list.appendTag(spellRing.serializeNBT());
					}
					ItemNBTHelper.setList(pearl, Constants.NBT.SPELL, list);
					ItemNBTHelper.setBoolean(pearl, "infused", true);

					//Color lastColor = SpellUtils.getAverageSpellColor(builder.getSpell());
//
					//float[] hsv = ColorUtils.getHSVFromColor(lastColor);
					//ItemNBTHelper.setFloat(pearl, "hue", hsv[0]);
					//ItemNBTHelper.setFloat(pearl, "saturation", hsv[1]);
					ItemNBTHelper.setFloat(pearl, Constants.NBT.RAND, playerIn.world.rand.nextFloat());

					plate.outputPearl.getHandler().setStackInSlot(0, pearl);
					plate.markDirty();
					PacketHandler.NETWORK.sendToAllAround(new PacketExplode(new Vec3d(pos).add(0.5, 0.5, 0.5), Color.CYAN, Color.BLUE, 2, 2, 500, 300, 20, false),
							new NetworkRegistry.TargetPoint(worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 256));

					worldIn.playSound(null, pos, ModSounds.BASS_BOOM, SoundCategory.BLOCKS, 1f, (float) RandUtil.nextDouble(1, 1.5));
					return true;
				} else {
					ItemStack stack = heldItem.copy();
					int oldCount = stack.getCount();
					int subtractHand = playerIn.isSneaking() ? 64 : 1;
					heldItem.shrink(subtractHand);
					stack.setCount(oldCount - heldItem.getCount());

					if (!plate.isInventoryEmpty() && stack.getItem() instanceof IInfusableItem) {
						plate.inputPearl.getHandler().setStackInSlot(0, stack);
						plate.markDirty();

						playerIn.openContainer.detectAndSendChanges();
						worldIn.notifyBlockUpdate(pos, state, state, 3);

					} else if (!(stack.getItem() instanceof IInfusableItem)) {
						ItemHandlerHelper.insertItem(plate.realInventory.getHandler(), stack, false);
						PacketHandler.NETWORK.sendToAllAround(new PacketAddItemCraftingPlate(pos, stack), new NetworkRegistry.TargetPoint(worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 256));
						plate.markDirty();
						playerIn.openContainer.detectAndSendChanges();
					}

					return true;
				}
			} else {

				if (plate.hasOutputPearl()) {
					playerIn.setHeldItem(hand, plate.outputPearl.getHandler().extractItem(0, 1, false));
					plate.markDirty();

					playerIn.openContainer.detectAndSendChanges();

					return true;
				} else {

					for (int i = plate.realInventory.getHandler().getSlots() - 1; i >= 0; i--) {
						ItemStack extracted = plate.realInventory.getHandler().extractItem(i, playerIn.isSneaking() ? 64 : 1, false);
						PacketHandler.NETWORK.sendToAllAround(new PacketRemoveItemCraftingPlate(pos, i), new NetworkRegistry.TargetPoint(worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 256));
						if (!extracted.isEmpty()) {
							playerIn.addItemStackToInventory(extracted);
							plate.markDirty();

							playerIn.openContainer.detectAndSendChanges();

							break;
						}
					}
				}
				return true;
			}

		} else {
			if (playerIn.isCreative() && playerIn.isSneaking()) {
				buildStructure(worldIn, pos);
			} else {
				TileCraftingPlate plate = getTE(worldIn, pos);
				plate.revealStructure = !plate.revealStructure;
				plate.markDirty();
			}
		}
		return heldItem.isEmpty();
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB_CRAFTING_PLATE;
	}

	@Override
	public WizardryStructureRenderCompanion getStructure() {
		return ModStructures.INSTANCE.getStructure(ModBlocks.CRAFTING_PLATE);
	}

	@Override
	public Vec3i offsetToCenter() {
		return new Vec3i(4, 1, 4);
	}
}
