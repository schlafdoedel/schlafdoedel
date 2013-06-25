package com.kg6.schlafdoedel.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.kg6.schlafdoedel.Configuration;
import com.kg6.schlafdoedel.custom.Util;

public class EventScheduler extends Thread {
	private static EventScheduler eventScheduler;
	
	public static EventScheduler CreateInstance() {
		return eventScheduler;
	}
	
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
			
			notifyEventListenersForChangedEvents();
		}
	}
	
	public void removeEvent(Event event) {
		if(this.eventList.contains(event)) {
			this.eventList.remove(event);
			
			dismissEvent(event);
			
			notifyEventListenersForChangedEvents();
		}
	}
	
	public List<Event> getEventList() {
		return Collections.unmodifiableList(this.eventList);
	}
	
	public void setEventAudioVolume(float volume) {
		try {
			for(int i = 0; i < this.eventExecutorList.size(); i++) {
				EventExecutor eventExecutor = this.eventExecutorList.get(i);
				
				eventExecutor.setVolume(volume);
			}
		} catch (Exception e) {
			Log.e("EventScheduler.java", "Unable to change the event media volume", e);
		}
	}
	
	public float getEventAudioVolume() {
		if (this.eventExecutorList.size() > 0) {
			return this.eventExecutorList.get(0).getVolume();
		}
		
		return 0;
	}
	
	public Event getNextUpcomingEvent() {
		Event nextEvent = null;
		Event firstEvent = null;
		
		final long timeOfDay = System.currentTimeMillis() - Util.GetMillisecondsOfDay();
		
		for(int i = 0; i < this.eventList.size(); i++) {
			Event event = this.eventList.get(i);
			
			if(firstEvent == null || event.getStart() < firstEvent.getStart()) {
				firstEvent = event;
			}
			
			if((nextEvent == null || event.getStart() < nextEvent.getStart()) && event.getStart() > timeOfDay) {
				nextEvent = event;
			}
		}
		
		if(nextEvent != null) {
			return nextEvent;
		}
		
		return firstEvent;
	}
	
	public void dismissEvent(Event event) {
		for(int i = this.eventExecutorList.size() - 1; i >= 0; i--) {
			EventExecutor eventExecutor = this.eventExecutorList.get(i);
			
			if(eventExecutor.getEvent().equals(event)) {
				eventExecutor.dismiss();
				
				fireOnEventDismissed(eventExecutor.getEvent());
				
				this.eventExecutorList.remove(i);
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
	
	public void setScreenBrightness(final float brightness) {
		CONTEXT.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				float nextScreenBrightness = brightness;
				
				if(eventExecutorList.size() > 0) {
					nextScreenBrightness = 1;
				}
				
				//Modify the brightness
				Window myWindow = CONTEXT.getWindow();

				WindowManager.LayoutParams winParams = myWindow.getAttributes();
				winParams.screenBrightness = nextScreenBrightness;

				myWindow.setAttributes(winParams);
			}

		});
	}
	
	private void handleEvent(final Event event) {
		setScreenBrightness(1);
		
		//If there is an event with the same source type currently in the list, remove it
		List<EventSource> eventSourceList = event.getEventSourceList();
		
		for(int i = 0; i < this.eventExecutorList.size(); i++) {
			EventExecutor executor = this.eventExecutorList.get(i);
			List<EventSource> executedSourceList = executor.getEvent().getEventSourceList();
			
			boolean sourceFound = false;
			
			for(EventSource eventSource : executedSourceList) {
				if(eventSourceList.contains(eventSource)) {
					sourceFound = true;
					break;
				}
			}
			
			if(sourceFound) {
				executor.dismiss();
			}
		}
		
		//Start the event
		EventExecutor executor = new EventExecutor(CONTEXT, this, event, CONTAINER);
		executor.start();
		
		this.eventExecutorList.add(executor);
	}
	
	public void notifyEventListenersForChangedEvents() {
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
