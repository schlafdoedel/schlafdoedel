package com.kg6.schlafdoedel.custom;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.kg6.schlafdoedel.Configuration;
import com.kg6.schlafdoedel.network.BluetoothConnection;

public class SleepingPhaseDialog extends Dialog {
	private final int DIALOG_MARGIN = 20;
	private final int CONNECT_BUTTON_WIDTH = 200;
	
	private final Activity CONTEXT;
	
	private LinearLayout contentLayout;

	public SleepingPhaseDialog(Activity context) {
		super(context);
		
		CONTEXT = context;
		
		initializeControls();
	}
	
	private void initializeControls() {
		ScrollView scrollView = new ScrollView(getContext());
		addContentView(scrollView, new LayoutParams(Util.GetDeviceWidth(getContext()) - 2 * DIALOG_MARGIN, LayoutParams.WRAP_CONTENT));
		
		this.contentLayout = new LinearLayout(getContext());
		this.contentLayout.setOrientation(LinearLayout.VERTICAL);
		
		scrollView.addView(this.contentLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		//title
		setTitle("Simulate sleeping phases");
		
		//sleeping phase panel
		LinearLayout entryLayout = new LinearLayout(getContext());
		entryLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		LayoutParams entryLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		entryLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
		
		this.contentLayout.addView(entryLayout, entryLayoutParams);
		
		//awake button
		Button awakeButton = new Button(getContext());
		awakeButton.setText("Awake");
		
		awakeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BluetoothConnection connection = BluetoothConnection.CreateInstance(CONTEXT);
				connection.sendCommand(Configuration.COMMAND_SLEEPING_PHASE_AWAKE);
			}
		});
		
		entryLayout.addView(awakeButton, new LayoutParams(CONNECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
		
		//deep sleep button
		Button deepSleepingPhaseButton = new Button(getContext());
		deepSleepingPhaseButton.setText("Deep sleep");
		
		deepSleepingPhaseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BluetoothConnection connection = BluetoothConnection.CreateInstance(CONTEXT);
				connection.sendCommand(Configuration.COMMAND_SLEEPING_PHASE_DEEP);
			}
		});
		
		entryLayout.addView(deepSleepingPhaseButton, new LayoutParams(CONNECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
		
		//shallow sleep button
		Button shallowSleepingPhaseButton = new Button(getContext());
		shallowSleepingPhaseButton.setText("Shallow sleep");
		
		shallowSleepingPhaseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BluetoothConnection connection = BluetoothConnection.CreateInstance(CONTEXT);
				connection.sendCommand(Configuration.COMMAND_SLEEPING_PHASE_SHALLOW);
			}
		});
		
		entryLayout.addView(shallowSleepingPhaseButton, new LayoutParams(CONNECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
	}
	
}
