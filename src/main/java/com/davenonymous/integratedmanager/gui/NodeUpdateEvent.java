package com.davenonymous.integratedmanager.gui;

import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.lib.gui.event.IEvent;

public record NodeUpdateEvent(NetworkElementData data) implements IEvent {
}
