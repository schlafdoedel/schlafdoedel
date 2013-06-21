package com.kg6.schlafdoedel.custom;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kg6.schlafdoedel.event.Event;
import com.kg6.schlafdoedel.event.EventNotification;
import com.kg6.schlafdoedel.event.EventScheduler;

public class EventListPanel extends LinearLayout implements EventNotification {
	private final int VIEW_DISMISS_COLUMN_WIDTH = 160;
	private final int VIEW_COLUMN_MARGIN = 20;
	
	private final Activity CONTEXT;
	
	private EventScheduler eventScheduler;

	public EventListPanel(Activity context) {
		super(context);
		
		CONTEXT = context;
		
		setOrientation(LinearLayout.VERTICAL);
		
		initializeControls();
	}
	
	public void setEventScheduler(EventScheduler eventScheduler) {
		this.eventScheduler = eventScheduler;
		
		initializeControls();
	}
	
	private void initializeControls() {
		if(this.eventScheduler != null) {
			updateEventList(this.eventScheduler.getEventList());
		}
	}
	
	private void updateEventList(List<Event> eventList) {
		removeAllViews();
		
		if(eventList.size() == 0) {
			addEventEntry("No event defined", null);
		} else {
			for(Event event : eventList) {
				addEventEntry(String.format("%s - %s: %s", Util.GetPrintableTimeOfDay(event.getStart()), Util.GetPrintableTimeOfDay(event.getEnd()), event.getTitle()), event);
			}
		}
	}

	private void addEventEntry(String text, final Event event) {
		LinearLayout entryLayout = new LinearLayout(CONTEXT);
		entryLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		addView(entryLayout, getStatusEntryParams());
		
		TextView textView = new TextView(CONTEXT);
		textView.setText(text);
		
		entryLayout.addView(textView, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - VIEW_DISMISS_COLUMN_WIDTH - 2 * VIEW_COLUMN_MARGIN, LayoutParams.WRAP_CONTENT));
		
		if(event != null) {
			Button deleteButton = new Button(CONTEXT);
			deleteButton.setText("Delete");
			
			deleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					eventScheduler.removeEvent(event);
				}
			});
			
			entryLayout.addView(deleteButton, new LayoutParams(VIEW_DISMISS_COLUMN_WIDTH, LayoutParams.WRAP_CONTENT));
		}
	}
	
	private LayoutParams getStatusEntryParams() {
		LayoutParams entryParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		entryParams.topMargin = 10;
		entryParams.rightMargin = 10;
		entryParams.leftMargin = 10;
		
		return entryParams;
	}

	@Override
	public void onEventListChanged() {
		updateEventList(this.eventScheduler.getEventList());
	}

	@Override
	public void onEventRaised(Event event) {
		//not needed here
	}

	@Override
	public void onEventDismissed(Event event) {
		//not needed here
	}

	@Override
	public void onEventError(Event event, String error) {
		//not needed here
	}
}
