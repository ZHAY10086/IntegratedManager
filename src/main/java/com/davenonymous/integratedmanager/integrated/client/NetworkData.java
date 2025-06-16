package com.davenonymous.integratedmanager.integrated.client;

import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkData {
	private static final NetworkData INSTANCE = new NetworkData();


	private NetworkData() {
	}

	public static NetworkData cache() {
		return INSTANCE;
	}

	// ----------

	public int networkId = 0;
	public int totalParts = 0;
	public int usedVariables = 0;
	public int freeVariables = 0;

	public Map<Integer, NetworkElementData> elementDataById = new HashMap<>();
	public Map<Integer, NetworkElementData> elementDataByPartId = new HashMap<>();
	public Map<BlockPos, NetworkElementData> elementDataByPosition = new HashMap<>();
	public List<NetworkElementData> elementDataList = new ArrayList<>();

	public int minX = Integer.MAX_VALUE;
	public int minY = Integer.MAX_VALUE;

	public NetworkData reset() {
		this.networkId = 0;
		this.totalParts = 0;
		this.usedVariables = 0;
		this.freeVariables = 0;
		this.elementDataById.clear();
		this.elementDataByPosition.clear();
		this.elementDataByPartId.clear();
		this.elementDataList.clear();
		this.minX = Integer.MAX_VALUE;
		this.minY = Integer.MAX_VALUE;

		return this;
	}

	public void addElementData(NetworkElementData data) {
		if (data == null) {
			return; // Ignore null data
		}
		elementDataList.add(data);

		if(data.id != -1) {
			this.elementDataById.put(data.id, data);
		}
		if(data.partId != -1) {
			this.elementDataByPartId.put(data.partId, data);
		}
		if(data.position != null) {
			this.elementDataByPosition.put(data.position, data);
			if (data.position.getX() < minX) {
				minX = data.position.getX();
			}
			if (data.position.getY() < minY) {
				minY = data.position.getY();
			}
		}

	}

}
