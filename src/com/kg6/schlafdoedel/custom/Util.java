package com.kg6.schlafdoedel.custom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class Util {
	public static String GetTimeOfDatePrintableFormat(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
		
		return format.format(date);
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
}
