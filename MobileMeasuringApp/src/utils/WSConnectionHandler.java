package utils;

import com.example.mobilemeasuringapp.PureWebSocket;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

public class WSConnectionHandler extends WebSocketConnectionHandler{

	PureWebSocket pWebSocket;
	WebSocketConnection webSocketConnection;
	public WSConnectionHandler(PureWebSocket pWebSocket, WebSocketConnection webSocketConnection){
		this.pWebSocket = pWebSocket;
		this.webSocketConnection = webSocketConnection;
	}

	@Override
	public void onOpen() {
		pWebSocket.notificationArrayAdapter.add("Sender trying to connect to Server ...");

		if(webSocketConnection.isConnected()){
			pWebSocket.notificationArrayAdapter.add("Sender connected to Server " + pWebSocket.serverAddress);
			pWebSocket.connectedToServer = true;
			//webSocketConnection.sendTextMessage("bulbber".toString());
		}
		else
			pWebSocket.notificationArrayAdapter.add("Sender NOT connected to Server " + pWebSocket.serverAddress);
	}

	@Override
	public void onBinaryMessage(byte[] payload) {
		// TODO Auto-generated method stub
		super.onBinaryMessage(payload);
		pWebSocket.notificationArrayAdapter.add("onBinaryMessage");
		pWebSocket.notificationArrayAdapter.add(payload.toString());
	}
	
	@Override
	public void onTextMessage(String payload) {
		// TODO Auto-generated method stub
		super.onTextMessage(payload);
		pWebSocket.notificationArrayAdapter.add("onTextMessage");
		pWebSocket.notificationArrayAdapter.add(payload.toString());
		
		// momentan wird nur random data übertragen deshalb ist keine berechnung von latenz usw. möglich -> auswertung über wireshark
		// TODO: Logger (Smartphone Data, und andere Berechungen)
	}


	@Override
	public void onClose(int code, String reason) {

		//mConnectionMeasurements.notificationArrayAdapter.add("code: " + code + " reason: " + reason);
		if(!webSocketConnection.isConnected())
			pWebSocket.notificationArrayAdapter.add("Sender connection closed");

		//Auto reconnect to Server
		if (pWebSocket.wsReconnect && pWebSocket.connectButtonPressed && !webSocketConnection.isConnected()) {
			final String wsuri = "ws://" + pWebSocket.serverAddress;

			pWebSocket.notificationArrayAdapter.add("Sender trying to reconnect to Server ...");
			try {
				webSocketConnection.connect(wsuri, pWebSocket.wsConnectionHandler);
			} catch (WebSocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
