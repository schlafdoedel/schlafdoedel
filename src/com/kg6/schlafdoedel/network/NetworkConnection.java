package com.kg6.schlafdoedel.network;

import java.util.ArrayList;
import java.util.List;

public class NetworkConnection {
	public enum ConnectionType {
		Bluetooth
	}
	
	private final ConnectionType TYPE;
	
	private List<NetworkEvent> eventListenerList;
	
	public NetworkConnection(ConnectionType connectionType) {
		TYPE = connectionType;
		
		this.eventListenerList = new ArrayList<NetworkEvent>();
	}
	
	public void addNetworkEventListener(NetworkEvent listener) {
		if(!this.eventListenerList.contains(listener)) {
			this.eventListenerList.add(listener);
		}
	}
	
	public void removeNetworkEventListener(NetworkEvent listener) {
		this.eventListenerList.remove(listener);
	}
	
	protected void fireOnStartListening() {
		for(int i = 0; i < this.eventListenerList.size(); i++) {
			this.eventListenerList.get(i).onStartListening(TYPE);
		}
	}
	
	protected void fireOnConnectionEstablished() {
		for(int i = 0; i < this.eventListenerList.size(); i++) {
			this.eventListenerList.get(i).onConnectionEstablished(TYPE);
		}
	}
	
	protected void fireOnConnectionClosed() {
		for(int i = 0; i < this.eventListenerList.size(); i++) {
			this.eventListenerList.get(i).onConnectionClosed(TYPE);
		}
	}
	
	protected void fireOnConnectionError(String error) {
		for(int i = 0; i < this.eventListenerList.size(); i++) {
			this.eventListenerList.get(i).onConnectionError(TYPE, error);
		}
	}
	
	protected void fireOnCommandReceived(String command) {
		for(int i = 0; i < this.eventListenerList.size(); i++) {
			this.eventListenerList.get(i).onCommandReceived(command);
		}
	}
	
	public static String GetConnectionTypePrintname(ConnectionType connectionType) {
		switch(connectionType) {
			case Bluetooth:
				return "Bluetooth";
			default:
				return "unknown";
		}
	}
}
