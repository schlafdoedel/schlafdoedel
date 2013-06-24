package com.kg6.schlafdoedel.custom;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kg6.schlafdoedel.Configuration;
import com.kg6.schlafdoedel.R;
import com.kg6.schlafdoedel.event.Event;
import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedel.event.EventSource;
import com.kg6.schlafdoedel.event.EventSource.SourceType;

public class EventDefinitionDialog extends Dialog {
	private final int ROW_HEADER_WIDTH = 180;
	private final int SELECT_BUTTON_WIDTH = 150;
	private final int WHEEL_WIDTH = 100;
	private final int HOURS_PER_DAY = 24;
	private final int MINUTES_PER_HOUR = 60;
	
	private final Activity CONTEXT;
	private final EventScheduler EVENT_SCHEDULER;
	
	private EditText titleTextBox;
	private LinearLayout suggestionsLayout;
	
	private WheelView startWheelHours;
	private WheelView startWheelMinutes;
	
	private WheelView endWheelHours;
	private WheelView endWheelMinutes;
	
	private CheckBox[] repetitionCheckBoxes;
	private Spinner radioSourceSpinner;
	
	private EditText musicSourceTextBox;
	private EditText imageSourceTextBox;
	private EditText weatherSourceTextBox;
	
	private Button addEventButton;
	
	private Event predefinedEvent;
	
	public EventDefinitionDialog(Activity context, EventScheduler eventScheduler) {
		super(context, android.R.style.Theme_Black);
		
		CONTEXT = context;
		EVENT_SCHEDULER = eventScheduler;
		
		this.musicSourceTextBox = null;
		this.imageSourceTextBox = null;
		this.weatherSourceTextBox = null;
		
		this.predefinedEvent = null;

		initializeControls();
	}
	
	public void setMusicSource(String source) {
		this.musicSourceTextBox.setText(source);
	}
	
	public void setImageSource(String source) {
		this.imageSourceTextBox.setText(source);
	}
	
	public void setEvent(Event event) {
		this.predefinedEvent = event;
		
		updateControls(event);
	}
	
	private void initializeControls() {
		ScrollView scrollView = new ScrollView(getContext());
		addContentView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		LinearLayout contentContainerLayout = new LinearLayout(getContext());
		contentContainerLayout.setOrientation(LinearLayout.VERTICAL);
		
		scrollView.addView(contentContainerLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		LinearLayout contentLayout = new LinearLayout(getContext());
		contentLayout.setOrientation(LinearLayout.VERTICAL);
		
		LayoutParams contentLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		contentLayoutParams.topMargin = 10;
		contentLayoutParams.rightMargin = 10;
		contentLayoutParams.bottomMargin = 10;
		contentLayoutParams.leftMargin = 10;
		
		contentContainerLayout.addView(contentLayout, contentLayoutParams);
		
		//header
		TextView headerTextView = new TextView(CONTEXT);
		headerTextView.setText("Add a new event");
		headerTextView.setTextSize(18);
		headerTextView.setTextColor(Color.WHITE);
		
		contentLayout.addView(headerTextView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		//title
		this.titleTextBox = new EditText(CONTEXT);
		this.titleTextBox.setSingleLine();
		this.titleTextBox.setText(getDefaultEventTitle());
		
		addRow(contentLayout, "Event title", this.titleTextBox);
		
		//related dates from the calendar
		List<CalendarEvent> calendarDateList = getCalendarDates();
		
		if(calendarDateList.size() > 0) {
			LinearLayout relatedDatesLayout = new LinearLayout(getContext());
			relatedDatesLayout.setOrientation(LinearLayout.VERTICAL);
			
			this.suggestionsLayout = addRow(contentLayout, "Suggestions", relatedDatesLayout);
			
			for(CalendarEvent event : calendarDateList) {
				addRow(relatedDatesLayout, event);
			}
		}
		
		//start
		final int[] expectedWakeupTime = getNextWakeUpTime();
		
		LinearLayout startWheelLayout = new LinearLayout(CONTEXT);
		startWheelLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		this.startWheelHours = new WheelView(CONTEXT);
		this.startWheelHours.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, HOURS_PER_DAY - 1));
		this.startWheelHours.setCurrentItem(expectedWakeupTime[0]);
		
		startWheelLayout.addView(this.startWheelHours, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
		
		this.startWheelMinutes = new WheelView(CONTEXT);
		this.startWheelMinutes.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, MINUTES_PER_HOUR - 1, "%02d"));
		this.startWheelMinutes.setCurrentItem(expectedWakeupTime[1]);
		this.startWheelMinutes.setCyclic(true);
		
		startWheelLayout.addView(this.startWheelMinutes, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
		
		addRow(contentLayout, "Wake up start time", startWheelLayout);
		
		//end
		int endTimeHour = expectedWakeupTime[0] + 1;
		
		if(endTimeHour >= HOURS_PER_DAY) {
			endTimeHour = expectedWakeupTime[0];
		}
		
		LinearLayout endWheelLayout = new LinearLayout(CONTEXT);
		endWheelLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		this.endWheelHours = new WheelView(CONTEXT);
		this.endWheelHours.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, HOURS_PER_DAY - 1));
		this.endWheelHours.setCurrentItem(endTimeHour);
		
		endWheelLayout.addView(this.endWheelHours, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
		
		this.endWheelMinutes = new WheelView(CONTEXT);
		this.endWheelMinutes.setViewAdapter(new NumericWheelAdapter(CONTEXT, 0, MINUTES_PER_HOUR - 1, "%02d"));
		this.endWheelMinutes.setCurrentItem(expectedWakeupTime[1]);
		this.endWheelMinutes.setCyclic(true);
		
		endWheelLayout.addView(this.endWheelMinutes, new LayoutParams(WHEEL_WIDTH, LayoutParams.WRAP_CONTENT));
		
		addRow(contentLayout, "Wake up end time", endWheelLayout);
		
		//repetition
		final boolean[] expectedRepetition = getExpectedRepetition();
		
		this.repetitionCheckBoxes = new CheckBox[Util.GetNumberOfWeekdays()];
		
		LinearLayout repetitionLayout = new LinearLayout(CONTEXT);
		repetitionLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		LinearLayout repetitionLeftLayout = new LinearLayout(CONTEXT);
		repetitionLeftLayout.setOrientation(LinearLayout.VERTICAL);
		repetitionLayout.addView(repetitionLeftLayout);
		
		for(int i = 1; i <= this.repetitionCheckBoxes.length; i++) {
			final int index = i % this.repetitionCheckBoxes.length;
			
			CheckBox checkBox = new CheckBox(CONTEXT);
			checkBox.setText(Util.getWeekdayPrintname(index));
			checkBox.setChecked(expectedRepetition[index]);
			
			repetitionLeftLayout.addView(checkBox);
			
			this.repetitionCheckBoxes[index] = checkBox;
		}
		
		LinearLayout repetitionRightLayout = new LinearLayout(CONTEXT);
		repetitionRightLayout.setOrientation(LinearLayout.VERTICAL);
		repetitionLayout.addView(repetitionRightLayout);
		
		LinearLayout allWorkdaysLayout = new LinearLayout(CONTEXT);
		allWorkdaysLayout.setOrientation(LinearLayout.HORIZONTAL);
		repetitionRightLayout.addView(allWorkdaysLayout);
		
		final ImageButton allWorkdaysBracketButton = new ImageButton(CONTEXT);		
		allWorkdaysBracketButton.setBackground(CONTEXT.getResources().getDrawable(R.drawable.bracket));
		
		LayoutParams allWorkdaysBracketButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		allWorkdaysBracketButtonParams.topMargin = 20;
		allWorkdaysBracketButtonParams.leftMargin = 50;
		
		allWorkdaysLayout.addView(allWorkdaysBracketButton, allWorkdaysBracketButtonParams);
		
		final Button allWorkdaysButton = new Button(CONTEXT);
		allWorkdaysButton.setText("All workdays");
		
		allWorkdaysButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for(int i = 1; i <= 5; i++) {
					repetitionCheckBoxes[i].setChecked(true);
				}
			}
		});
		
		LayoutParams allWorkdaysButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		allWorkdaysButtonParams.topMargin = 130;
		allWorkdaysButtonParams.leftMargin = 30;
		
		allWorkdaysLayout.addView(allWorkdaysButton, allWorkdaysButtonParams);
		
		addRow(contentLayout, "Repetition", repetitionLayout);
		
		//radio event type
		final List<Pair<String, String>> internetRadioList = getInternetRadioList();
		
		List<String> spinnerEntries = new ArrayList<String>();
		spinnerEntries.add("No internet radio selected");
		
		for(Pair<String, String> entry : internetRadioList) {
			spinnerEntries.add(entry.first);
		}
		
	    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(CONTEXT, android.R.layout.simple_spinner_dropdown_item, spinnerEntries);
	    
	    this.radioSourceSpinner = new Spinner(CONTEXT);
	    this.radioSourceSpinner.setAdapter(spinnerArrayAdapter);
		
	    addRow(contentLayout, "Play internet radio", this.radioSourceSpinner);
		
		//music event type
		LinearLayout musicSourceLayout = new LinearLayout(CONTEXT);
		musicSourceLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		this.musicSourceTextBox = new EditText(CONTEXT);
		this.musicSourceTextBox.setSingleLine();
		
		musicSourceLayout.addView(this.musicSourceTextBox, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - ROW_HEADER_WIDTH - SELECT_BUTTON_WIDTH - 10, LayoutParams.WRAP_CONTENT));
		
		final Button musicSourceButton = new Button(CONTEXT);
		musicSourceButton.setText("Select");
		
		musicSourceButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
			    intent.setType("*/*"); 
			    intent.addCategory(Intent.CATEGORY_OPENABLE);

			    try {
			        CONTEXT.startActivityForResult(Intent.createChooser(intent, "Select a music source"), Configuration.FILE_CHOOSER_MUSIC_RESPONSE_CODE);
			    } catch (android.content.ActivityNotFoundException ex) {
			        Toast.makeText(CONTEXT, "You need to install a file manager in order to use this functionality.", Toast.LENGTH_SHORT).show();
			    }
			}
		});
		
		musicSourceLayout.addView(musicSourceButton, new LayoutParams(SELECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
		
		addRow(contentLayout, "Play music from", musicSourceLayout);
		
		//image event type
		LinearLayout imageSourceLayout = new LinearLayout(CONTEXT);
		imageSourceLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		this.imageSourceTextBox = new EditText(CONTEXT);
		this.imageSourceTextBox.setSingleLine();
		
		imageSourceLayout.addView(this.imageSourceTextBox, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - ROW_HEADER_WIDTH - SELECT_BUTTON_WIDTH - 10, LayoutParams.WRAP_CONTENT));
		
		final Button imageSourceButton = new Button(CONTEXT);
		imageSourceButton.setText("Select");
		
		imageSourceButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
			    intent.setType("*/*");
			    intent.addCategory(Intent.CATEGORY_OPENABLE);

			    try {
			        CONTEXT.startActivityForResult(Intent.createChooser(intent, "Select a music source"), Configuration.FILE_CHOOSER_IMAGE_RESPONSE_CODE);
			    } catch (android.content.ActivityNotFoundException ex) {
			        Toast.makeText(CONTEXT, "You need to install a file manager in order to use this functionality.", Toast.LENGTH_SHORT).show();
			    }
			}
		});
		
		imageSourceLayout.addView(imageSourceButton, new LayoutParams(SELECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
		
		addRow(contentLayout, "Show image from", imageSourceLayout);
		
		//weather event type
		LinearLayout weatherSourceLayout = new LinearLayout(CONTEXT);
		weatherSourceLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		this.weatherSourceTextBox = new EditText(CONTEXT);
		this.weatherSourceTextBox.setSingleLine();
		
		weatherSourceLayout.addView(this.weatherSourceTextBox, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - ROW_HEADER_WIDTH - SELECT_BUTTON_WIDTH - 10, LayoutParams.WRAP_CONTENT));
		
		addRow(contentLayout, "Get weather for", weatherSourceLayout);
		
		
		//add button
		this.addEventButton = new Button(CONTEXT);
		this.addEventButton.setText("Add event");
		
		this.addEventButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String eventTitle = titleTextBox.getText().toString();
				String eventMusicSource = musicSourceTextBox.getText().toString();
				String eventImageSource = imageSourceTextBox.getText().toString();
				String eventWeatherSource = weatherSourceTextBox.getText().toString();
				
				if(eventTitle.trim().length() == 0) {
					eventTitle = getDefaultEventTitle();
				}
				
				if(!new File(eventMusicSource).exists()) {
					eventMusicSource = "";
				}
				
				if(!new File(eventImageSource).exists()) {
					eventImageSource = "";
				}
				
				long start = Util.GetMillisecondsOfDay(startWheelHours.getCurrentItem(), startWheelMinutes.getCurrentItem(), 0);
				long end = Util.GetMillisecondsOfDay(endWheelHours.getCurrentItem(), endWheelMinutes.getCurrentItem(), 0);
				
				if(end < start) {
					end = start;
				}
				
				//Create the event
				Event event = null;
				
				if(predefinedEvent == null) {
					event = new Event(eventTitle, start, end);
				} else {
					event = predefinedEvent;
					
					event.setTitle(eventTitle);
					event.setStart(start);
					event.setEnd(end);
					
					event.clearEventSources();
				}
				
				//Add repetition entries
				for(int i = 0; i < repetitionCheckBoxes.length; i++) {
					event.setRepetition(i, repetitionCheckBoxes[i].isChecked());
				}
				
				//Add event sources
				final String selectedInternetRadio = radioSourceSpinner.getSelectedItem().toString();
				
				for(Pair<String, String> entry : internetRadioList) {
					if(entry.first.compareTo(selectedInternetRadio) == 0) {
						event.addEventSource(new EventSource(SourceType.Music, entry.second));
					}
				}
				
				if(eventMusicSource.length() > 0) {
					event.addEventSource(new EventSource(SourceType.Music, eventMusicSource));
				}
				
				if(eventMusicSource.length() > 0) {
					event.addEventSource(new EventSource(SourceType.Image, eventImageSource));
				}
				
				if(eventWeatherSource.length() > 0) {
					event.addEventSource(new EventSource(SourceType.Weather, eventWeatherSource));
				}
				
				//Add the event to the scheduler
				if(predefinedEvent == null) {
					EVENT_SCHEDULER.addEvent(event);
				} else {
					EVENT_SCHEDULER.notifyEventListenersForChangedEvents();
				}
				
				dismiss();
			}
		});
		
		contentLayout.addView(this.addEventButton, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		//Make sure that the end time is always > than the start time
		OnWheelChangedListener wheelChangeListener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				final int startHour = startWheelHours.getCurrentItem();
				final int startMinute = startWheelMinutes.getCurrentItem();
				
				final int endHour = endWheelHours.getCurrentItem();
				final int endMinute = endWheelMinutes.getCurrentItem();
				
				if(startWheelHours.getCurrentItem() > endWheelHours.getCurrentItem()) {
					endWheelHours.setCurrentItem(startHour);
				}
				
				if(startMinute > endMinute && startHour == endHour) {
					endWheelMinutes.setCurrentItem(startMinute);
				}
			}
		};
		
		this.startWheelHours.addChangingListener(wheelChangeListener);
		this.startWheelMinutes.addChangingListener(wheelChangeListener);
		
		this.endWheelHours.addChangingListener(wheelChangeListener);
		this.endWheelMinutes.addChangingListener(wheelChangeListener);
	}
	
	private void updateControls(Event event) {
		this.titleTextBox.setText(event.getTitle());
		
		this.startWheelHours.setCurrentItem(Util.GetHourOfTimestamp(event.getStart()));
		this.startWheelMinutes.setCurrentItem(Util.GetMinuteOfTimestamp(event.getStart()));
		
		this.endWheelHours.setCurrentItem(Util.GetHourOfTimestamp(event.getEnd()));
		this.endWheelMinutes.setCurrentItem(Util.GetMinuteOfTimestamp(event.getEnd()));
		
		//selected repetition
		final boolean[] repetition = event.getRepetition();
		
		for(int i = 0; i < this.repetitionCheckBoxes.length; i++) {
			repetitionCheckBoxes[i].setChecked(repetition[i]);
		}
		
		//selected radio station
		final SpinnerAdapter spinnerAdapter = this.radioSourceSpinner.getAdapter();
		
		List<EventSource> handledEventSourceList = new ArrayList<EventSource>();
		
		for(int i = 0; i < spinnerAdapter.getCount(); i++) {
			final String entryName = spinnerAdapter.getItem(i).toString();
			final String entryUrl = getInternetRadioUrl(entryName);
			
			boolean elementSelected = false;
			
			for(EventSource source : event.getEventSourceList()) {
				if(source.getSourceType() == SourceType.Music && entryUrl.compareTo(source.getUrl()) == 0) {
					this.radioSourceSpinner.setSelection(i);
					
					handledEventSourceList.add(source);
					
					elementSelected = true;
					
					break;
				}
			}
			
			if(elementSelected) {
				break;
			}
		}
		
		//selected event source
		for(EventSource source : event.getEventSourceList()) {
			if(handledEventSourceList.contains(source)) {
				continue;
			}
			
			if(source.getSourceType() == SourceType.Music) {
				this.musicSourceTextBox.setText(source.getUrl());
			} else if(source.getSourceType() == SourceType.Image) {
				this.imageSourceTextBox.setText(source.getUrl());
			}
		}
		
		//hide the suggestions layout
		if(this.suggestionsLayout != null) {
			this.suggestionsLayout.setVisibility(View.GONE);
		}
		
		this.addEventButton.setText("Update event");
	}
	
	private LinearLayout addRow(LinearLayout container, String text, View control) {
		TextView textView = new TextView(CONTEXT);
		textView.setText(text);
		
		LinearLayout rowLayout = new LinearLayout(CONTEXT);
		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		rowLayout.addView(textView, new LayoutParams(ROW_HEADER_WIDTH, LayoutParams.WRAP_CONTENT));
		rowLayout.addView(control, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		LayoutParams entryLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		entryLayout.topMargin = 10;
		entryLayout.bottomMargin = 10;
		
		container.addView(rowLayout, entryLayout);
		
		return rowLayout;
	}
	
	private void addRow(LinearLayout container, final CalendarEvent calendarEvent) {
		Button addCalendarEventButton = new Button(CONTEXT);
		addCalendarEventButton.setText(String.format("Get up before %s: %s", Util.GetPrintableTimeOfDay(calendarEvent.getStartTime()), calendarEvent.getTitle()));
		
		container.addView(addCalendarEventButton, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		addCalendarEventButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final long startTime = calendarEvent.getStartTime() - Configuration.EVENT_AUTODEFINITION_START_OFFSET;
				final long endTime = startTime + Configuration.EVENT_AUTODEFINITION_END_OFFSET;
				
				final int currentDayOfWeek = Util.GetCurrentDayOfWeek();
				
				Event event = new Event(String.format("Get up for: %s", calendarEvent.getTitle()), startTime, endTime);
				event.setRepetition(currentDayOfWeek, true);
				event.addEventSource(Configuration.EVENT_AUTODEFINITION_DEFAULT_SOURCE);
				
				updateControls(event);
			}
		});
	}
	
	private String getDefaultEventTitle() {
		return String.format("New event %s", Event.GetNextEventId());
	}
	
	private List<Pair<String, String>> getInternetRadioList() {
		List<Pair<String, String>> internetRadioList = new ArrayList<Pair<String, String>>();
		internetRadioList.add(new Pair<String, String>("Antenne 1 (pop)", "http://stream.antenne1.de/stream1/livestream.mp3"));
		internetRadioList.add(new Pair<String, String>("Kronehit (pop)", "http://onair.krone.at/kronehit.mp3"));
		internetRadioList.add(new Pair<String, String>("Klassik radio (classic)", "http://edge.live.mp3.mdn.newmedia.nacamar.net/klassikradio128/livestream.mp3"));
		
		return internetRadioList;
	}
	
	private String getInternetRadioUrl(String name) {
		final List<Pair<String, String>> internetRadioList = getInternetRadioList();
		
		for(Pair<String, String> entry : internetRadioList) {
			String entryName = entry.first;
			
			if(entryName.compareTo(name) == 0) {
				return entry.second;
			}
		}
		
		return "";
	}

	private int[] getNextWakeUpTime() {
		Calendar calendar = Calendar.getInstance();
		
		//TODO: Access the kalendar to get next events
		
		return new int[] {
			calendar.get(Calendar.HOUR_OF_DAY) + 1,
			calendar.get(Calendar.MINUTE),
		};
	}
	
	private boolean[] getExpectedRepetition() {
		//TODO: Access the kalendar to get next events
		
		boolean[] expectedRepetition = new boolean[Util.GetNumberOfWeekdays()];
		
		for(int i = 0; i < expectedRepetition.length; i++) {
			expectedRepetition[i] = true;
		}
		
		return expectedRepetition;
	}
	
	private List<CalendarEvent> getCalendarDates() {
		List<CalendarEvent> resultList = new ArrayList<CalendarEvent>();
		
		Cursor cursor = null;
		
		try {
			final long dayOffset = Util.GetDayOffset();
			final long minTimestamp = Util.GetMillisecondsOfDay() + dayOffset;
			final long maxTimestamp = minTimestamp + dayOffset;
			
			final String[] columns = new String[] { 
				CalendarContract.Events.TITLE, 
				CalendarContract.Events.DTSTART 
			};
			
			ContentResolver contentResolver = CONTEXT.getContentResolver();
			
			cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI, columns, null, null, null); 
			cursor.moveToFirst();
			
			while(cursor.moveToNext()) {
				final long startTimestamp = cursor.getLong(1);
				
				if(startTimestamp >= minTimestamp && startTimestamp < maxTimestamp) {
					resultList.add(new CalendarEvent(cursor.getString(0), startTimestamp - Util.GetMillisecondsOfDay()));
				}
			}
		} catch (Exception e) {
			Log.e("EventDefinitionDialog.java", "Unable to get calendar dates", e);
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				
			}
		}
		
		return resultList;
	}
	
	private class CalendarEvent {
		private String title;
		private long startTime;
		
		public CalendarEvent(String title, long startTime) {
			this.title = title;
			this.startTime = startTime;
		}

		public String getTitle() {
			return title;
		}

		public long getStartTime() {
			return startTime;
		}
	}
}
