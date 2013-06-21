package com.kg6.schlafdoedel.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
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
	
	private final String DEVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	
	private final int CONNECTION_SLEEPTIME = 1000;
	private final int REQUEST_ENABLE_BT = 1;
	
	private final Activity CONTEXT;
	
	private boolean enabled;
	
	private BluetoothServer server;
	private BluetoothClient client;
	
	private BluetoothConnection(Activity context) {
		super(ConnectionType.Bluetooth);
		
		CONTEXT = context;
		
		this.enabled = true;
		
		this.server = null;
		this.client = null;
	}
	
	public void cleanup() {
		this.enabled = false;
		
		try {
			if(this.server != null) {
				this.server.interrupt();
				this.server = null;
			}
		} catch (Exception e) {
			
		}
		
		try {
			if(this.client != null) {
				this.client.interrupt();
				this.client = null;
			}
		} catch (Exception e) {
			
		}
	}
	
	public void startServer() {
		try {
			if(this.server == null || !this.server.isAlive()) {
				BluetoothAdapter bluetoothAdapter = getBlueToothAdapter();
				
				this.server = new BluetoothServer(bluetoothAdapter);
				this.server.start();
			}
		} catch (Exception e) {
			this.server = null;
			
			Log.e("BluetoothConnection.java", "Unable to create Bluetooth server", e);
			fireOnConnectionError("Unable to create Bluetooth server");
		}
	}
	
	public void connectToServer() {
		try {
			if(this.client == null || !this.client.isAlive()) {
				BluetoothAdapter bluetoothAdapter = getBlueToothAdapter();
				
				this.client = new BluetoothClient(bluetoothAdapter);
				this.client.start();
			}
		} catch (Exception e) {
			this.client = null;
			
			Log.e("BluetoothConnection.java", "Unable to create Bluetooth client", e);
			fireOnConnectionError("Unable to create Bluetooth client");
		}
	}
	
	public boolean isConnected() {
		if(this.client != null) {
			return this.client.isConnected();
		}
		
		return false;
	}
	
	public void disconnectFromServer() {
		if(this.client != null) {
			this.client.disconnectFromServer();
			this.client = null;
		}
	}
	
	private BluetoothAdapter getBlueToothAdapter() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(bluetoothAdapter == null) {
			AlertDialog dialog = new AlertDialog.Builder(CONTEXT).create();
			dialog.setCancelable(false);
			dialog.setMessage("This device does not support a bluetooth adapter, which is necessary to connect it to external sensors. The functionality of the Schlafdödel is therefore limited.");
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
			return null;
		}
		
		//Turn bluetooth on if necessary
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
			CONTEXT.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		return bluetoothAdapter;
	}
	
	private boolean isEnabled() {
		return this.enabled;
	}
	
	private void releaseDeviceSelectionDialog() {
		if(this.client != null) {
			this.client.releaseDeviceSelectionDialog();
		}
	}
	
	public void connectToDevice(BluetoothDevice device) {
		if(this.client != null) {
			this.client.connectToDevice(device);
		}
	}
	
	public void sendCommand(String command) {
		if(this.client != null) {
			this.client.sendCommand(command);
		}
	}
	
	private class BluetoothServer extends Thread {
		private final BluetoothServerSocket SERVER_SOCKET; 
		
		private BluetoothSocket clientSocket;
		
		public BluetoothServer(BluetoothAdapter bluetoothAdapter) throws IOException {
			SERVER_SOCKET = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Overview", UUID.fromString(DEVICE_UUID)); 
			
			this.clientSocket = null;
		}
		
		public void run() { 
			fireOnStartListening();
			
			BufferedReader reader = null;
			
			while(isEnabled()) {
				try { 
                    this.clientSocket = SERVER_SOCKET.accept(); 
                } catch (Exception e) { 
                    Log.e("BluetoothConnection.java", "Unable to accept Bluetooth connection", e); 
                    break; 
                }
				
				try {
					fireOnConnectionEstablished();
					
					reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
					
					while(isEnabled() && isConnected()) {
						String command = reader.readLine();
						
						if(command != null) {
							fireOnCommandReceived(command);
						} else {
							break;
						}
					}
				} catch (Exception e) {
					Log.e("BluetoothConnection.java", "Unable to receive commands via Bluetooth", e); 
					
					fireOnConnectionError("Connection interrupted!");
				} finally {
					if(reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							
						}
					}
				}
				
				fireOnConnectionClosed();
				
				try {
					Thread.sleep(CONNECTION_SLEEPTIME);
				} catch (InterruptedException e) {
					
				}
			}
		}
		
		public boolean isConnected() {
			try {
				return this.clientSocket != null && this.clientSocket.isConnected();
			} catch (Exception e) {
				Log.e("BluetoothConnection.java", "Unable to determine the connection status", e);
				
				return false;
			}
		}
	}
	
	private class BluetoothClient extends Thread {
		private final BluetoothAdapter BLUETOOTH_ADAPTER;
		
		private DeviceSelectionDialog deviceSelectionDialog;
		
		private BluetoothDevice bluetoothDevice;
		private BluetoothSocket clientSocket;
		
		private List<String> messageQueue;
		
		public BluetoothClient(BluetoothAdapter bluetoothAdapter) {
			BLUETOOTH_ADAPTER = bluetoothAdapter;
			
			this.messageQueue = new ArrayList<String>();
			
			this.deviceSelectionDialog = null;
			this.bluetoothDevice = null;
		}
		
		public void run() { 
			waitForBondedDevices();
			waitForBluetoothDevice();
			
			try {
				this.clientSocket = this.bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(DEVICE_UUID));
				this.clientSocket.connect();
				
				if(isConnected()) {
					fireOnConnectionEstablished();
				} else {
					fireOnConnectionError("Unable to connect to sleeping phase sensor");
					return;
				}
				
				while(isEnabled() && isConnected()) {
					if(!this.messageQueue.isEmpty()) {
						final String command = this.messageQueue.remove(0) + "\n";
						
						this.clientSocket.getOutputStream().write(command.getBytes());
					}
				}
			} catch (IOException e) {
				Log.e("BluetoothConnection.java", "Unable to create bluetooth socket", e);
				
				fireOnConnectionError("Unable to connect to sleeping phase sensor");
			}
			
			fireOnConnectionClosed();
		}
		
		private void waitForBondedDevices() {
			while(isEnabled()) {
				Set<BluetoothDevice> availableDevices = BLUETOOTH_ADAPTER.getBondedDevices();
				
				if(availableDevices.size() > 0) {
					BLUETOOTH_ADAPTER.cancelDiscovery();
					
					showAvailableDevices(availableDevices);
					
					break;
				}
				
				try {
					Thread.sleep(CONNECTION_SLEEPTIME);
				} catch (InterruptedException e) {
					
				}
			}
		}
		
		public void disconnectFromServer() {
			if(!isConnected()) {
				return;
			}
			try {
				if(this.clientSocket != null) {
					this.clientSocket.close();
				}
			} catch (Exception e) {
				Log.e("BluetoothConnection.java", "Unable to disconnect from server", e);
			}
		}
		
		public boolean isConnected() {
			try {
				return this.clientSocket != null && this.clientSocket.isConnected();
			} catch (Exception e) {
				Log.e("BluetoothConnection.java", "Unable to determine the connection status", e);
				
				return false;
			}
		}
		
		public void connectToDevice(BluetoothDevice device) {
			this.bluetoothDevice = device;
		}
		
		public void sendCommand(String command) {
			this.messageQueue.add(command);
		}
		
		private void waitForBluetoothDevice() {
			while(isEnabled() && this.bluetoothDevice == null) {
				try {
					Thread.sleep(CONNECTION_SLEEPTIME);
				} catch (InterruptedException e) {
					
				}
			}
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
		
		private void releaseDeviceSelectionDialog() {
			this.deviceSelectionDialog = null;
		}
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
			
			//Handle back buttons
			setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(!isConnected()) {
						disconnectFromServer();
					}
				}
			});
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
					
					releaseDeviceSelectionDialog();
					
					dismiss();
				}
			});
			
			entryLayout.addView(connectButton, new LayoutParams(CONNECT_BUTTON_WIDTH, LayoutParams.WRAP_CONTENT));
		}
	}
}
