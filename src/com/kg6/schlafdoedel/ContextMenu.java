package com.kg6.schlafdoedel;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.kg6.schlafdoedel.custom.SleepingPhaseDialog;
import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedel.network.BluetoothConnection;

public class ContextMenu {
	
	private enum ContextMenuOptions {
		SIMULATE_SLEEPING_PHASES,
		SIMULATE_WEATHER_REQUEST,
		SIMULATE_NEWS_REQUEST,
		CLOSE_APPLICATION
	};
	
	public static void Create(Menu menu, Context context) {
		menu.add(0, ContextMenuOptions.SIMULATE_SLEEPING_PHASES.ordinal(), ContextMenuOptions.SIMULATE_SLEEPING_PHASES.ordinal(), "Simulate sleeping phases");
		menu.add(0, ContextMenuOptions.SIMULATE_WEATHER_REQUEST.ordinal(), ContextMenuOptions.SIMULATE_WEATHER_REQUEST.ordinal(), "Simulate weather request");
		menu.add(0, ContextMenuOptions.SIMULATE_NEWS_REQUEST.ordinal(), ContextMenuOptions.SIMULATE_NEWS_REQUEST.ordinal(), "Simulate news request");
		menu.add(0, ContextMenuOptions.CLOSE_APPLICATION.ordinal(), ContextMenuOptions.CLOSE_APPLICATION.ordinal(), "Close");
	}
	
	public static void ManageItemClick(MenuItem item, Activity context) {
		ContextMenuOptions option = ContextMenuOptions.values()[item.getItemId()];
		
		switch(option) {
			case SIMULATE_SLEEPING_PHASES:
				simulateSleepingPhases(context);
				
				break;
			case SIMULATE_WEATHER_REQUEST:
				simulateWeatherRequest();
				
				break;
			case SIMULATE_NEWS_REQUEST:
				simulateNewsRequest();
				
				break;
			case CLOSE_APPLICATION:
				closeApplication(context);
				
				break;
		}
	}
	
	private static void simulateSleepingPhases(Activity context) {
		SleepingPhaseDialog dialog = new SleepingPhaseDialog(context);
		
		dialog.show();
	}
	
	private static void simulateWeatherRequest() {
		//TODO
	}
	
	private static void simulateNewsRequest() {
		//TODO
	}
	
	private static void closeApplication(Activity context) {
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
		
		System.exit(0);
	}
}
