package com.davenonymous.integratedmanager.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import java.util.Collection;
import java.util.function.IntFunction;

public class NetworkHelper {
	public static <T, C extends Collection<T>> C readCollection(RegistryFriendlyByteBuf buf, IntFunction<C> collectionFactory, StreamDecoder<? super RegistryFriendlyByteBuf, T> elementReader) {
		int i = buf.readVarInt();
		C c = collectionFactory.apply(i);

		for(int j = 0; j < i; ++j) {
			c.add(elementReader.decode(buf));
		}

		return c;
	}

	public static <T> void writeCollection(RegistryFriendlyByteBuf buf, Collection<T> collection, StreamEncoder<? super RegistryFriendlyByteBuf, T> elementWriter) {
		buf.writeVarInt(collection.size());

		for(T t : collection) {
			elementWriter.encode(buf, t);
		}

	}
}
