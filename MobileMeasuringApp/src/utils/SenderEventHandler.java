package utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.mobilemeasuringapp.MobileConnectionMeasurements;

import de.tavendo.autobahn.Wamp;

public class SenderEventHandler implements Wamp.EventHandler {
	JSONObject reveivedPayload = new JSONObject();
	MobileConnectionMeasurements mConnectionMeasurements;
	SmartphoneData smartphoneData;
	
	public SenderEventHandler(MobileConnectionMeasurements mConnectionMeasurements, SmartphoneData smartphoneData) {       
        this.mConnectionMeasurements = mConnectionMeasurements;
        this.smartphoneData = smartphoneData;
	}
	
	@Override
	public void onEvent(String topicUri, Object event) {
		mConnectionMeasurements.notificationArrayAdapter.add("SenderEventHandler ... Nothing impl. for OnEvent ... ");
	}
}
