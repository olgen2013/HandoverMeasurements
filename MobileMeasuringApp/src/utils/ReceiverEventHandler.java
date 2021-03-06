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
	}

	@Override
	public void onEvent(String topicUri, Object event) {
		//		mConnectionMeasurements.notificationArrayAdapter.add(event.toString());


		//		if (mConnectionMeasurements.notificationArrayAdapter.getCount() == mConnectionMeasurements.MAX_SIZE_NOTIFICATION_ARRAY) {
		//			mConnectionMeasurements.notificationArrayAdapter.clear();
		//		}
		//			
		// cast String to JSON Object
		String reveivedPayloadString = (String) event;
		//mConnectionMeasurements.notificationArrayAdapter.add(reveivedPayloadString);
		reveivedPayload = null;

		try {reveivedPayload = new JSONObject(reveivedPayloadString);} 
		catch (JSONException e1) {e1.printStackTrace();}

		/*
		 * latency calculation 
		 */

		long transmissionTime = 0;
		try {transmissionTime = Long.parseLong(reveivedPayload.get("timestamp_sender").toString());}
		catch (JSONException e) {e.printStackTrace();}

		long receiveTime = smartphoneData.getDate().getTime();
		double latency = (int)(receiveTime - transmissionTime);

		/*
		 * throughput calculation 
		 */

		// pro zeichen ein byte + header size
		int payloadSize = (reveivedPayloadString.length() + 48 + 34)* 8;
		byte messageSize[] = reveivedPayloadString.getBytes();
		int tmpSize = messageSize.length;

		// throughput in kbit/s
		double throughput = ((double)payloadSize*(1000.0 / (latency/2.0)))/1000;

		try {
			mConnectionMeasurements.notificationArrayAdapter.add("NetworkType: " + reveivedPayload.get("networktype").toString());
			mConnectionMeasurements.notificationArrayAdapter.add("String lenght: " + reveivedPayloadString.length());
			mConnectionMeasurements.notificationArrayAdapter.add("String: " + reveivedPayloadString);
			mConnectionMeasurements.notificationArrayAdapter.add("ByteSize: " + tmpSize);
			mConnectionMeasurements.notificationArrayAdapter.add("calculated throughput: " + throughput + "kbit/sec");
			mConnectionMeasurements.notificationArrayAdapter.add("calculated latency: " + latency + "ms");

			
			JSONObject logObject = new JSONObject();
			// Log latency package received
			if (mConnectionMeasurements.loggerOn == true) {
				logObject.put("mode", "receiver");
				logObject.put("timestamp_sender", transmissionTime);
				logObject.put("timestamp_receiver", receiveTime);
				logObject.put("latency", latency);
				logObject.put("throughput", throughput);
				logObject.put("payloadSize", payloadSize);
				Logger.log(logObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
