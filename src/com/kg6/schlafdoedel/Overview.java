package com.kg6.schlafdoedel;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ToggleButton;

import com.kg6.schlafdoedel.custom.DigitalClock;
import com.kg6.schlafdoedel.custom.StatusPanel;
import com.kg6.schlafdoedel.custom.Util;
import com.kg6.schlafdoedel.event.Event;
import com.kg6.schlafdoedel.event.Event.EventType;
import com.kg6.schlafdoedel.event.EventNotification;
import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedel.event.EventSource;
import com.kg6.schlafdoedel.network.BluetoothConnection;
import com.kg6.schlafdoedel.network.NetworkConnection;
import com.kg6.schlafdoedel.network.NetworkConnection.ConnectionType;
import com.kg6.schlafdoedel.network.NetworkEvent;
import com.kg6.schlafdoedelmaster.R;

public class Overview extends Activity implements NetworkEvent, EventNotification {
	private BluetoothConnection bluetoothConnection;
	private EventScheduler eventScheduler;
	
	private StatusPanel statusPanel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setScreenBrightness(Configuration.WINDOW_MAX_BRIGHTNESS);
		
		addOptionsButtonListener();
		addBluetoothButtonListener();
		addStatusPanel();
		
		initializeBluetoothConnection();
		initializeEventScheduler();
		
		//TODO
		createTestEvents();
	}
	
	private void createTestEvents() {
		int[] repetition = new int[] { 1, 1, 1, 1, 1, 1, 1 };
		EventSource source = new EventSource("http://www.cr944.at:8000/cr944-high.mp3");
		Event event = new Event("Wake me up", EventType.Music, Util.GetMillisecondsOfDay(00, 41, 00), Util.GetMillisecondsOfDay(10, 45, 00), repetition, source);
		
		//this.eventScheduler.addEvent(event);
		
		source = new EventSource("http://3.bp.blogspot.com/-J0ms_mKUTMg/TuS1QPg8LqI/AAAAAAAAGHA/1IobgDijAiQ/s1600/sunrise.jpg");
		event = new Event("Show me an image", EventType.Image, Util.GetMillisecondsOfDay(21, 27, 00), Util.GetMillisecondsOfDay(21, 45, 00), repetition, source);
		
		this.eventScheduler.addEvent(event);
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
	
	private void initializeBluetoothConnection() {
		this.bluetoothConnection = BluetoothConnection.CreateInstance(this);
		this.bluetoothConnection.addNetworkEventListener(this);
		this.bluetoothConnection.startServer();
	}
	
	private void initializeEventScheduler() {
		this.eventScheduler = EventScheduler.CreateInstance(this, (FrameLayout)findViewById(R.id.visualizationPanel));
		this.eventScheduler.addEventNotificationListener(this);
		
		if(!this.eventScheduler.isAlive()) {
			this.eventScheduler.start();
		}
		
		if(this.statusPanel != null) {
			this.statusPanel.setEventScheduler(this.eventScheduler);
		}
		
		DigitalClock digitalClock = (DigitalClock)findViewById(R.id.digitalClock);
		
		if(digitalClock != null) {
			digitalClock.setEventScheduler(this.eventScheduler);
		}
	}
	
	private void addStatusPanel() {
		this.statusPanel = new StatusPanel(this);
		
		LinearLayout statusPanelContainer = (LinearLayout)findViewById(R.id.statusPanelContent);
		statusPanelContainer.addView(this.statusPanel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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

	@Override
	public void onStartListening(final ConnectionType connectionType) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				showStatusText(String.format("Searching for nearby %s sensors", NetworkConnection.GetConnectionTypePrintname(connectionType)));
			}
			
		});
	}

	@Override
	public void onConnectionEstablished(final ConnectionType connectionType) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				showStatusText(String.format("Connected to %s sensor", NetworkConnection.GetConnectionTypePrintname(connectionType)));
				
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
				showStatusText(String.format("Disconnected from %s sensor", NetworkConnection.GetConnectionTypePrintname(connectionType)));
				
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
				showStatusText(String.format("Command received: %s", command));
				
				handleSensorCommand(command);
			}
			
		});
	}

	@Override
	public void onConnectionError(final ConnectionType connectionType, final String error) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				showStatusText(String.format("%s error: %s", connectionType, error));
			}
			
		});
	}
	
	private void handleSensorCommand(String command) {
		if(this.eventScheduler != null && (command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_AWAKE) == 0 || 
				command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_DEEP) == 0 || command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_SHALLOW) == 0)) {
			this.eventScheduler.setSleepingPhase(command);
		}
		
		//Switch brightness
		if(command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_AWAKE) == 0) {
			setScreenBrightness(Configuration.WINDOW_MAX_BRIGHTNESS);
		} else {
			setScreenBrightness(Configuration.WINDOW_MIN_BRIGHTNESS);
		}
	}

	@Override
	public void onEventRaised(final Event event) {
		if(this.statusPanel != null) {
			this.statusPanel.addStatusText(event);
		}
	}

	@Override
	public void onEventDismissed(Event event) {
		if(this.statusPanel != null) {
			this.statusPanel.removeStatusText(event);
		}
	}
	
	private void showStatusText(String text) {
		if(this.statusPanel != null) {
			this.statusPanel.addStatusText(text);
		}
	}
	
	private void setScreenBrightness(float brightness) {
		Window myWindow = getWindow();
		
		WindowManager.LayoutParams winParams = myWindow.getAttributes();
		winParams.screenBrightness = brightness;
		
		myWindow.setAttributes(winParams);
	}
}
