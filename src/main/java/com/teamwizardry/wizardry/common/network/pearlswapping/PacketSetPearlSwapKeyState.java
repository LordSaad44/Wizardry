package com.teamwizardry.wizardry.common.network.pearlswapping;

import com.teamwizardry.librarianlib.features.autoregister.PacketRegister;
import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.saving.Save;
import com.teamwizardry.wizardry.init.ModKeybinds;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

@PacketRegister(Side.SERVER)
public class PacketSetPearlSwapKeyState extends PacketBase {

	@Save
	public boolean keyState;

	public PacketSetPearlSwapKeyState() {
	}

	public PacketSetPearlSwapKeyState(boolean keyState) {
		this.keyState = keyState;
	}

	@Override
	public void handle(@NotNull MessageContext ctx) {
		ModKeybinds.putPearlSwappingState(ctx.getServerHandler().player.getUniqueID(), keyState);
	}
}
