package utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.mobilemeasuringapp.MobileConnectionMeasurements;

import de.tavendo.autobahn.Wamp;

public class ReceiverEventHandler implements Wamp.EventHandler {
	JSONObject reveivedPayload = new JSONObject();
	MobileConnectionMeasurements mConnectionMeasurements;
	SmartphoneData smartphoneData;
	
	public ReceiverEventHandler(MobileConnectionMeasurements mConnectionMeasurements, SmartphoneData smartphoneData) {       
        this.mConnectionMeasurements = mConnectionMeasurements;
        this.smartphoneData = smartphoneData;
		mConnectionMeasurements.notificationArrayAdapter.add("init ReceiverEventHandler");
	}
	
	@Override
	public void onEvent(String topicUri, Object event) {
		
		
		mConnectionMeasurements.notificationArrayAdapter.add("Recveived packet in Eventhandler");
		
				
//		if (mConnectionMeasurements.notificationArrayAdapter.getCount() == mConnectionMeasurements.MAX_SIZE_NOTIFICATION_ARRAY) {
//			mConnectionMeasurements.notificationArrayAdapter.clear();
//		}
//			
//		// cast String to JSON Object
//		String reveivedPayloadString = (String) event;
//		mConnectionMeasurements.notificationArrayAdapter.add(reveivedPayloadString);
//		reveivedPayload = null;
//		
//		try {reveivedPayload = new JSONObject(reveivedPayloadString);} 
//		catch (JSONException e1) {e1.printStackTrace();}
//		
//		// latency calculation  
//		long transmissionTime = 0;
//		try {transmissionTime = Long.parseLong(reveivedPayload.get("timestamp_sender").toString());}
//		catch (JSONException e) {e.printStackTrace();}
//
//		long receiveTime = smartphoneData.getDate().getTime();
//		int latency = (int)(receiveTime - transmissionTime);
//		
//		// TODO: throughput calculation
//		
//		try {
//			mConnectionMeasurements.notificationArrayAdapter.add("<< incoming " + reveivedPayload.get("event").toString() + " package >>");
//			mConnectionMeasurements.notificationArrayAdapter.add("package sent at: " + reveivedPayload.get("timestamp_sender").toString());
//			mConnectionMeasurements.notificationArrayAdapter.add("package received at: " + receiveTime);
//			mConnectionMeasurements.notificationArrayAdapter.add("calculated latency: " + latency + "ms");
//			
//			// Log latency package received
//			if (C2CMainActivity.loggerPref == true) {
//				eventReceived.put("mode", "receiver");
//				eventReceived.put("timestamp_receiver", t1);
//				eventReceived.put("latency", latency);
//				eventReceived.put("networktype", smartphoneData.getNetworkType());
//				Logger.log(eventReceived);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}
}
