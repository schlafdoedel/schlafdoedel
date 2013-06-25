package com.kg6.schlafdoedel.custom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.kg6.schlafdoedel.event.Event;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class Util {
	private static final int NUM_WEEKDAYS = 7;
	
	public static String GetTimeOfDatePrintableFormat(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
		
		return format.format(date);
	}
	
	public static boolean IsEventToday(Event event) {
		final int weekDay = GetCurrentDayOfWeek();
		
		if(event.getStart() > GetCurrentTime() && event.isRepeatedOnWeekday(weekDay)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean IsDateTomorrow(Event event) {
		final int nextWeekDay = (GetCurrentDayOfWeek() + 1) % NUM_WEEKDAYS;
		
		return !IsEventToday(event) && event.isRepeatedOnWeekday(nextWeekDay);
	}
	
	public static String GetPrintableTimeOfDay(long timeOfDay) {
		Date date = new Date(Util.GetMillisecondsOfDay() + timeOfDay);
		
		return GetTimeOfDatePrintableFormat(date);
	}
	
	public static long GetMillisecondsOfDay() {
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		return calendar.getTimeInMillis();
	}
	
	public static long GetMillisecondsOfDay(int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		
		while(minute > 60) {
			minute -= 60;
			hour++;
		}
		
		hour = hour % 24;
		
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		
		return calendar.getTimeInMillis() - Util.GetMillisecondsOfDay();
	}
	
	public static long GetCurrentTime() {
		Calendar calendar = Calendar.getInstance();
		
		return calendar.getTimeInMillis() - GetMillisecondsOfDay();
	}
	
	public static int GetDayOffset() {
		return 1000 * 60 * 60 * 24;
	}
	
	public static int GetHourOffset() {
		return 1000 * 60 * 60;
	}
	
	public static int GetMinuteOffset() {
		return 1000 * 60;
	}
	
	public static int GetHourOfTimestamp(long timestamp) {
		return (int)(timestamp / GetHourOffset());
	}
	
	public static int GetMinuteOfTimestamp(long timestamp) {
		return (int)(timestamp / GetMinuteOffset()) - GetHourOfTimestamp(timestamp) * 60;
	}
	
	public static int GetNumberOfWeekdays() {
		return NUM_WEEKDAYS;
	}
	
	public static int GetCurrentDayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		
		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	public static int GetCurrentDayOfYear() {
		Calendar calendar = Calendar.getInstance();
		
		return calendar.get(Calendar.DAY_OF_YEAR);
	}
	
	public static String getWeekdayPrintname(int weekday) {
		switch(weekday) {
			case 0:
				return "Sunday";
			case 1:
				return "Monday";
			case 2:
				return "Tuesday";
			case 3:
				return "Wednesday";
			case 4:
				return "Thursday";
			case 5:
				return "Friday";
			case 6:
				return "Saturday";
		}
		
		return "unknown";
	}
	
	public static int GetDeviceWidth(Context context) {
		Point windowSize = new Point();
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getSize(windowSize);
		
		return windowSize.x;
	}
	
	public static String GetTrimmedText(String text, int maxTextLength) {
		if(text.length() < maxTextLength || maxTextLength <= 3) {
			return text;
		}
		
		String prefix = text.substring(0, maxTextLength / 2);
		String postfix = text.substring(text.length() - (maxTextLength / 2 + 3));
		
		return prefix + "..." + postfix;
	}
}
