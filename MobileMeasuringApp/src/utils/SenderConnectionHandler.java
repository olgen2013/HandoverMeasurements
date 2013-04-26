package utils;

import com.example.mobilemeasuringapp.MobileConnectionMeasurements;

import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.ConnectionHandler;
import de.tavendo.autobahn.Wamp.EventHandler;

public class SenderConnectionHandler implements ConnectionHandler {

	private MobileConnectionMeasurements mConnectionMeasurements;
	private Wamp connection;
	private EventHandler wsEventHandler;
	private String topic;
	
	public SenderConnectionHandler(MobileConnectionMeasurements mConnectionMeasurements, String topic, Wamp connection, EventHandler wsEventHandler) {
		this.mConnectionMeasurements = mConnectionMeasurements;
		this.connection = connection;
		this.wsEventHandler = wsEventHandler;
		this.topic = topic;
	}

	@Override
	public void onOpen() {
		
		mConnectionMeasurements.notificationArrayAdapter.add("Sender trying to connect to Server ...");

		if(connection.isConnected()){
			mConnectionMeasurements.notificationArrayAdapter.add("Sender connected to Server " + mConnectionMeasurements.serverAddress);
			mConnectionMeasurements.connectedToServer = true;
		}
		else
			mConnectionMeasurements.notificationArrayAdapter.add("Sender NOT connected to Server " + mConnectionMeasurements.serverAddress);
	}
	
	@Override
	public void onClose(int code, String reason) {
		
		//mConnectionMeasurements.notificationArrayAdapter.add("code: " + code + " reason: " + reason);
		if(!connection.isConnected())
			mConnectionMeasurements.notificationArrayAdapter.add("Sender connection closed");
			
		//Auto reconnect to Server
		if (mConnectionMeasurements.wsReconnect && mConnectionMeasurements.connectButtonPressed && !connection.isConnected()) {
			final String wsuri = "ws://" + mConnectionMeasurements.serverAddress;
			
			mConnectionMeasurements.notificationArrayAdapter.add("Sender trying to reconnect to Server ...");
			connection.connect(wsuri, mConnectionMeasurements.senderConnectionHandler);
			
//			// Clear list view
//			if (mConnectionMeasurements.notificationArrayAdapter.getCount() == mConnectionMeasurements.MAX_SIZE_NOTIFICATION_ARRAY)
//				mConnectionMeasurements.notificationArrayAdapter.clear();
		}
	}
}
