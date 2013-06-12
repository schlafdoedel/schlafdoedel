package com.kg6.schlafdoedel.network;

import com.kg6.schlafdoedel.network.NetworkConnection.ConnectionType;

public interface NetworkEvent {
	public void onStartListening(ConnectionType connectionType);
	public void onConnectionEstablished(ConnectionType connectionType);
}
