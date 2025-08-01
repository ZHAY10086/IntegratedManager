package com.davenonymous.integratedmanager.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import java.util.Collection;
import java.util.Map;
import java.util.function.IntFunction;

public class NetworkHelper {

	public static <K, V, M extends Map<K, V>> M readMap(RegistryFriendlyByteBuf buf, IntFunction<M> mapFactory, StreamDecoder<? super RegistryFriendlyByteBuf, K> keyReader, StreamDecoder<? super RegistryFriendlyByteBuf, V> valueReader) {
		int i = buf.readVarInt();
		M m = (M)(mapFactory.apply(i));

		for(int j = 0; j < i; ++j) {
			K k = (K)keyReader.decode(buf);
			V v = (V)valueReader.decode(buf);
			m.put(k, v);
		}

		return m;
	}

	public static <K, V> void writeMap(RegistryFriendlyByteBuf buf, Map<K, V> map, StreamEncoder<? super RegistryFriendlyByteBuf, K> keyWriter, StreamEncoder<? super RegistryFriendlyByteBuf, V> valueWriter) {
		buf.writeVarInt(map.size());
		map.forEach((p_319534_, p_319535_) -> {
			keyWriter.encode(buf, p_319534_);
			valueWriter.encode(buf, p_319535_);
		});
	}

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
