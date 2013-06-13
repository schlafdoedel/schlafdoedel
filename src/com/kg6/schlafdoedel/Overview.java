package com.kg6.schlafdoedel;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kg6.schlafdoedel.custom.Util;
import com.kg6.schlafdoedel.network.BluetoothConnection;
import com.kg6.schlafdoedel.network.NetworkConnection;
import com.kg6.schlafdoedel.network.NetworkConnection.ConnectionType;
import com.kg6.schlafdoedel.network.NetworkEvent;
import com.kg6.schlafdoedelmaster.R;

public class Overview extends Activity implements NetworkEvent {
	private BluetoothConnection bluetoothConnection;
	
	private LinearLayout defaultStatusPanelView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		addOptionsButtonListener();
		addBluetoothButtonListener();
		
		addDefaultStatusEntry();
		
		this.bluetoothConnection = BluetoothConnection.CreateInstance(this);
		this.bluetoothConnection.addNetworkEventListener(this);
		this.bluetoothConnection.startServer();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	ContextMenu.Create(menu, this);    	
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	ContextMenu.ManageItemClick(item, this);
    	return true;
    }

	private void addOptionsButtonListener() {
		final ImageButton optionsButton = (ImageButton)findViewById(R.id.optionsButton);
		
		optionsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});
	}
	
	private void addBluetoothButtonListener() {
		final ToggleButton toggleButton = (ToggleButton)findViewById(R.id.bluetoothActiveButton);
		
		toggleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(toggleButton.isChecked()) {
					bluetoothConnection.connectToServer();
				} else {
					bluetoothConnection.disconnectFromServer();
				}
			}
		});
	}

	private void addStatusEntry(String text) {
		final int dateColumnWidth = 100;
		final int dismissColumnWidth = 60;
		final int dismissButtonSize = 48;
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
		ImageButton dismissButton = new ImageButton(this);
		dismissButton.setBackground(getResources().getDrawable(R.drawable.button_dismiss));
		
		dismissButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				statusPanel.removeView(statusPanelEntry);
				
				if(statusPanel.getChildCount() == 0) {
					addDefaultStatusEntry();
				}
			}
		});
		
		statusPanelEntry.addView(dismissButton, new LayoutParams(dismissButtonSize, dismissButtonSize));
		
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
	public void onStartListening(final ConnectionType connectionType) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				addStatusEntry(String.format("Searching for nearby %s sensors", NetworkConnection.GetConnectionTypePrintname(connectionType)));
			}
			
		});
	}

	@Override
	public void onConnectionEstablished(final ConnectionType connectionType) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				addStatusEntry(String.format("Connected to %s sensor", NetworkConnection.GetConnectionTypePrintname(connectionType)));
				
				ToggleButton toggleButton = (ToggleButton)findViewById(R.id.bluetoothActiveButton);
				toggleButton.setBackground(getResources().getDrawable(R.drawable.button_bluetooth_on));
			}
			
		});
	}
	
	@Override
	public void onConnectionClosed(final ConnectionType connectionType) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				addStatusEntry(String.format("Disconnected from %s sensor", NetworkConnection.GetConnectionTypePrintname(connectionType)));
				
				ToggleButton toggleButton = (ToggleButton)findViewById(R.id.bluetoothActiveButton);
				toggleButton.setBackground(getResources().getDrawable(R.drawable.button_bluetooth_off));
			}
			
		});
	}

	@Override
	public void onCommandReceived(final String command) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				addStatusEntry(String.format("Command received: %s", command));
			}
			
		});
	}

	@Override
	public void onConnectionError(final ConnectionType connectionType, final String error) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				addStatusEntry(String.format("Connection error to %s sensors: %s", connectionType, error));
			}
			
		});
	}
}
