package com.kg6.schlafdoedel.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Event {
	private static int IdCounter = 1;
	
	public enum EventType {
		Music,
		Radio,
		Image,
	}
	
	private final int NUM_WEEKDAYS = 7;
	
	private final int ID;
	private String title;
	private EventType type;
	
	private long start;
	private long end;
	private int[] repetition;
	private EventSource source;
	
	private List<Integer> handledTimestampList;
	
	public Event(String title, EventType type, long start, long end, int[] repetition, EventSource source) {
		ID = IdCounter++;
		
		this.title = title;
		this.type = type;
		this.start = start;
		this.end = end;
		this.repetition = repetition;
		this.source = source;
		
		this.handledTimestampList = new ArrayList<Integer>();
	}

	public int getId() {
		return ID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public int[] getRepetition() {
		return repetition;
	}

	public void setRepetition(int[] repetition) {
		this.repetition = repetition;
	}

	public EventSource getSource() {
		return source;
	}

	public void setSource(EventSource source) {
		this.source = source;
	}
	
	public boolean handle() {
		Calendar calendar = Calendar.getInstance();
		final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
		
		//Check whether the repetition is enabled for the current day
		if(this.repetition.length > dayOfWeek && this.repetition[dayOfWeek] == 0) {
			return false;
		}
		
		//Check whether the event was already handled this day
		if(this.handledTimestampList.contains(dayOfYear)) {
			return false;
		}
		
		//Remember that the event was handled and make sure the list will be cleaned up after some time
		this.handledTimestampList.add(dayOfYear);
		
		if(this.handledTimestampList.size() > NUM_WEEKDAYS) {
			this.handledTimestampList.remove(0);
		}
		
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Event)) {
			return false;
		}
		
		return ((Event)o).getId() == getId();
	}
}
