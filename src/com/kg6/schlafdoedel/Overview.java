package com.kg6.schlafdoedel;

import java.util.Date;

import com.kg6.schlafdoedelmaster.R;
import com.kg6.schlafdoedel.custom.Util;
import com.kg6.schlafdoedel.network.BluetoothConnection;
import com.kg6.schlafdoedel.network.NetworkConnection;
import com.kg6.schlafdoedel.network.NetworkConnection.ConnectionType;
import com.kg6.schlafdoedel.network.NetworkEvent;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Overview extends Activity implements NetworkEvent {
	
	private LinearLayout defaultStatusPanelView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		addDefaultStatusEntry();
		
		BluetoothConnection connection = BluetoothConnection.CreateInstance(this);
		connection.addNetworkEventListener(this);
		connection.startListening();
	}

	private void addStatusEntry(String text) {
		final int dateColumnWidth = 100;
		final int dismissColumnWidth = 100;
		final int borderWidth = 60;
		final int margin = 20;
		final int textColumnWidth = Util.GetDeviceWidth(this) - dateColumnWidth - dismissColumnWidth - borderWidth;
		
		final LinearLayout statusPanel = (LinearLayout)findViewById(R.id.statusPanelContent);
		
		final LinearLayout statusPanelEntry = new LinearLayout(this);
		statusPanelEntry.setOrientation(LinearLayout.HORIZONTAL);
		
		LayoutParams statusPanelEntryParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		statusPanelEntryParams.topMargin = margin;
		statusPanelEntryParams.bottomMargin = margin;
		statusPanelEntryParams.leftMargin = margin;
		statusPanelEntryParams.rightMargin = margin;
		
		statusPanel.addView(statusPanelEntry, statusPanelEntryParams);
		
		//date
		TextView entryDateView = new TextView(this);
		entryDateView.setText(Util.GetTimeOfDatePrintableFormat(new Date(System.currentTimeMillis())));
		
		statusPanelEntry.addView(entryDateView, new LayoutParams(dateColumnWidth, LayoutParams.WRAP_CONTENT));
		
		//description
		TextView entryDescriptionView = new TextView(this);
		entryDescriptionView.setText(text);
		
		statusPanelEntry.addView(entryDescriptionView, new LayoutParams(textColumnWidth, LayoutParams.WRAP_CONTENT));
		
		//dismiss button
		Button dismissButton = new Button(this);
		dismissButton.setText("Dismiss");
		
		dismissButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				statusPanel.removeView(statusPanelEntry);
				
				if(statusPanel.getChildCount() == 0) {
					addDefaultStatusEntry();
				}
			}
		});
		
		statusPanelEntry.addView(dismissButton, new LayoutParams(dismissColumnWidth, LayoutParams.WRAP_CONTENT));
		
		removeDefaultStatusEntry();
	}
	
	private void addDefaultStatusEntry() {
		final int margin = 20;
		
		LinearLayout statusPanel = (LinearLayout)findViewById(R.id.statusPanelContent);
		
		this.defaultStatusPanelView = new LinearLayout(this);
		this.defaultStatusPanelView.setOrientation(LinearLayout.HORIZONTAL);
		
		LayoutParams statusPanelEntryParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		statusPanelEntryParams.topMargin = margin;
		statusPanelEntryParams.bottomMargin = margin;
		statusPanelEntryParams.leftMargin = margin;
		statusPanelEntryParams.rightMargin = margin;
		
		statusPanel.addView(this.defaultStatusPanelView, statusPanelEntryParams);
		
		TextView entryDescriptionView = new TextView(this);
		entryDescriptionView.setText("No recent activities");
		
		this.defaultStatusPanelView.addView(entryDescriptionView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	private void removeDefaultStatusEntry() {
		if(this.defaultStatusPanelView != null) {
			LinearLayout statusPanel = (LinearLayout)findViewById(R.id.statusPanelContent);
			statusPanel.removeView(this.defaultStatusPanelView);
		}
	}

	@Override
	public void onStartListening(ConnectionType connectionType) {
		addStatusEntry(String.format("Searching for nearby %s sensors", NetworkConnection.GetConnectionTypePrintname(connectionType)));
	}

	@Override
	public void onConnectionEstablished(ConnectionType connectionType) {
		addStatusEntry(String.format("Connected to %s sensor", NetworkConnection.GetConnectionTypePrintname(connectionType)));
	}
}
