package com.kg6.schlafdoedel.network;

import com.kg6.schlafdoedel.network.NetworkConnection.ConnectionType;

public interface NetworkEvent {
	public void onStartListening(ConnectionType connectionType);
	public void onConnectionEstablished(ConnectionType connectionType);
	public void onConnectionClosed(ConnectionType connectionType);
	public void onCommandReceived(String command);
	public void onConnectionError(ConnectionType connectionType, String error);
	public void onWaitingForBondedDevice(ConnectionType connectionType);
}
