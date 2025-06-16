package com.davenonymous.integratedmanager.lib.gui.widgets.graph;

import java.util.function.Supplier;

public enum GraphAlgorithms {
	FIXED(FixedPositioning::new),
	INTEGRATE_THEN_APPLY(IntegrateThenApplyPositioning::new);

	private final Supplier<IGraphAlgorithm> supplier;

	GraphAlgorithms(Supplier<IGraphAlgorithm> supplier) {
		this.supplier = supplier;
	}

	public IGraphAlgorithm get() {
		return supplier.get();
	}
}
