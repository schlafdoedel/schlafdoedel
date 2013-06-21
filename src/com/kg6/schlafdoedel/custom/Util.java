package com.kg6.schlafdoedel.custom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class Util {
	private static final int NUM_WEEKDAYS = 7;
	
	public static String GetTimeOfDatePrintableFormat(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
		
		return format.format(date);
	}
	
	public static String GetPrintableTimeOfDay(long timeOfDay) {
		Date date = new Date(Util.GetMillisecondsOfDay() + timeOfDay);
		
		return GetTimeOfDatePrintableFormat(date);
	}
	
	public static int GetDeviceWidth(Context context) {
		Point windowSize = new Point();
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getSize(windowSize);
		
		return windowSize.x;
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
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		
		return calendar.getTimeInMillis() - Util.GetMillisecondsOfDay();
	}
	
	public static int GetNumberOfWeekdays() {
		return NUM_WEEKDAYS;
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
}
