package com.teamwizardry.wizardry.client.render.block;

import com.teamwizardry.librarianlib.features.animator.Animator;
import com.teamwizardry.librarianlib.features.animator.Easing;
import com.teamwizardry.librarianlib.features.animator.animations.BasicAnimation;
import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.math.interpolate.numeric.InterpFloatInOut;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.features.tesr.TileRenderHandler;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.common.tile.TileJar;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

/**
 * Created by Demoniaque.
 */
@SideOnly(Side.CLIENT)
public class TileJarRenderer extends TileRenderHandler<TileJar> {

	public Vec3d fairyPos = Vec3d.ZERO;
	private Animator ANIMATOR = new Animator();
	private TileJar te;

	public TileJarRenderer(TileJar jar) {
		super(jar);
		this.te = jar;
		animCurve();
	}

	@Override
	public void render(float partialTicks, int destroyStage, float alpha) {
		if (!te.hasFairy) return;
		Vec3d pos = new Vec3d(te.getPos()).add(0.5, 0.35, 0.5).add(fairyPos);

		float excitement = (float) (tile.cap.getHandler().getMana() / tile.cap.getHandler().getMaxMana());

		Color color = te.color;
		ParticleBuilder glitter = new ParticleBuilder((int) (RandUtil.nextInt(1, 3) + (10 * (1 - excitement))));
		glitter.setColor(color);
		glitter.setRender(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
		glitter.setAlphaFunction(new InterpFloatInOut(0.2f, 1f));
		glitter.setScale(0.3f + (excitement * 3));
		ParticleSpawner.spawn(glitter, te.getWorld(), new StaticInterp<>(pos), 1);

		if (RandUtil.nextInt((int) (10 * (1 - excitement))) == 0) {
			ParticleBuilder trail = new ParticleBuilder(RandUtil.nextInt(10, 20));
			trail.setColor(te.color);
			trail.setRender(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
			trail.setAlphaFunction(new InterpFloatInOut(0.2f, 1f));
			trail.setScale(0.2f + 0.2f * excitement);
			//trail.enableMotionCalculation();
			ParticleSpawner.spawn(trail, te.getWorld(), new StaticInterp<>(pos), 1, 0, (aFloat, particleBuilder) -> {
				trail.setMotion(new Vec3d(
						RandUtil.nextDouble(-0.005, 0.005) * (10 * excitement),
						RandUtil.nextDouble(-0.005, 0.005) * (10 * excitement),
						RandUtil.nextDouble(-0.005, 0.005) * (10 * excitement)
				));
			});
		}
	}

	private void animCurve() {
		float excitement = (float) (1f - tile.cap.getHandler().getMana() / tile.cap.getHandler().getMaxMana());
		new BasicAnimation<>(this, "fairyPos").ease(Easing.easeInQuint)
				.to(new Vec3d(RandUtil.nextDouble(-0.1, 0.1), RandUtil.nextDouble(-0.25, 0.25), RandUtil.nextDouble(-0.1, 0.1)))
				.duration((1 + RandUtil.nextFloat(10, 20) * excitement))
				.completion(this::animCurve).addTo(ANIMATOR);

	}
}
