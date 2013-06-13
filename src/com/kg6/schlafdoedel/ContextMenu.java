package com.kg6.schlafdoedel;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import com.kg6.schlafdoedel.custom.SleepingPhaseDialog;

public class ContextMenu {
	
	private enum ContextMenuOptions {
		SLEEPING_PHASES_SIMULATION
	};
	
	public static void Create(Menu menu, Context context) {
		menu.add(0, ContextMenuOptions.SLEEPING_PHASES_SIMULATION.ordinal(), ContextMenuOptions.SLEEPING_PHASES_SIMULATION.ordinal(), "Simulate sleeping phases");
	}
	
	public static void ManageItemClick(MenuItem item, Activity context) {
		if(item.getItemId() == ContextMenuOptions.SLEEPING_PHASES_SIMULATION.ordinal()) {
			SleepingPhaseDialog dialog = new SleepingPhaseDialog(context);
			dialog.show();
		}
	}
}
