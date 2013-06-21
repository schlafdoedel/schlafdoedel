package com.kg6.schlafdoedel.event;

public interface EventNotification {
	public void onEventListChanged();
	public void onEventRaised(Event event);
	public void onEventDismissed(Event event);
	public void onEventError(Event event, String error);
}
