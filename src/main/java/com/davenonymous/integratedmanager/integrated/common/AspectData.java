package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.gui.search.ElementSearchables;
import com.davenonymous.integratedmanager.gui.search.SearchIndex;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
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

	public AspectData(IOperator operator) {
		this.uniqueName = operator.getUniqueName();
		this.isWriteAspect = operator instanceof IAspectWrite<?,?>;
		this.translationKey = operator.getTranslationKey();
	}

	public AspectData(IValueType<?> value) {
		this.uniqueName = value.getUniqueName();
		this.isWriteAspect = value instanceof IAspectWrite<?,?>;
		this.translationKey = value.getTranslationKey();
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

	public void updateSearchIndex(Widget owner) {
		if(I18n.exists(translationKey)) {
			SearchIndex.add(ElementSearchables.ASPECTS, I18n.get(translationKey), owner);
		}
		if(I18n.exists(translationKey + ".info")) {
			SearchIndex.add(ElementSearchables.ASPECTS, I18n.get(translationKey + ".info"), owner);
		}
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, AspectData> STREAM_CODEC =
		StreamCodec.ofMember(AspectData::writeToBuffer, AspectData::new);
}
