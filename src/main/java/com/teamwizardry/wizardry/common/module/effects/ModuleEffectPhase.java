package com.teamwizardry.wizardry.common.module.effects;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.attribute.Attributes;
import com.teamwizardry.wizardry.api.spell.module.ModuleEffect;
import com.teamwizardry.wizardry.api.spell.module.ModuleModifier;
import com.teamwizardry.wizardry.api.spell.module.RegisterModule;
import com.teamwizardry.wizardry.common.module.modifiers.ModuleModifierExtendTime;
import com.teamwizardry.wizardry.init.ModPotions;
import com.teamwizardry.wizardry.init.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Created by Demoniaque.
 */
@RegisterModule
public class ModuleEffectPhase extends ModuleEffect {

	@Nonnull
	@Override
	public String getID() {
		return "effect_phase";
	}

	@Override
	public ModuleModifier[] applicableModifiers() {
		return new ModuleModifier[]{new ModuleModifierExtendTime()};
	}

	@Override
	@SuppressWarnings("unused")
	public boolean run(@Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		Entity caster = spell.getCaster();
		Entity targetEntity = spell.getVictim();
		Vec3d targetHit = spell.getTarget();

		double time = spellRing.getModifier(Attributes.DURATION, 10, 500);

		if (!tax(this, spell, spellRing)) return false;

		if (targetEntity != null && targetEntity instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) targetEntity;
			entity.addPotionEffect(new PotionEffect(ModPotions.PHASE, (int) time, 0, true, false));
			spell.world.playSound(null, targetEntity.getPosition(), ModSounds.ETHEREAL_PASS_BY, SoundCategory.NEUTRAL, 1, 1);
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(@Nonnull SpellData spell, @NotNull SpellRing spellRing) {

	}
}
