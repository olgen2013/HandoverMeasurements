package utils;

import com.example.mobilemeasuringapp.MobileConnectionMeasurements;

import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.ConnectionHandler;
import de.tavendo.autobahn.Wamp.EventHandler;

public class WebSocketConnectionHandler implements ConnectionHandler {

	private MobileConnectionMeasurements mConnectionMeasurements;
	private Wamp connection;
	private EventHandler wsEventHandler;
	private String topic;
	
	public WebSocketConnectionHandler(MobileConnectionMeasurements mConnectionMeasurements, String topic, Wamp connection, EventHandler wsEventHandler) {
		this.mConnectionMeasurements = mConnectionMeasurements;
		this.connection = connection;
		this.wsEventHandler = wsEventHandler;
		this.topic = topic;
		mConnectionMeasurements.notificationArrayAdapter.add("init WebSocketConnectionHandler");
	}

	@Override
	public void onOpen() {
		
		mConnectionMeasurements.notificationArrayAdapter.add("trying to reconnect to Server ...");
		//if(mConnectionMeasurements.subscribed == false)
		connection.subscribe(topic, String.class, wsEventHandler);
		mConnectionMeasurements.notificationArrayAdapter.add("subscribed on " + topic);

		if(connection.isConnected()){
			mConnectionMeasurements.notificationArrayAdapter.add("connected to Server " + mConnectionMeasurements.serverAddress);
			mConnectionMeasurements.connectedToServer = true;
		}
		else
			mConnectionMeasurements.notificationArrayAdapter.add("NOT connected to Server " + mConnectionMeasurements.serverAddress);
		
		
		
		// Rename connect button in view if appmode == PUBSUBDEMO
//		mConnectionMeasurements.connectButton.setText("Disconnect");
//		mConnectionMeasurements.buttonPressed = true;
	}
	
	@Override
	public void onClose(int code, String reason) {

		mConnectionMeasurements.notificationArrayAdapter.add("connection closed");
		
		// Rename connect button in view if appmode == PUBSUBDEMO
//		mConnectionMeasurements.connectButton.setText("Connect");
//		mConnectionMeasurements.buttonPressed = false;
		if(!mConnectionMeasurements.subscribed)

		mConnectionMeasurements.connectedToServer = false;		
		
		//Auto reconnect to Server
//		if (mConnectionMeasurements.buttonPressed == true && mConnectionMeasurements.wsReconnect == true) {
//			final String wsuri = "ws://" + mConnectionMeasurements.serverAddress;
//			
//			mConnectionMeasurements.notificationArrayAdapter.add("trying to reconnect to Server ...");
//			mConnectionMeasurements.mobileConnection.connect(wsuri, mConnectionMeasurements.wsConnectionHandler);
//			
//			// Clear list view
//			if (mConnectionMeasurements.notificationArrayAdapter.getCount() == mConnectionMeasurements.MAX_SIZE_NOTIFICATION_ARRAY)
//				mConnectionMeasurements.notificationArrayAdapter.clear();
//		}
	}
}
