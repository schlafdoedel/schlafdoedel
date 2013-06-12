package com.kg6.schlafdoedel.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kg6.schlafdoedel.custom.Util;

public class BluetoothConnection extends NetworkConnection {
	
	private static BluetoothConnection bluetoothConnection;
	
	public static BluetoothConnection CreateInstance(Activity context) {
		if(bluetoothConnection == null) {
			bluetoothConnection = new BluetoothConnection(context);
		}
		
		return bluetoothConnection;
	}
	
	private final int CONNECTION_SLEEPTIME = 1000;
	private final int REQUEST_ENABLE_BT = 1;
	
	private final Activity CONTEXT;
	
	private Thread listeningThread;
	
	private DeviceSelectionDialog deviceSelectionDialog;
	
	private InputStream inputStream;
	private OutputStream outputStream;
	
	private boolean connected;
	private boolean enabled;
	
	private BluetoothConnection(Activity context) {
		super(ConnectionType.Bluetooth);
		
		CONTEXT = context;
		
		this.deviceSelectionDialog = null;
		
		this.inputStream = null;
		this.outputStream = null;
		
		this.connected = false;
		this.enabled = true;
	}
	
	public void cleanup() {
		this.enabled = false;
		
		try {
			if(this.listeningThread != null) {
				this.listeningThread.interrupt();
				this.listeningThread = null;
			}
		} catch (Exception e) {
			
		}
		
		try {
			if(this.inputStream != null) {
				this.inputStream.close();
			}
		} catch (Exception e) {
			
		}
		
		try {
			if(this.outputStream != null) {
				this.outputStream.close();
			}
		} catch (Exception e) {
			
		}
	}
	
	public void startListening() {
		this.listeningThread = new Thread(new Runnable() {

			@Override
			public void run() {
				fireOnStartListening();
				
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				
				if(bluetoothAdapter == null) {
					AlertDialog ad = new AlertDialog.Builder(CONTEXT).create();
					ad.setCancelable(false);
					ad.setMessage("This device does not support a bluetooth adapter, which is necessary to connect it to external sensors. The functionality of the Schlafdödel is therefore limited.");
					ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					
					return;
				}
				
				//Turn bluetooth on
				if (!bluetoothAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
					CONTEXT.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
				
				while(isEnabled() && !isConnected()) {
					Set<BluetoothDevice> availableDevices = bluetoothAdapter.getBondedDevices();
					
					if(availableDevices.size() == 0) {
						try {
							Thread.sleep(CONNECTION_SLEEPTIME);
						} catch (InterruptedException e) {
							
						}
						
						continue;
					}
					
					showAvailableDevices(availableDevices);
				}
			}
			
		});
		
		this.listeningThread.setName("Schlafdoedel - Bluetooth listening thread");
		this.listeningThread.start();
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	private boolean isEnabled() {
		return this.enabled;
	}
	
	public InputStream getInputStream() {
		return this.inputStream;
	}
	
	public OutputStream getOutputStream() {
		return this.outputStream;
	}
	
	private void showAvailableDevices(final Set<BluetoothDevice> availableDevices) {
		CONTEXT.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(isConnected()) {
					return;
				}
				
				if(deviceSelectionDialog == null) {
					deviceSelectionDialog = new DeviceSelectionDialog(CONTEXT);
					deviceSelectionDialog.show();
				}
				
				for(BluetoothDevice device : availableDevices) {
					deviceSelectionDialog.addSelectableDevice(device);
				}
			}
		});
	}
	
	private void connectToDevice(BluetoothDevice device) {
		try {
			BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
			
			this.inputStream = socket.getInputStream();
			this.outputStream = socket.getOutputStream();
			
			this.connected = true;
			
			fireOnConnectionEstablished();
		} catch (IOException e) {
			Log.e("BluetoothConnection.java", "Unable to create bluetooth socket", e);
		}
	}
	
	private void releaseDeviceSelectionDialog() {
		this.deviceSelectionDialog = null;
	}
	
	private class DeviceSelectionDialog extends Dialog {
		private final int DIALOG_MARGIN = 20;
		private final int CONNECT_BUTTON_WIDTH = 200;
		
		private LinearLayout contentLayout;
		
		private List<BluetoothDevice> visibleDeviceList;

		public DeviceSelectionDialog(Context context) {
			super(context);
			
			this.visibleDeviceList = new ArrayList<BluetoothDevice>();
			
			initializeControls();
		}
		
		private void initializeControls() {
			ScrollView scrollView = new ScrollView(getContext());
			addContentView(scrollView, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - 2 * DIALOG_MARGIN, LayoutParams.WRAP_CONTENT));
			
			this.contentLayout = new LinearLayout(getContext());
			this.contentLayout.setOrientation(LinearLayout.VERTICAL);
			
			scrollView.addView(this.contentLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			//title
			setTitle("Select the Schlafdödel sleeping phase sensor");
		}
		
		public void addSelectableDevice(final BluetoothDevice device) {
			if(this.visibleDeviceList.contains(device)) {
				return;
			}
			
			this.visibleDeviceList.add(device);
			
			LinearLayout entryLayout = new LinearLayout(getContext());
			entryLayout.setOrientation(LinearLayout.HORIZONTAL);
			
			LayoutParams entryLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			entryLayoutParams.leftMargin = DIALOG_MARGIN;
			entryLayoutParams.rightMargin = DIALOG_MARGIN;
			
			this.contentLayout.addView(entryLayout, entryLayoutParams);
			
			LinearLayout entryDescriptionLayout = new LinearLayout(getContext());
			entryDescriptionLayout.setOrientation(LinearLayout.VERTICAL);
			
			entryLayout.addView(entryDescriptionLayout, new LayoutParams(Util.GetDeviceWidth(CONTEXT) - 3 * DIALOG_MARGIN - CONNECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
			
			//device name
			TextView entryNameView = new TextView(getContext());
			entryNameView.setText(device.getName());
			entryNameView.setTextSize(12);
			
			entryDescriptionLayout.addView(entryNameView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			//device address
			TextView entryAddressView = new TextView(getContext());
			entryAddressView.setText(device.getAddress());
			entryAddressView.setTextSize(10);
			
			entryDescriptionLayout.addView(entryAddressView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			//connect button
			Button connectButton = new Button(getContext());
			connectButton.setText("Connect");
			
			connectButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					connectToDevice(device);
					
					dismiss();
					
					releaseDeviceSelectionDialog();
				}
			});
			
			entryLayout.addView(connectButton, new LayoutParams(CONNECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
		}
	}
}
