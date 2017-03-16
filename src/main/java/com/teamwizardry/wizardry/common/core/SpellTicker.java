package com.teamwizardry.wizardry.common.core;

import com.teamwizardry.wizardry.api.spell.Module;
import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellStack;
import kotlin.Pair;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

/**
 * Created by LordSaad.
 */
public class SpellTicker {

	public static SpellTicker INSTANCE = new SpellTicker();

	public HashMap<Module, Pair<SpellData, Integer>> ticker = new HashMap<>();

	private SpellTicker() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void tick(TickEvent.WorldTickEvent event) {
		ticker.keySet().removeIf(module -> {
			int time = ticker.get(module).getSecond();
			if (time > 0) {
				ticker.put(module, new Pair<>(ticker.get(module).getFirst().copy(), --time));
				SpellStack.runModules(module, ticker.get(module).getFirst());
				return false;
			} else return true;
		});
	}
}