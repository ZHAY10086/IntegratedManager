package com.davenonymous.integratedmanager.lib.gui.tooltip;

import com.davenonymous.integratedmanager.lib.gui.ColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StringTooltipComponent(String message, int color) implements SerializableTooltipComponent<StringTooltipComponent> {

	public static final StreamCodec<FriendlyByteBuf, StringTooltipComponent> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, StringTooltipComponent::message,
		ByteBufCodecs.INT, StringTooltipComponent::color,
		StringTooltipComponent::new
	);

	public static final StringTooltipComponent white(String message) {
		return new StringTooltipComponent(message, ChatFormatting.WHITE.getColor());
	}

	public static final StringTooltipComponent gray(String message) {
		return new StringTooltipComponent(message, ChatFormatting.GRAY.getColor());
	}

	public static final StringTooltipComponent chat(String message, ChatFormatting color) {
		return new StringTooltipComponent(message, color.getColor());
	}

	public static StringTooltipComponent green(String message) {
		return new StringTooltipComponent(message, ColorHelper.COLOR_GREEN);
	}

	public static StringTooltipComponent orange(String message) {
		return new StringTooltipComponent(message, ColorHelper.COLOR_ORANGE);
	}

	public static StringTooltipComponent cyan(String message) {
		return new StringTooltipComponent(message, ColorHelper.COLOR_CYAN);
	}

	@Override
	public int getHeight() {
		return 10;
	}

	@Override
	public int getWidth(Font font) {
		return font.width(message);
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		guiGraphics.drawString(font, message, x, y, color);
	}

	@Override
	public StreamCodec<FriendlyByteBuf, StringTooltipComponent> getCodec() {
		return CODEC;
	}
}
