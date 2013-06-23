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
		SLEEPING_PHASES_SIMULATION,
		CLOSE_APPLICATION
	};
	
	public static void Create(Menu menu, Context context) {
		menu.add(0, ContextMenuOptions.SLEEPING_PHASES_SIMULATION.ordinal(), ContextMenuOptions.SLEEPING_PHASES_SIMULATION.ordinal(), "Simulate sleeping phases");
		menu.add(0, ContextMenuOptions.CLOSE_APPLICATION.ordinal(), ContextMenuOptions.CLOSE_APPLICATION.ordinal(), "Close");
	}
	
	public static void ManageItemClick(MenuItem item, Activity context) {
		if(item.getItemId() == ContextMenuOptions.SLEEPING_PHASES_SIMULATION.ordinal()) {
			SleepingPhaseDialog dialog = new SleepingPhaseDialog(context);
			dialog.show();
		} else if(item.getItemId() == ContextMenuOptions.CLOSE_APPLICATION.ordinal()) {
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
}
