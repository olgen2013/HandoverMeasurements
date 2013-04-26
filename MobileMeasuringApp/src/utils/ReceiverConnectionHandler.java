package utils;

import com.example.mobilemeasuringapp.MobileConnectionMeasurements;

import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.ConnectionHandler;
import de.tavendo.autobahn.Wamp.EventHandler;

public class ReceiverConnectionHandler implements ConnectionHandler {

	private MobileConnectionMeasurements mConnectionMeasurements;
	private Wamp connection;
	private EventHandler wsEventHandler;
	private String topic;
	
	public ReceiverConnectionHandler(MobileConnectionMeasurements mConnectionMeasurements, String topic, Wamp connection, EventHandler wsEventHandler) {
		this.mConnectionMeasurements = mConnectionMeasurements;
		this.connection = connection;
		this.wsEventHandler = wsEventHandler;
		this.topic = topic;
	}

	@Override
	public void onOpen() {
		
		mConnectionMeasurements.notificationArrayAdapter.add("Receiver trying to reconnect to Server ...");
		connection.subscribe(topic, String.class, wsEventHandler);
		mConnectionMeasurements.notificationArrayAdapter.add("Receiver subscribed on " + topic);

		if(connection.isConnected()){
			mConnectionMeasurements.notificationArrayAdapter.add("Receiver connected to Server " + mConnectionMeasurements.serverAddress);
			mConnectionMeasurements.statusText.setText("Receiver connected!");
			mConnectionMeasurements.connectedToServer = true;
		}
		else
			mConnectionMeasurements.notificationArrayAdapter.add("Receiver NOT connected to Server " + mConnectionMeasurements.serverAddress);
	}
	
	@Override
	public void onClose(int code, String reason) {

		//mConnectionMeasurements.notificationArrayAdapter.add("code: " + code + " reason: " + reason);
				
		//Auto reconnect to Server
		if (mConnectionMeasurements.wsReconnect && !connection.isConnected()) {
			
			final String wsuri = "ws://" + mConnectionMeasurements.serverAddress;
			
			mConnectionMeasurements.notificationArrayAdapter.add("Receiver trying to reconnect to Server ...");
			connection.connect(wsuri, mConnectionMeasurements.receiverConnectionHandler);
			
//			// Clear list view
//			if (mConnectionMeasurements.notificationArrayAdapter.getCount() == mConnectionMeasurements.MAX_SIZE_NOTIFICATION_ARRAY)
//				mConnectionMeasurements.notificationArrayAdapter.clear();
		}
		else{
			mConnectionMeasurements.notificationArrayAdapter.add("Receiver connection closed ...");
			mConnectionMeasurements.statusText.setText("Receiver not connected!");
		}
	}
}
