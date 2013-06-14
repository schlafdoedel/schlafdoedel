package com.kg6.schlafdoedel.event;

public interface EventNotification {
	public void onEventRaised(Event event);
	public void onEventDismissed(Event event);
}
