package com.teamwizardry.wizardry.common.module.effects;

import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.math.interpolate.numeric.InterpFloatInOut;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.spell.IDelayedModule;
import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.annotation.RegisterModule;
import com.teamwizardry.wizardry.api.spell.SpellDataTypes.BlockSet;
import com.teamwizardry.wizardry.api.spell.SpellDataTypes.BlockStateCache;
import com.teamwizardry.wizardry.api.spell.attribute.AttributeRegistry;
import com.teamwizardry.wizardry.api.spell.module.IModuleEffect;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstanceEffect;
import com.teamwizardry.wizardry.api.spell.module.ModuleRegistry;
import com.teamwizardry.wizardry.api.util.BlockUtils;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.client.core.renderer.PhasedBlockRenderer;
import com.teamwizardry.wizardry.common.core.SpellNemezTracker;
import com.teamwizardry.wizardry.common.core.nemez.NemezEventHandler;
import com.teamwizardry.wizardry.common.core.nemez.NemezTracker;
import com.teamwizardry.wizardry.init.ModBlocks;
import com.teamwizardry.wizardry.init.ModPotions;
import com.teamwizardry.wizardry.init.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.teamwizardry.wizardry.api.util.PosUtils.getPerpendicularFacings;

/**
 * Created by Demoniaque.
 */
@RegisterModule(ID="effect_phase")
public class ModuleEffectPhase implements IModuleEffect, IDelayedModule {

	@Override
	public String[] compatibleModifiers() {
		return new String[]{"modifier_extend_time", "modifier_increase_aoe", "modifier_extend_range"};
	}

	@Override
	public void runDelayedEffect(SpellData spell, SpellRing spellRing) {
		BlockPos targetPos = spell.getTargetPos();
		if (targetPos == null) return;

		NemezTracker nemezDrive = SpellNemezTracker.getAndRemoveNemezDrive(spell.world, targetPos);

		if (nemezDrive != null) {
			NemezEventHandler.reverseTime(spell.world, nemezDrive, targetPos);
		}
	}

	@Override
	public boolean run(ModuleInstanceEffect instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		Entity caster = spell.getCaster();
		Entity targetEntity = spell.getVictim();
		BlockPos targetPos = spell.getTargetPos();
		EnumFacing faceHit = spell.getFaceHit();

		double duration = spellRing.getAttributeValue(AttributeRegistry.DURATION, spell) * 20;
		double area = spellRing.getAttributeValue(AttributeRegistry.AREA, spell);
		double range = spellRing.getAttributeValue(AttributeRegistry.RANGE, spell);

		if (!spellRing.taxCaster(spell, true)) return false;

		if (targetEntity instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) targetEntity;
			entity.addPotionEffect(new PotionEffect(ModPotions.PHASE, (int) duration, 0, true, false));
			spell.world.playSound(null, targetEntity.getPosition(), ModSounds.ETHEREAL, SoundCategory.NEUTRAL, 1, 1);
		}

		if (targetPos != null && faceHit != null) {
			spell.world.playSound(null, targetPos, ModSounds.ETHEREAL, SoundCategory.NEUTRAL, 1, 1);
			NemezTracker nemezDrive = SpellNemezTracker.getOrCreateNemezDrive(spell.world, targetPos);
			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(targetPos);

			faceHit = faceHit.getOpposite();

			IBlockState targetState = spell.world.getBlockState(mutable);
			if (BlockUtils.isAnyAir(targetState)) return true;

			Set<BlockPos> poses = new HashSet<>();
			HashMap<BlockPos, IBlockState> stateCache = new HashMap<>();

			int rangeTick = 0;
			while (rangeTick <= (int) range) {

				AxisAlignedBB bb = new AxisAlignedBB(mutable, mutable);
				switch (faceHit) {
					case DOWN:
					case UP:
						bb = bb.grow(area + 1, 0, area + 1);
						break;
					case NORTH:
					case SOUTH:
						bb = bb.grow(area + 1, area + 1, 0);
						break;
					case WEST:
					case EAST:
						bb = bb.grow(0, area + 1, area + 1);
						break;
				}

				Set<BlockPos> edges = new HashSet<>();
				switch (faceHit) {
					case DOWN:
					case UP:
						for (int x = (int) bb.minX; x <= (int) bb.maxX; x++) {
							for (int z = (int) bb.minZ; z <= (int) bb.maxZ; z++) {
								if (x == (int) bb.maxX || x == (int) bb.minX) {
									edges.add(new BlockPos(x, mutable.getY(), z));
								} else if (z == (int) bb.minZ || z == (int) bb.maxZ) {
									edges.add(new BlockPos(x, mutable.getY(), z));
								}
							}
						}
						break;
					case NORTH:
					case SOUTH:
						for (int x = (int) bb.minX; x <= (int) bb.maxX; x++) {
							for (int y = (int) bb.minY; y <= (int) bb.maxY; y++) {
								if (y == (int) bb.maxY || y == (int) bb.minY) {
									edges.add(new BlockPos(x, y, mutable.getZ()));
								} else if (x == (int) bb.minX || x == (int) bb.maxX) {
									edges.add(new BlockPos(x, y, mutable.getZ()));
								}
							}
						}
						break;
					case WEST:
					case EAST:
						for (int z = (int) bb.minZ; z <= (int) bb.maxZ; z++) {
							for (int y = (int) bb.minY; y <= (int) bb.maxY; y++) {
								if (y == (int) bb.maxY || y == (int) bb.minY) {
									edges.add(new BlockPos(mutable.getX(), y, z));
								} else if (z == (int) bb.minZ || z == (int) bb.maxZ) {
									edges.add(new BlockPos(mutable.getX(), y, z));
								}
							}
						}
						break;
				}

				HashMap<BlockPos, IBlockState> tmp = new HashMap<>();
				boolean fullAirPlane = true;
				int edgeAirCount = 0;
				int edgeBlockCount = 0;
				for (BlockPos pos : BlockPos.getAllInBox((int) bb.minX, (int) bb.minY, (int) bb.minZ, (int) bb.maxX, (int) bb.maxY, (int) bb.maxZ)) {

					IBlockState originalState = spell.world.getBlockState(pos);
					Block block = originalState.getBlock();

					if (edges.contains(pos)) {
						stateCache.put(pos, originalState);
						if (block == Blocks.AIR) edgeAirCount++;
						else edgeBlockCount++;
						continue;
					}

					if (block != Blocks.AIR) fullAirPlane = false;
					if (block == ModBlocks.FAKE_AIR) continue;
					if (spell.world.getTileEntity(pos) != null) continue;

					tmp.put(pos, originalState);
				}

				if (!fullAirPlane) {
					if (edgeAirCount <= edgeBlockCount) {
						for (Map.Entry<BlockPos, IBlockState> entry : tmp.entrySet()) {

							nemezDrive.trackBlock(entry.getKey(), entry.getValue());

							IBlockState state = ModBlocks.FAKE_AIR.getDefaultState();
							BlockUtils.placeBlock(spell.world, entry.getKey(), state, (EntityPlayerMP) caster);

							stateCache.put(entry.getKey(), state);

							nemezDrive.trackBlock(entry.getKey(), state);
						}
						poses.addAll(tmp.keySet());
					} else {
						for (Map.Entry<BlockPos, IBlockState> entry : tmp.entrySet()) {
							if (entry.getValue().getBlock() == Blocks.AIR) {
								stateCache.put(entry.getKey(), entry.getValue());
								continue;
							}

							nemezDrive.trackBlock(entry.getKey(), entry.getValue());

							IBlockState state = ModBlocks.FAKE_AIR.getDefaultState();
							BlockUtils.placeBlock(spell.world, entry.getKey(), state, (EntityPlayerMP) caster);

							stateCache.put(entry.getKey(), state);
							nemezDrive.trackBlock(entry.getKey(), state);

							poses.add(entry.getKey());
						}
					}
				} else break;

				mutable.move(faceHit);
				rangeTick++;
			}

			nemezDrive.endUpdate();
			nemezDrive.collapse();

			//spell.addData(SpellData.DefaultKeys.NEMEZ, nemezDrive.serializeNBT());
			spell.addData(SpellData.DefaultKeys.BLOCK_SET, new BlockSet(poses));
			spell.addData(SpellData.DefaultKeys.BLOCKSTATE_CACHE, new BlockStateCache(stateCache));

			addDelayedSpell(instance, spellRing, spell, (int) duration);
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderSpell(ModuleInstanceEffect instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		EnumFacing faceHit = spell.getFaceHit();

		Set<BlockPos> blockSet = spell.getDataWithFallback(SpellData.DefaultKeys.BLOCK_SET, new BlockSet(new HashSet<>())).getBlockSet();
		Map<BlockPos, IBlockState> blockStateCache = spell.getDataWithFallback(SpellData.DefaultKeys.BLOCKSTATE_CACHE, new BlockStateCache(new HashMap<>())).getBlockStateCache();
		HashMap<BlockPos, IBlockState> tmpCache = new HashMap<>(blockStateCache);

		double duration = spellRing.getAttributeValue(AttributeRegistry.DURATION, spell) * 20;
		PhasedBlockRenderer.addPhase(spell.world, blockSet, (int) duration);

		if (faceHit != null) {
			for (Map.Entry<BlockPos, IBlockState> entry : tmpCache.entrySet()) {

				IBlockState thisState = entry.getValue();
				if (thisState.getBlock() != ModBlocks.FAKE_AIR) continue;

				ParticleBuilder glitter2 = new ParticleBuilder(10);
				glitter2.setRenderNormalLayer(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
				glitter2.disableRandom();
				ParticleSpawner.spawn(glitter2, spell.world, new StaticInterp<>(new Vec3d(entry.getKey()).add(0.5, 0.5, 0.5)), 5, (int) duration, (aFloat, build) -> {
					build.setColor(Color.CYAN);
					//build.setAlphaFunction(new InterpFloatInOut(1f, 0.1f));
					build.setAlpha(RandUtil.nextFloat(0.05f, 0.2f));

					build.setPositionOffset(new Vec3d(
							RandUtil.nextDouble(-0.5, 0.5),
							RandUtil.nextDouble(-0.5, 0.5),
							RandUtil.nextDouble(-0.5, 0.5)
					));
					build.setMotion(new Vec3d(
							RandUtil.nextDouble(-0.001, 0.001),
							RandUtil.nextDouble(-0.001, 0.001),
							RandUtil.nextDouble(-0.001, 0.001)
					));
					build.setLifetime(RandUtil.nextInt(20, 40));
					build.setScaleFunction(new InterpFloatInOut(0.9f, 0.9f));
					build.setScale(RandUtil.nextFloat(0.1f, 0.3f));
				});

				BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(entry.getKey());
				for (EnumFacing facing : EnumFacing.VALUES) {
					mutable.move(facing);

					IBlockState adjState;
					if (!blockStateCache.containsKey(mutable)) {
						adjState = spell.world.getBlockState(mutable);
						blockStateCache.put(mutable.toImmutable(), adjState);
					} else adjState = blockStateCache.get(mutable);

					if (adjState.getBlock() != Blocks.AIR && adjState.getBlock() != ModBlocks.FAKE_AIR) {

						Vec3d directionOffsetVec = new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.5);
						Vec3d adjPos = new Vec3d(mutable).add(0.5, 0.5, 0.5).add(directionOffsetVec);

						for (EnumFacing subFacing : getPerpendicularFacings(facing)) {
							mutable.move(subFacing);

							IBlockState subState;
							if (!blockStateCache.containsKey(mutable)) {
								subState = spell.world.getBlockState(mutable);
								blockStateCache.put(mutable.toImmutable(), subState);
							} else subState = blockStateCache.get(mutable);

							if (BlockUtils.isAnyAir(subState)) {
								Vec3d subPos = new Vec3d(mutable).add(0.5, 0.5, 0.5).add(directionOffsetVec);
								Vec3d midPointVec = new Vec3d(
										(adjPos.x + subPos.x) / 2.0,
										(adjPos.y + subPos.y) / 2.0,
										(adjPos.z + subPos.z) / 2.0);
								Vec3d sub = subPos.subtract(adjPos);
								EnumFacing adjSubFacing = EnumFacing.getFacingFromVector((float) sub.x, (float) sub.y, (float) sub.z);
								Vec3d cross = new Vec3d(adjSubFacing.getDirectionVec()).crossProduct(new Vec3d(facing.getDirectionVec())).normalize().scale(0.5);

								ParticleBuilder glitter = new ParticleBuilder(10);
								glitter.setRenderNormalLayer(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
								glitter.disableRandom();
								ParticleSpawner.spawn(glitter, spell.world, new StaticInterp<>(midPointVec), 50, (int) duration, (aFloat, build) -> {
									build.setColor(Color.CYAN);
									//build.setAlphaFunction(new InterpFloatInOut(1f, 0.1f));
									build.setAlpha(RandUtil.nextFloat(0.3f, 0.7f));

									build.setPositionOffset(cross.scale(RandUtil.nextFloat(-1, 1)));
									build.setLifetime(RandUtil.nextInt(20, 40));
									build.setScaleFunction(new InterpFloatInOut(0.9f, 0.9f));
									build.setScale(RandUtil.nextFloat(0.2f, 0.5f));
								});
							}
							mutable.move(subFacing.getOpposite());
						}
					}
					mutable.move(facing.getOpposite());
				}
			}
		}
	}

	@NotNull
	@Override
	public SpellData renderVisualization(ModuleInstanceEffect instance, @Nonnull SpellData data, @Nonnull SpellRing ring, @Nonnull SpellData previousData) {
		if (ring.getParentRing() != null
				&& ring.getParentRing().getModule() != null
				&& ring.getParentRing().getModule() == ModuleRegistry.INSTANCE.getModule("event_collide_entity"))
			return previousData;

		BlockPos targetPos = data.getData(SpellData.DefaultKeys.BLOCK_HIT);
		EnumFacing faceHit = data.getFaceHit();

		double area = ring.getAttributeValue(AttributeRegistry.AREA, data);
		double range = ring.getAttributeValue(AttributeRegistry.RANGE, data);

		if (faceHit != null && targetPos != null) {

			IBlockState targetState = instance.getCachableBlockstate(data.world, targetPos, previousData);
			if (BlockUtils.isAnyAir(targetState)) return previousData;

			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(targetPos);

			faceHit = faceHit.getOpposite();

			int rangeTick = 0;
			while (rangeTick <= (int) range) {

				AxisAlignedBB bb = new AxisAlignedBB(mutable, mutable);
				switch (faceHit) {
					case DOWN:
					case UP:
						bb = bb.grow(area + 1, 0, area + 1);
						break;
					case NORTH:
					case SOUTH:
						bb = bb.grow(area + 1, area + 1, 0);
						break;
					case WEST:
					case EAST:
						bb = bb.grow(0, area + 1, area + 1);
						break;
				}

				Set<BlockPos> edges = new HashSet<>();
				switch (faceHit) {
					case DOWN:
					case UP:
						for (int x = (int) bb.minX; x <= (int) bb.maxX; x++) {
							for (int z = (int) bb.minZ; z <= (int) bb.maxZ; z++) {
								if (x == (int) bb.maxX || x == (int) bb.minX) {
									edges.add(new BlockPos(x, mutable.getY(), z));
								} else if (z == (int) bb.minZ || z == (int) bb.maxZ) {
									edges.add(new BlockPos(x, mutable.getY(), z));
								}
							}
						}
						break;
					case NORTH:
					case SOUTH:
						for (int x = (int) bb.minX; x <= (int) bb.maxX; x++) {
							for (int y = (int) bb.minY; y <= (int) bb.maxY; y++) {
								if (y == (int) bb.maxY || y == (int) bb.minY) {
									edges.add(new BlockPos(x, y, mutable.getZ()));
								} else if (x == (int) bb.minX || x == (int) bb.maxX) {
									edges.add(new BlockPos(x, y, mutable.getZ()));
								}
							}
						}
						break;
					case WEST:
					case EAST:
						for (int z = (int) bb.minZ; z <= (int) bb.maxZ; z++) {
							for (int y = (int) bb.minY; y <= (int) bb.maxY; y++) {
								if (y == (int) bb.maxY || y == (int) bb.minY) {
									edges.add(new BlockPos(mutable.getX(), y, z));
								} else if (z == (int) bb.minZ || z == (int) bb.maxZ) {
									edges.add(new BlockPos(mutable.getX(), y, z));
								}
							}
						}
						break;
				}

				HashMap<BlockPos, IBlockState> tmp = new HashMap<>();
				boolean fullAirPlane = true;
				for (BlockPos pos : BlockPos.getAllInBox((int) bb.minX, (int) bb.minY, (int) bb.minZ, (int) bb.maxX, (int) bb.maxY, (int) bb.maxZ)) {

					IBlockState originalState = instance.getCachableBlockstate(data.world, pos, previousData);
					Block block = originalState.getBlock();

					if (edges.contains(pos)) continue;

					if (block != Blocks.AIR) fullAirPlane = false;
					if (block == ModBlocks.FAKE_AIR) continue;
					if (data.world.getTileEntity(pos) != null) continue;

					if (BlockUtils.isAnyAir(originalState)) continue;
					tmp.put(pos, originalState);
				}

				if (!fullAirPlane) {
					for (Map.Entry<BlockPos, IBlockState> entry : tmp.entrySet()) {
						if (BlockUtils.isAnyAir(entry.getValue())) continue;

						BlockPos.MutableBlockPos mutable2 = new BlockPos.MutableBlockPos(entry.getKey());
						for (EnumFacing facing : getPerpendicularFacings(faceHit)) {

							mutable2.move(facing);

							if (!tmp.containsKey(mutable2))
								instance.drawFaceOutline(mutable2, facing.getOpposite());

							mutable2.move(facing.getOpposite());
						}
					}
				} else break;

				mutable.move(faceHit);
				rangeTick++;
			}
		}

		return previousData;
	}
}
