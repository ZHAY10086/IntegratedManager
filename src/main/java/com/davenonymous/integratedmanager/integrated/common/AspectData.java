package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;

public class AspectData {
	public ResourceLocation uniqueName;
	public boolean isWriteAspect;
	public String translationKey;

	public AspectData(IAspect<?, ?> aspect) {
		this.uniqueName = aspect.getUniqueName();
		this.isWriteAspect = aspect instanceof IAspectWrite<?,?>;
		this.translationKey = aspect.getTranslationKey();
	}

	public AspectData(RegistryFriendlyByteBuf buf) {
		this.uniqueName = buf.readResourceLocation();
		this.translationKey = buf.readUtf();
		this.isWriteAspect = buf.readBoolean();
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeResourceLocation(uniqueName);
		buf.writeUtf(translationKey);
		buf.writeBoolean(isWriteAspect);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, AspectData> STREAM_CODEC =
		StreamCodec.ofMember(AspectData::writeToBuffer, AspectData::new);
}
