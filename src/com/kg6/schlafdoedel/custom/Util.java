package com.kg6.schlafdoedel.custom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.kg6.schlafdoedel.Configuration;
import com.kg6.schlafdoedel.R;
import com.kg6.schlafdoedel.event.Event;
import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedel.network.BluetoothConnection;
import com.kg6.schlafdoedel.speechrecognition.SpeechRecognition;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
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
	
	public static void AddDeviceNotificationEntry(Activity context) {
		try {
			Intent intent = new Intent(context, context.getClass());
	    	
	    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	        
	        Notification.Builder notificationBuilder = new Notification.Builder(context);
	        notificationBuilder.setContentTitle("Schlafdödel: NSA uplink enabled");
		    notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
		    notificationBuilder.setContentIntent(pendingIntent);
		    notificationBuilder.setOngoing(true);
		    
		    Notification notification = notificationBuilder.build();
		    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); 
		    notificationManager.notify(Configuration.NOTIFICATION_ID, notification);
		} catch (Exception e) {
			Log.e("Util.java", "Unable to create device notification", e);
		}
	}
	
	public static void RemoveDeviceNotificationEntry(Activity context) {
		try {
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); 
		    notificationManager.cancel(Configuration.NOTIFICATION_ID);
		} catch (Exception e) {
			Log.e("Util.java", "Unable to remove device notification", e);
		}
	}
	
	public static void CleanupApplication(Activity context) {
		context.stopService(new Intent(context, SpeechRecognition.class));
    	
    	Util.RemoveDeviceNotificationEntry(context);
		
		try {
			BluetoothConnection bluetoothConnection = BluetoothConnection.CreateInstance(context);
			
			bluetoothConnection.cleanup();
		} catch (Exception e) {
			Log.e("ContextMenu.java", "Unable to cleanup the Bluetooth connection", e);
		}
		
		try {
			EventScheduler eventScheduler = EventScheduler.CreateInstance(context, null);
			
			eventScheduler.cleanup();
		} catch (Exception e) {
			Log.e("ContextMenu.java", "Unable to cleanup the EventScheduler", e);
		}
	}
}
