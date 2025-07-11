package com.davenonymous.integratedmanager.gui.search;

public enum ElementSearchables {
	VARIABLES("variables"),
	PARTS("parts"),
	VALUES("values"),
	ASPECTS("aspects"),
	TILES("tiles"),
	IDS("ids");

	public String key;
	ElementSearchables(String key) {
		this.key = key;
	}
}
