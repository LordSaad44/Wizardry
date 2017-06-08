package com.teamwizardry.wizardry.client.render.entity;

import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.features.particle.functions.InterpColorHSV;
import com.teamwizardry.librarianlib.features.particle.functions.InterpFadeInOut;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.api.util.interp.InterpScale;
import com.teamwizardry.wizardry.common.entity.EntitySpellProjectile;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Created by Saad on 8/25/2016.
 */
public class RenderSpellProjectile extends Render<EntitySpellProjectile> {

	public RenderSpellProjectile(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(@Nonnull EntitySpellProjectile entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		Color color = entity.primaryColor;
		Color color2 = entity.secondaryColor;

		ParticleBuilder glitter = new ParticleBuilder(10);
		glitter.setAlphaFunction(new InterpFadeInOut(0.3f, 0.3f));
		glitter.setRender(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
		glitter.setCollision(true);
		glitter.setColorFunction(new InterpColorHSV(color, color2));
		glitter.setAcceleration(new Vec3d(0, RandUtil.nextDouble(-0.005, -0.001), 0));
		ParticleSpawner.spawn(glitter, entity.world, new StaticInterp<>(entity.getPositionVector()), 5, 0, (aFloat, particleBuilder) -> {
			glitter.setScaleFunction(new InterpScale((float) RandUtil.nextDouble(0.3, 0.8), 0));
			glitter.setLifetime(RandUtil.nextInt(10, 100));
			glitter.addMotion(new Vec3d(
					RandUtil.nextDouble(-0.01, 0.01),
					RandUtil.nextDouble(-0.01, 0.01),
					RandUtil.nextDouble(-0.01, 0.01)
			));
		});
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(@NotNull EntitySpellProjectile entity) {
		return null;
	}
}
