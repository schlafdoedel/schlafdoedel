package com.kg6.schlafdoedel.event;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;

public class EventScheduler extends Thread {
	private final int SCHEDULER_SLEEPTIME = 1000;
	
	private final View OUTPUT_VIEW;
	
	private List<Event> eventList;
	private boolean enabled;
	
	public EventScheduler(View outputView) {
		OUTPUT_VIEW = outputView;
		
		this.eventList = new ArrayList<Event>();
		
		this.enabled = true;
	}
	
	public void cleanup() {
		this.enabled = false;
	}
	
	public void run() {
		while(isEnabled()) {
			try {
				for(int i = 0; i < this.eventList.size(); i++) {
					handleEvent(this.eventList.get(i));
				}
			} catch (Exception e) {
				Log.e("EventScheduler.java", "Unable to handle event", e);
			}
			
			try {
				Thread.sleep(SCHEDULER_SLEEPTIME);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	private boolean isEnabled() {
		return this.enabled;
	}
	
	private void handleEvent(Event event) {
		//TODO
	}
	
	private void showImage(Event event) {
		//TODO
	}
	
	private void playSound(Event event) {
		//TODO
	}
	
	private void turnRadioOn(Event event) {
		//TODO
	}
	
	private void showInformation(Event event) {
		//TODO
	}
}
