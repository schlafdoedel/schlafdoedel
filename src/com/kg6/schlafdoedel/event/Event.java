package com.kg6.schlafdoedel.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.kg6.schlafdoedel.custom.Util;


public class Event {
	private static int IdCounter = 1;
	
	public static int GetNextEventId() {
		return IdCounter;
	}
	
	private final int ID;
	private String title;
	
	private long start;
	private long end;
	private int[] repetition;
	private List<EventSource> sourceList;
	
	private List<Integer> handledTimestampList;
	
	public Event(String title, long start, long end) {
		ID = IdCounter++;
		
		this.title = title;
		this.start = start;
		this.end = end;
		this.repetition = new int[Util.GetNumberOfWeekdays()];
		this.sourceList = new ArrayList<EventSource>();
		
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

	public List<EventSource> getEventSourceList() {
		return Collections.unmodifiableList(this.sourceList);
	}

	public void addEventSource(EventSource source) {
		this.sourceList.add(source);
	}
	
	public void removeEventSource(EventSource source) {
		this.sourceList.remove(source);
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

	public void setRepetition(int day, boolean enabled) {
		if(day < 0 || day >= this.repetition.length) {
			return;
		}
		
		if(enabled) {
			this.repetition[day] = 1;
		} else {
			this.repetition[day] = 0;
		}
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
		
		if(this.handledTimestampList.size() > Util.GetNumberOfWeekdays()) {
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
