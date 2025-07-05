package com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;

import java.util.Objects;

public abstract class AbstractGraphEdge implements IGraphEdge {
	Widget source;
	Widget target;
	LineStyle style = null;

	int colorSource = 0x00FFFFFF; // Default color for source widget
	int colorTarget = 0xFFFFFFFF; // Default color for target widget

	public AbstractGraphEdge(Widget source, Widget target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public Widget source() {
		return source;
	}

	@Override
	public Widget target() {
		return target;
	}

	@Override
	public int colorSource() {
		return colorSource;
	}

	@Override
	public int colorTarget() {
		return colorTarget;
	}

	@Override
	public LineStyle getStyle() {
		return style;
	}

	public AbstractGraphEdge setStyle(LineStyle style) {
		this.style = style;
		return this;
	}

	public AbstractGraphEdge setColorSource(int colorSource) {
		this.colorSource = colorSource;
		return this;
	}

	public AbstractGraphEdge setColorTarget(int colorTarget) {
		this.colorTarget = colorTarget;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof AbstractGraphEdge that)) {
			return false;
		}
		return Objects.equals(source, that.source) && Objects.equals(target, that.target);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, target);
	}
}
