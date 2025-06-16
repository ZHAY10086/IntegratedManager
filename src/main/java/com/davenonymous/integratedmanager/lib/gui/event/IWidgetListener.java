package com.davenonymous.integratedmanager.lib.gui.event;


import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;

public interface IWidgetListener<T extends IEvent> {
	WidgetEventResult call(T event, Widget widget);
}
