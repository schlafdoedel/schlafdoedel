package com.kg6.schlafdoedel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.kg6.schlafdoedel.custom.DigitalClock;
import com.kg6.schlafdoedel.custom.EventDefinitionDialog;
import com.kg6.schlafdoedel.custom.EventListPanel;
import com.kg6.schlafdoedel.custom.RecentActivitiesPanel;
import com.kg6.schlafdoedel.custom.Util;
import com.kg6.schlafdoedel.event.Event;
import com.kg6.schlafdoedel.event.EventNotification;
import com.kg6.schlafdoedel.event.EventScheduler;
import com.kg6.schlafdoedel.event.EventSource;
import com.kg6.schlafdoedel.event.EventSource.SourceType;
import com.kg6.schlafdoedel.network.BluetoothConnection;
import com.kg6.schlafdoedel.network.NetworkConnection;
import com.kg6.schlafdoedel.network.NetworkConnection.ConnectionType;
import com.kg6.schlafdoedel.network.NetworkEvent;
import com.kg6.schlafdoedel.speechrecognition.SpeechRecognition;

@SuppressLint("UseSparseArrays")
public class Overview extends Activity implements NetworkEvent, EventNotification, OnInitListener, OnUtteranceCompletedListener {
	
	private BluetoothConnection bluetoothConnection;
	private EventScheduler eventScheduler;

	private EventDefinitionDialog eventDefinitionDialog;
	private RecentActivitiesPanel recentActivitiesPanel;
	private EventListPanel eventListPanel;
	
	private SpeechRecognitionDialog speechRecognitionDialog;
	private TextToSpeech tts;
	
	private float previousVolume;
	
	private HashMap<Integer, View> availableStatusTabsHash;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.eventDefinitionDialog = null;
		
		this.speechRecognitionDialog = new SpeechRecognitionDialog(this);
		
		this.availableStatusTabsHash = new HashMap<Integer, View>();
		
		this.tts = new TextToSpeech(this, this);
		
		this.previousVolume = 0;

		addOptionsButtonListener();
		addBluetoothButtonListener();
		addEventButtonListener();
		addStatusPanel();

		initializeBluetoothConnection();
		initializeEventScheduler();
		
		startSpeechRecognitionService();

		// TODO
		createTestEvents();
	}
	
	@Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Configuration.SPEECH_RECOGNITION_BROADCAST);
        registerReceiver(this.speechRecognitionDialog.getBroadcastReceiver(), filter);
        
        super.onResume();
    }

    @Override
    protected void onPause() {
    	if(this.speechRecognitionDialog != null) {
    		unregisterReceiver(this.speechRecognitionDialog.getBroadcastReceiver());
    	}
    	
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	stopService(new Intent(this, SpeechRecognition.class));
    	
    	if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

	private void createTestEvents() {
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		
		Event event = new Event("Wake me up", Util.GetMillisecondsOfDay(hour, minute, second + 3), Util.GetMillisecondsOfDay(hour + 1, minute, second));
		event.addEventSource(new EventSource(SourceType.Music,"http://onair.krone.at/kronehit.mp3"));
		event.addEventSource(new EventSource(SourceType.Image,"http://3.bp.blogspot.com/-J0ms_mKUTMg/TuS1QPg8LqI/AAAAAAAAGHA/1IobgDijAiQ/s1600/sunrise.jpg"));

		for (int i = 0; i < 7; i++) {
			event.setRepetition(i, true);
		}
		
		this.eventScheduler.addEvent(event);

		this.eventScheduler.setSleepingPhase(Configuration.COMMAND_SLEEPING_PHASE_SHALLOW);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && data != null) {
			switch (requestCode) {
				case Configuration.FILE_CHOOSER_MUSIC_RESPONSE_CODE:
					if (eventDefinitionDialog != null) {
						eventDefinitionDialog.setMusicSource(data.getData().toString());
					}
	
					break;
				case Configuration.FILE_CHOOSER_IMAGE_RESPONSE_CODE:
					if (eventDefinitionDialog != null) {
						eventDefinitionDialog.setImageSource(data.getData().toString());
					}
	
					break;
			}
		}
	}

	private void initializeBluetoothConnection() {
		this.bluetoothConnection = BluetoothConnection.CreateInstance(this);
		this.bluetoothConnection.addNetworkEventListener(this);
		this.bluetoothConnection.startServer();
	}

	private void initializeEventScheduler() {
		this.eventScheduler = EventScheduler.CreateInstance(this,(FrameLayout) findViewById(R.id.visualizationPanel));
		this.eventScheduler.addEventNotificationListener(this);
		
		this.eventScheduler.setScreenBrightness(Configuration.WINDOW_MAX_BRIGHTNESS);

		if (!this.eventScheduler.isAlive()) {
			this.eventScheduler.start();
		}

		if (this.recentActivitiesPanel != null) {
			this.recentActivitiesPanel.setEventScheduler(this.eventScheduler);
		}
		
		if (this.eventListPanel != null) {
			this.eventListPanel.setEventScheduler(this.eventScheduler);
			this.eventScheduler.addEventNotificationListener(this.eventListPanel);
		}

		DigitalClock digitalClock = (DigitalClock) findViewById(R.id.digitalClock);

		if (digitalClock != null) {
			digitalClock.setEventScheduler(this.eventScheduler);
		}
	}
	
	private void startSpeechRecognitionService() {
		startService(new Intent(this, SpeechRecognition.class));
	}

	private void addStatusPanel() {
		final Button recentActivitiesButton = (Button) findViewById(R.id.recentActivitiesButton);
		
		recentActivitiesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchToTab(R.id.recentActivitiesButton);
			}
		});
		
		final Button eventListButton = (Button) findViewById(R.id.eventListButton);
		
		eventListButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchToTab(R.id.eventListButton);
			}
		});
		
		this.recentActivitiesPanel = new RecentActivitiesPanel(this);
		this.eventListPanel = new EventListPanel(this);
		
		//Add items to the status tab hash
		this.availableStatusTabsHash.put(R.id.recentActivitiesButton, this.recentActivitiesPanel);
		this.availableStatusTabsHash.put(R.id.eventListButton, this.eventListPanel);
		
		//Show the recent activities panel by default
		switchToTab(R.id.recentActivitiesButton);
	}
	
	private void switchToTab(int buttonId) {
		final LinearLayout statusPanelContainer = (LinearLayout) findViewById(R.id.statusPanelContent);
		statusPanelContainer.removeAllViews();
		
		Set<Integer> tabButtonIdSet = this.availableStatusTabsHash.keySet();
		
		for(Integer tabButtonId : tabButtonIdSet) {
			Button tabButton = (Button) findViewById(tabButtonId);
			
			if(tabButtonId == buttonId) {
				tabButton.setBackgroundColor(Color.GRAY);
				
				statusPanelContainer.addView(this.availableStatusTabsHash.get(tabButtonId), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			} else {
				tabButton.setBackgroundColor(Color.WHITE);
			}
		}
	}

	private void addOptionsButtonListener() {
		final ImageButton optionsButton = (ImageButton) findViewById(R.id.optionsButton);

		optionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});
	}

	private void addBluetoothButtonListener() {
		final ImageButton bluetoothButton = (ImageButton) findViewById(R.id.bluetoothActiveButton);

		bluetoothButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bluetoothConnection.isConnected()) {
					bluetoothConnection.disconnectFromServer();
				} else {
					bluetoothConnection.connectToServer();
				}
			}
		});
	}

	private void addEventButtonListener() {
		final ImageButton addEventButton = (ImageButton) findViewById(R.id.addEventButton);

		addEventButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				eventDefinitionDialog = new EventDefinitionDialog(Overview.this, eventScheduler);
				eventDefinitionDialog.show();
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
	public void onWaitingForBondedDevice(final ConnectionType connectionType) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showStatusText(String.format("Waiting for bonded %s device", NetworkConnection.GetConnectionTypePrintname(connectionType)));
			}

		});
	}

	@Override
	public void onConnectionEstablished(final ConnectionType connectionType) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showStatusText(String.format("Connected to %s sensor",NetworkConnection.GetConnectionTypePrintname(connectionType)));

				ImageButton toggleButton = (ImageButton) findViewById(R.id.bluetoothActiveButton);
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

				ImageButton toggleButton = (ImageButton) findViewById(R.id.bluetoothActiveButton);
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
		if (this.eventScheduler != null && (command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_AWAKE) == 0 || command .compareTo(Configuration.COMMAND_SLEEPING_PHASE_DEEP) == 0 || 
				command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_SHALLOW) == 0)) {
			this.eventScheduler.setSleepingPhase(command);
		}

		// Switch brightness
		if(this.eventScheduler != null) {
			if (command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_AWAKE) == 0) {
				this.eventScheduler.setScreenBrightness(Configuration.WINDOW_MAX_BRIGHTNESS);
			} else {
				this.eventScheduler.setScreenBrightness(Configuration.WINDOW_MIN_BRIGHTNESS);
			}
		}
	}
	
	@Override
	public void onEventListChanged() {
		//not needed here
	}

	@Override
	public void onEventRaised(final Event event) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (recentActivitiesPanel != null) {
					recentActivitiesPanel.addStatusText(event);
				}
			}

		});
	}

	@Override
	public void onEventDismissed(final Event event) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (recentActivitiesPanel != null) {
					recentActivitiesPanel.removeStatusText(event);
				}
			}

		});
	}
	
	@Override
	public void onEventError(final Event event, final String error) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showStatusText(String.format("Error in event %s: %s", event.getTitle(), error));
			}

		});
	}

	private void showStatusText(String text) {
		if (this.recentActivitiesPanel != null) {
			this.recentActivitiesPanel.addStatusText(text);
			
			switchToTab(R.id.recentActivitiesButton);
		}
	}
	
	public class SpeechRecognitionDialog extends Dialog {
		private BroadcastReceiver broadcastReceiver;
		
		private LinearLayout contentContainerLayout;
		private ImageView avatarImageView;
		
		private SpeechRecognitionDialog(Context context) {
			super(context);
			
			initializeControls();
			
			manageBroadcastReceiver();
		}
		
		private void initializeControls() {
			setTitle("How can I help you?");
			
			this.contentContainerLayout = new LinearLayout(getContext());
			this.contentContainerLayout.setOrientation(LinearLayout.VERTICAL);
			
			addContentView(this.contentContainerLayout, new LayoutParams(Util.GetDeviceWidth(getContext()), LayoutParams.MATCH_PARENT));
			
			this.avatarImageView = new ImageView(getContext());
		}
		
		private void manageBroadcastReceiver() {
			this.broadcastReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					final String command = intent.getStringExtra("command");
					
					if(command.compareTo(Configuration.SPEECH_RECOGNITION_COMMAND_ACTIVATED) == 0) {
						showSpeechRecognitionSymbol();
						previousVolume = eventScheduler.getEventAudioVolume();
						eventScheduler.setEventAudioVolume(0);
					} else if(command.compareTo(Configuration.SPEECH_RECOGNITION_COMMAND_DEACTIVATED) == 0) {
						hideSpeechRecognitionSymbol();
					} else if(command.compareTo(Configuration.SPEECH_RECOGNITION_COMMAND_WEATHER) == 0) {
						Thread weather = new WeatherRequest();
						weather.start();
					} else if(command.compareTo(Configuration.SPEECH_RECOGNITION_COMMAND_NEWS) == 0) {
						//TODO
					} else {
						Toast.makeText(Overview.this, String.format("Command %s can not be handled", command), Toast.LENGTH_SHORT).show();
					}
				}
			};
		}
		
		public BroadcastReceiver getBroadcastReceiver() {
			return this.broadcastReceiver;
		}
		
		private void showSpeechRecognitionSymbol() {
			this.avatarImageView.setImageDrawable(Overview.this.getResources().getDrawable(R.drawable.speech_recognition_avatar));
			
			LayoutParams avatarImageParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			avatarImageParams.gravity = Gravity.CENTER;
			
			this.contentContainerLayout.removeAllViews();
			this.contentContainerLayout.addView(this.avatarImageView, avatarImageParams);
			
			show();
		}
		
		private void hideSpeechRecognitionSymbol() {
			this.contentContainerLayout.removeAllViews();
			
			dismiss();
		}
		
		private class WeatherRequest extends Thread {
			
			@Override
			public void run() {
				
				HttpClient client = new DefaultHttpClient();
				HttpGet getCommand = new HttpGet(Configuration.WEATHER_API_URL_1 + "Linz" + Configuration.WEATHER_API_URL_2); 
				HttpResponse response;
			    
			    try {
			        response = client.execute(getCommand);
			        HttpEntity entity = response.getEntity();
			        
			        Log.i("HTTPResponseStatus",response.getStatusLine().toString());
			        
			        if (entity != null) {
			        	
			        	InputStream in = entity.getContent();
			            
			            String result= convertInputStreamToString(in);
			            in.close();
			            
			            JSONObject weatherData = new JSONObject(result);
			            JSONObject data = weatherData.getJSONObject("data");
			            JSONObject currentCondition = data.getJSONArray("current_condition").getJSONObject(0);
			            
			            String currentConditionText = "The current condition is " +
			            		currentCondition.getJSONArray("weatherDesc").getJSONObject(0).getString("value").toLowerCase() +
			            		" at " + currentCondition.getString("temp_C") + " degrees celcius, dude";
			            
			            HashMap<String, String> ttsHash = new HashMap<String, String>();
			            ttsHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
			            ttsHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "WEATHER");
			            
			            tts.speak(currentConditionText, TextToSpeech.QUEUE_FLUSH, ttsHash);
			        }
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }
			}
			
			private String convertInputStreamToString(InputStream in) {
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			    StringBuilder finalString = new StringBuilder();
			    String currentLine = null;
			    
			    try {
			        while ((currentLine = reader.readLine()) != null) {
			        	finalString.append(currentLine + "\n");
			        }
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			    
			    return finalString.toString();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onInit(int stat) {
		if (stat == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.UK);
            tts.setOnUtteranceCompletedListener(this);
		}
	}
	
	@Override
	public void onUtteranceCompleted(String utteranceID) {
		
		if (utteranceID.equals("WEATHER")) {
			eventScheduler.setEventAudioVolume(previousVolume);
		}
	}
}
