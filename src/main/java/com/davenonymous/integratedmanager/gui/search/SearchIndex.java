package com.davenonymous.integratedmanager.gui.search;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.overview.*;
import com.davenonymous.integratedmanager.integrated.common.ValueData;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;

import java.util.*;

public class SearchIndex {
	public static Map<ElementSearchables, Map<String, Set<Widget>>> searchIndex = new HashMap<>();

	public static Map<Widget, Map<ElementSearchables, Set<String>>> widgetSearchIndex = new HashMap<>();

	static {
		for (ElementSearchables searchable : ElementSearchables.values()) {
			searchIndex.put(searchable, new HashMap<>());
		}
	}

	public static void clear() {
		searchIndex.clear();
		widgetSearchIndex.clear();
	}

	public static void add(ElementSearchables type, String key, Widget widget) {
		var typedIndexMap = searchIndex.get(type);
		typedIndexMap.computeIfAbsent(key, k -> new HashSet<>()).add(widget);
		for(String word : key.split("\\s+")) {
			typedIndexMap.computeIfAbsent(word, k -> new HashSet<>()).add(widget);
		}

		widgetSearchIndex.computeIfAbsent(widget, k -> new HashMap<>())
				.computeIfAbsent(type, k -> new HashSet<>())
				.add(key);

	}

	public static void add(String key, Widget widget) {
		ElementSearchables type = null;
		ValueData valueData = null;
		if(widget instanceof NetworkPartWidget part) {
			type = ElementSearchables.PARTS;
			valueData = part.getValue().valueData;
		} else if(widget instanceof VariableFacadeWidget variable) {
			type = ElementSearchables.VARIABLES;
			valueData = variable.getValue().valueData;
		} else if(widget instanceof NetworkTileWidget) {
			type = ElementSearchables.TILES;
		} else if(widget instanceof PartTargetWidget) {
			type = ElementSearchables.TILES;
		}

		if(type == null) {
			throw new IllegalArgumentException("Unknown widget type: " + widget.getClass().getName());
		}

		add(type, key, widget);
		if(valueData != null) {
			String valueKey = valueData.getBestName();
			add(ElementSearchables.VALUES, valueKey, widget);
		}
	}

}
