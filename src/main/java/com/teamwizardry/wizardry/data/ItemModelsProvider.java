package com.teamwizardry.wizardry.data;

import com.teamwizardry.wizardry.Wizardry;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelsProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
	public ItemModelsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, Wizardry.MODID, existingFileHelper);
	}

	@SuppressWarnings("unused")
    private void pointToBlock(Item item) {
		String name = item.getRegistryName().getPath();
		getBuilder(name).parent(new ModelFile.UncheckedModelFile(Wizardry.location("block/" + name)));
	}

	@Override
	protected void registerModels() {

	}

	@Override
	public String getName() {
		return "Wizardry item models";
	}
}
