package com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges;

import com.davenonymous.integratedmanager.lib.gui.GUISpriteInfo;
import com.davenonymous.integratedmanager.lib.gui.Icons;

public enum LineStyle {
	AA_THIN(true),
	THIN(),
	ARROW(0, true, 1.0f, 0, null, false),
	MEDIUM(2.0f, 0, 0),
	THICK(3.0f, 0, 0),
	INTEGRATED_DYNAMICS_CABLE(new GUISpriteInfo(Icons.guiIDCable, 6, 4)),
	INTEGRATED_DYNAMICS_MONO(new GUISpriteInfo(Icons.guiIDCable, 6, 4), 6),
	;


	public boolean useOpenGL = false;
	public float lineThickness = 1.0f;
	public int dashLength = 0;
	public GUISpriteInfo sprite = null;
	public int spacing = 0;
	public boolean drawArrow = false;

	LineStyle() {
	}


	LineStyle(boolean useOpenGL) {
		this.useOpenGL = useOpenGL;
	}

	LineStyle(GUISpriteInfo sprite) {
		this.sprite = sprite;
	}

	LineStyle(GUISpriteInfo sprite, int spacing) {
		this.sprite = sprite;
		this.spacing = spacing;
	}

	LineStyle(float lineThickness, int spacing, int dashLength) {
		this.lineThickness = lineThickness;
		this.spacing = spacing;
		this.dashLength = dashLength;
	}

	LineStyle(int dashLength, boolean drawArrow, float lineThickness, int spacing, GUISpriteInfo sprite, boolean useOpenGL) {
		this.dashLength = dashLength;
		this.drawArrow = drawArrow;
		this.lineThickness = lineThickness;
		this.spacing = spacing;
		this.sprite = sprite;
		this.useOpenGL = useOpenGL;
	}
}
