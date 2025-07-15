package com.davenonymous.integratedmanager.integrated.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum IntegratedConnectionType {
	CABLE(0, "cable"),
	MONO(1, "mono"),
	OMNI(2, "omni");

	private final int id;
	private final String name;

	IntegratedConnectionType(int id, String name) {
		this.name = name;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	// Gets Id -> Enum
	public static final IntFunction<IntegratedConnectionType> BY_ID =
		ByIdMap.continuous(
			IntegratedConnectionType::getId,
			IntegratedConnectionType.values(),
			ByIdMap.OutOfBoundsStrategy.ZERO
		);

	public static StreamCodec<ByteBuf, IntegratedConnectionType> STREAM_CODEC = ByteBufCodecs.idMapper(IntegratedConnectionType.BY_ID, IntegratedConnectionType::getId);
}
