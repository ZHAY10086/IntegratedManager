package com.davenonymous.integratedmanager.integrated.client;

import com.davenonymous.integratedmanager.integrated.common.IntegratedConnectionType;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
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

	public BlockPos masterPosition; // Position of the master tile
	public int networkId = 0;
	public int totalParts = 0;

	public Map<Integer, VariableData> variableDataByProxyId = new HashMap<>();
	public Map<Integer, VariableData> variableDataById = new HashMap<>();
	public Map<Integer, NetworkElementData> elementDataById = new HashMap<>();
	public Map<Integer, NetworkElementData> elementDataByPartId = new HashMap<>();
	public List<NetworkElementData> elementDataList = new ArrayList<>();
	public Map<Integer, Map<Integer, IntegratedConnectionType>> paths = new HashMap<>();
	public Map<Integer, List<NetworkElementData>> elementDataByPathId = new HashMap<>();

	public int minX = Integer.MAX_VALUE;
	public int minY = Integer.MAX_VALUE;

	public NetworkData reset() {
		this.masterPosition = null; // Reset master position
		this.networkId = 0;
		this.totalParts = 0;
		this.elementDataById.clear();
		this.elementDataByPartId.clear();
		this.elementDataList.clear();
		this.variableDataById.clear();
		this.variableDataByProxyId.clear();
		this.paths.clear();
		this.elementDataByPathId.clear();

		this.minX = Integer.MAX_VALUE;
		this.minY = Integer.MAX_VALUE;

		return this;
	}

	public void addElementData(NetworkElementData data) {
		if (data == null) {
			return; // Ignore null data
		}
		elementDataList.add(data);

		for(VariableData variableData : data.variables) {
			variableDataById.put(variableData.id, variableData);
		}

		if(data.id != -1) {
			this.elementDataById.put(data.id, data);
		}
		if(data.partId != -1) {
			this.elementDataByPartId.put(data.partId, data);
		}
		if(data.position != null) {
			if (data.position.getX() < minX) {
				minX = data.position.getX();
			}
			if (data.position.getY() < minY) {
				minY = data.position.getY();
			}
		}
		if(data.pathId != -1) {
			var connectedPaths = this.paths.computeIfAbsent(data.pathId, k -> new HashMap<>());
			for(int neighborId : data.connections.keySet()) {
				IntegratedConnectionType connectionType = data.connections.get(neighborId);
				if(connectionType != null) {
					connectedPaths.put(neighborId, connectionType);
				}
			}

			this.elementDataByPathId.computeIfAbsent(data.pathId, k -> new ArrayList<>()).add(data);
		}
	}

	private void inferProxyVariables(NetworkElementData elementData) {
		if(elementData.tileData == null || elementData.tileData.proxyId < 0) {
			return;
		}

		if(elementData.variables.isEmpty()) {
			return; // No variables, nothing to infer
		}

		int proxyId = elementData.tileData.proxyId;
		VariableData proxyVariable = elementData.variables.get(0);
		this.variableDataByProxyId.put(proxyId, proxyVariable);
	}

	public void inferValues() {
		for(NetworkElementData elementData : elementDataList) {
			inferProxyVariables(elementData);
		}
	}

}
