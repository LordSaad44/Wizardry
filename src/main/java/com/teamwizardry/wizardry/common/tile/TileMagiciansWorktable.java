package com.teamwizardry.wizardry.common.tile;

import com.google.common.collect.HashMultimap;
import com.teamwizardry.librarianlib.features.autoregister.TileRegister;
import com.teamwizardry.librarianlib.features.base.block.tile.TileMod;
import com.teamwizardry.librarianlib.features.saving.Save;
import com.teamwizardry.wizardry.api.spell.module.Module;
import com.teamwizardry.wizardry.api.spell.module.ModuleRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by LordSaad.
 */
@TileRegister("magicians_worktable")
public class TileMagiciansWorktable extends TileMod {

	@Save
	public BlockPos linkedTable;

	public HashMap<Module, UUID> paperComponents = new HashMap<>();
	@Deprecated
	public HashMultimap<Module, Module> modifiers = HashMultimap.create();
	public HashMap<UUID, UUID> componentLinks = new HashMap<>();

	@Override
	public void writeCustomNBT(@NotNull NBTTagCompound compound, boolean sync) {
		super.writeCustomNBT(compound, sync);

		NBTTagList list = new NBTTagList();
		for (Map.Entry<Module, UUID> entrySet : paperComponents.entrySet()) {
			NBTTagCompound compound1 = new NBTTagCompound();
			compound1.setTag("module", entrySet.getKey().serializeNBT());
			compound1.setUniqueId("uuid", entrySet.getValue());
			list.appendTag(compound1);
		}
		compound.setTag("components", list);

		list = new NBTTagList();
		for (Map.Entry<UUID, UUID> entrySet : componentLinks.entrySet()) {
			NBTTagCompound compound1 = new NBTTagCompound();
			compound1.setUniqueId("uuid1", entrySet.getKey());
			compound1.setUniqueId("uuid2", entrySet.getValue());
			list.appendTag(compound1);
		}
		compound.setTag("links", list);
	}

	@Override
	public void readCustomNBT(@NotNull NBTTagCompound compound) {
		super.readCustomNBT(compound);
		componentLinks = new HashMap<>();
		paperComponents = new HashMap<>();

		NBTTagList list = compound.getTagList("components", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound compound1 = list.getCompoundTagAt(i);
			if (compound1.hasKey("module") && compound1.hasKey("uuid")) {

				NBTTagCompound nbtModule = compound.getCompoundTag("module");

				if (nbtModule.hasKey("id")) {
					Module module = ModuleRegistry.INSTANCE.getModule(nbtModule.getString("id"));
					module.deserializeNBT(nbtModule);
					paperComponents.put(module, compound1.getUniqueId("uuid"));
				}
			}
		}

		list = compound.getTagList("links", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound compound1 = list.getCompoundTagAt(i);
			if (compound1.hasKey("uuid1") && compound1.hasKey("uuid2")) {
				componentLinks.put(compound1.getUniqueId("uuid1"), compound1.getUniqueId("uuid2"));
			}
		}
	}
}
