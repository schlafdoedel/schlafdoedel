package com.kg6.schlafdoedel.custom;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kg6.schlafdoedel.event.Event;
import com.kg6.schlafdoedel.event.EventNotification;
import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedel.event.EventSource;
import com.kg6.schlafdoedel.event.EventSource.SourceType;

public class EventListPanel extends LinearLayout implements EventNotification {
	private final int VIEW_DISMISS_COLUMN_WIDTH = 160;
	private final int VIEW_COLUMN_MARGIN = 20;
	private final int VIEW_SUBTITLE_LENGTH = 75;
	
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
			addEventEntry("No event defined", null, null);
		} else {
			for(Event event : eventList) {
				String subtitle = "";
				
				for(EventSource eventSource : event.getEventSourceList()) {
					if(subtitle.length() > 0) {
						subtitle += "\n";
					}
					
					if(eventSource.getSourceType() == SourceType.Music) {
						subtitle += String.format("Music: %s", Util.GetTrimmedText(eventSource.getUrl(), VIEW_SUBTITLE_LENGTH));
					} else {
						subtitle += String.format("Image: %s", Util.GetTrimmedText(eventSource.getUrl(), VIEW_SUBTITLE_LENGTH));
					}
				}
				
				addEventEntry(String.format("%s - %s: %s", Util.GetPrintableTimeOfDay(event.getStart()), Util.GetPrintableTimeOfDay(event.getEnd()), event.getTitle()), subtitle, event);
			}
		}
	}

	private void addEventEntry(String title, String subtitle, final Event event) {
		LinearLayout entryLayout = new LinearLayout(CONTEXT);
		entryLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		addView(entryLayout, getStatusEntryParams());
		
		LinearLayout textLayout = new LinearLayout(CONTEXT);
		textLayout.setOrientation(LinearLayout.VERTICAL);
		
		entryLayout.addView(textLayout, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - VIEW_DISMISS_COLUMN_WIDTH - 2 * VIEW_COLUMN_MARGIN, LayoutParams.WRAP_CONTENT));
		
		TextView titleTextView = new TextView(CONTEXT);
		titleTextView.setTextColor(Color.WHITE);
		titleTextView.setTextSize(13);
		titleTextView.setText(title);
		
		textLayout.addView(titleTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		if(subtitle != null && subtitle.length() > 0) {
			TextView subtitleTextView = new TextView(CONTEXT);
			subtitleTextView.setTextSize(10);
			subtitleTextView.setText(subtitle);
			
			textLayout.addView(subtitleTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
		
		if(event != null) {
			Button deleteButton = new Button(CONTEXT);
			deleteButton.setText("Delete");
			
			deleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AlertDialog dialog = new AlertDialog.Builder(CONTEXT).create();
					dialog.setCancelable(false);
					dialog.setMessage("Are you sure you want to delete this event?");
					
					dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							eventScheduler.removeEvent(event);
							
							dialog.dismiss();
						}
					});
					
					dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					
					dialog.show();
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
