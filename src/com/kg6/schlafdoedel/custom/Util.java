package com.kg6.schlafdoedel.custom;

import java.text.SimpleDateFormat;
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
}
