package com.kg6.schlafdoedel.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import com.kg6.schlafdoedel.Configuration;
import com.kg6.schlafdoedel.custom.Util;

public class EventScheduler extends Thread {
	private static EventScheduler eventScheduler;
	
	public static EventScheduler CreateInstance(Activity context, FrameLayout container) {
		if(eventScheduler == null) {
			eventScheduler = new EventScheduler(context, container);
		}
		
		return eventScheduler;
	}
	
	private final int SCHEDULER_SLEEPTIME = 1000;
	
	private final Activity CONTEXT;
	private final FrameLayout CONTAINER;
	
	private List<Event> eventList;
	private List<EventExecutor> eventExecutorList;
	private boolean enabled;
	
	private List<EventNotification> eventNotificationListenerList;
	
	private String sleepingPhase;
	
	private EventScheduler(Activity context, FrameLayout container) {
		CONTEXT = context;
		CONTAINER = container;
		
		this.eventList = new ArrayList<Event>();
		this.eventExecutorList = new ArrayList<EventExecutor>();
		this.enabled = true;
		
		this.eventNotificationListenerList = new ArrayList<EventNotification>();
		
		this.sleepingPhase = Configuration.COMMAND_SLEEPING_PHASE_AWAKE;
	}
	
	public void cleanup() {
		this.enabled = false;
	}
	
	public void addEventNotificationListener(EventNotification listener) {
		if(!this.eventNotificationListenerList.contains(listener)) {
			this.eventNotificationListenerList.add(listener);
		}
	}
	
	public void removeEventNotificationListener(EventNotification listener) {
		this.eventNotificationListenerList.remove(listener);
	}
	
	public void setSleepingPhase(String sleepingPhase) {
		this.sleepingPhase = sleepingPhase;
	}
	
	public void addEvent(Event event) {
		if(!this.eventList.contains(event)) {
			final long timeOfDay = System.currentTimeMillis() - Util.GetMillisecondsOfDay();
			
			if(event.getEnd() < timeOfDay) {
				event.handle();
			}
			
			this.eventList.add(event);
			
			fireOnEventListChanged();
		}
	}
	
	public void removeEvent(Event event) {
		if(this.eventList.contains(event)) {
			this.eventList.remove(event);
			
			dismissEvent(event);
			
			fireOnEventListChanged();
		}
	}
	
	public List<Event> getEventList() {
		return Collections.unmodifiableList(this.eventList);
	}
	
	public Event getNextUpcomingEvent() {
		Event nextEvent = null;
		
		final long timeOfDay = System.currentTimeMillis() - Util.GetMillisecondsOfDay();
		
		for(int i = 0; i < this.eventList.size(); i++) {
			Event event = this.eventList.get(i);
			
			if((nextEvent == null || event.getStart() < nextEvent.getStart()) && event.getStart() > timeOfDay) {
				nextEvent = event;
			}
		}
		
		return nextEvent;
	}
	
	public void dismissEvent(Event event) {
		for(int i = this.eventExecutorList.size() - 1; i >= 0; i--) {
			EventExecutor eventExecutor = this.eventExecutorList.get(i);
			
			if(eventExecutor.getEvent().equals(event)) {
				eventExecutor.dismiss();
				
				fireOnEventDismissed(eventExecutor.getEvent());
				
				removeEvent(event);
			}
		}
	}
	
	public void run() {
		while(isEnabled()) {
			try {
				final long timeOfDay = System.currentTimeMillis() - Util.GetMillisecondsOfDay();
				
				//Check if there are events that should be handled
				for(int i = 0; i < this.eventList.size(); i++) {
					Event event = this.eventList.get(i);
					
					if(event.getStart() < timeOfDay && (this.sleepingPhase.compareTo(Configuration.COMMAND_SLEEPING_PHASE_AWAKE) == 0 || this.sleepingPhase.compareTo(Configuration.COMMAND_SLEEPING_PHASE_SHALLOW) == 0 || event.getEnd() < timeOfDay) && event.handle()) {
						handleEvent(event);
						
						fireOnEventRaised(event);
					}
				}
				
				//Check if there are executors which should be stopped
				for(int i = this.eventExecutorList.size() - 1; i >= 0; i--) {
					EventExecutor eventExecutor = this.eventExecutorList.get(i);
					Event event = eventExecutor.getEvent();
					
					if(event.getEnd() + Configuration.EVENT_MAXIMUM_DURATION < timeOfDay) {
						eventExecutor.dismiss();
					}
					
					if(eventExecutor.isDismissed()) {
						fireOnEventDismissed(event);
						
						removeEvent(event);
					}
				}
			} catch (Exception e) {
				Log.e("EventScheduler.java", "Unable to handle event", e);
			}
			
			try {
				Thread.sleep(SCHEDULER_SLEEPTIME);
			} catch (InterruptedException e) {
				
			}
		}
		
		//Stop all executors
		for(int i = 0; i < this.eventExecutorList.size(); i++) {
			EventExecutor eventExecutor = this.eventExecutorList.get(i);
			
			eventExecutor.dismiss();
			
			fireOnEventDismissed(eventExecutor.getEvent());
		}
		
		this.eventExecutorList.clear();
	}
	
	private boolean isEnabled() {
		return this.enabled;
	}
	
	private void handleEvent(final Event event) {
		EventExecutor executor = new EventExecutor(CONTEXT, this, event, CONTAINER);
		executor.start();
		
		this.eventExecutorList.add(executor);
	}
	
	private void fireOnEventListChanged() {
		for(int i = 0; i < this.eventNotificationListenerList.size(); i++) {
			this.eventNotificationListenerList.get(i).onEventListChanged();
		}
	}
	
	private void fireOnEventRaised(Event event) {
		for(int i = 0; i < this.eventNotificationListenerList.size(); i++) {
			this.eventNotificationListenerList.get(i).onEventRaised(event);
		}
	}
	
	private void fireOnEventDismissed(Event event) {
		for(int i = 0; i < this.eventNotificationListenerList.size(); i++) {
			this.eventNotificationListenerList.get(i).onEventDismissed(event);
		}
	}
	
	public void raiseEventError(Event event, String error) {
		for(int i = 0; i < this.eventNotificationListenerList.size(); i++) {
			this.eventNotificationListenerList.get(i).onEventError(event, error);
		}
	}
}
