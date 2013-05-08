package com.example.mobilemeasuringapp;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import utils.ReceiverConnectionHandler;
import utils.ReceiverEventHandler;
import utils.SenderConnectionHandler;
import utils.SenderEventHandler;
import utils.ShellInterface;
import utils.SmartphoneData;
import utils.SntpClient;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.WampConnection;

public class MobileConnectionMeasurements extends Activity {

	// WebSocket variables
	public final String MOBILE_CONNECTION_MEASUREMENTS = "moma/latencyAndThroughtput";
	public final int MAX_SIZE_NOTIFICATION_ARRAY = 20;
	public boolean connectedToServer = false;

	public final Wamp receiverConnection = new WampConnection();
	public ReceiverConnectionHandler receiverConnectionHandler;
	public ReceiverEventHandler receiverEventHandler;

	public final Wamp senderConnection = new WampConnection();
	public SenderConnectionHandler senderConnectionHandler;
	public SenderEventHandler senderEventHandler;

	public boolean subscribed = false;
	private WebSocketTransmissionTask wsTransmissionTask;

	// smartphone data 
	private SmartphoneData smartphoneData;
	// location manager for GPS
	private LocationManager locationManager;
	
    // Time Sync variables
    long syncTimestamp = 0;
    private TimeSyncTask timeSyncTask = new TimeSyncTask();

	// preference variables
	public int transmissionInterval;
	public int playloadSize;
	public boolean wsReconnect;
	public String serverAddress;
	public boolean screenOn;
	public boolean mobileNetworkInfo;
	public boolean martphoneRadioInfo;
	public boolean locationInfo;
	public boolean loggerOn;
	public int loggingInterval;
	public boolean ntpServerOn;
	public String ntpServerAddress;
	public int timeSyncInterval;

	// view parameters
	public ArrayAdapter<String> notificationArrayAdapter;
	public Button connectButton;
	public Button sendButton;
	public boolean connectButtonPressed;
	public boolean sendButtonPressed;
	public TextView statusText;
	private ListView notificationList;


    private class TimeSyncTask extends AsyncTask <Void,Void,Void> {
    	@Override
    	protected Void doInBackground(Void... arg0) {
    		SntpClient client = new SntpClient();
    		
    		while (true) {
    			if (client.requestTime(ntpServerAddress, 1000)) {
        			syncTimestamp = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
        			setTime(syncTimestamp);
        		}
    			
    			try {
					Thread.sleep(timeSyncInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			
    			if (isCancelled()) {
					break;
				}
    		}
    		return null;
    	}
    	
    	// set system time
    	public void setTime(long time) {
    	    if (ShellInterface.isSuAvailable()) {
    	      ShellInterface.runCommand("chmod 666 /dev/alarm");
    	      SystemClock.setCurrentTimeMillis(time);
    	      ShellInterface.runCommand("chmod 664 /dev/alarm");
    	    }
    	  }
    }

	private class WebSocketTransmissionTask extends AsyncTask <Void,Void,Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			boolean init = true;
			JSONObject payload;

			while (true) {

				if(isCancelled()){
					//TODO: calculate conclusion 
					break;
				}
					  
				String randomData = "";
				for (int i=0; i < 40; i++){
					//randomData = randomData + "a";
					randomData = randomData + UUID.randomUUID().toString();
				}
				
				payload = new JSONObject();
				try {
					payload.put("latitude", smartphoneData.getLatitude());
					payload.put("longitude", smartphoneData.getLongitude());
					payload.put("gpsdate", smartphoneData.getDate());
					payload.put("gpsspeed", smartphoneData.getSpeed());
					payload.put("timestamp_sender", smartphoneData.getDate().getTime());
					payload.put("networktype", smartphoneData.getNetworkType());
					payload.put("randomData", randomData);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				
				senderConnection.publish(MOBILE_CONNECTION_MEASUREMENTS, payload.toString());
				// Log latency package sent
				//		    		if (loggerPref == true) {
				//		    			try {
				//							staticEvent.put("latency_mode", "sender");
				//							Logger.log(staticEvent);
				//						} catch (JSONException e1) {
				//							e1.printStackTrace();
				//						}
				//		    		}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mobile_connection_measurements);

		// access preference parameters
		getPreferences();
		// set view parameters
		initView();
		// get location manager
		locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		// access preference parameters
		getPreferences();

		// if GPS is disabled, call GPS dialog 
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(settingsIntent);
		}

		smartphoneData = new SmartphoneData((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
		//initWebSocketConnection();             

		timeSyncTask = new TimeSyncTask();
		timeSyncTask.execute();
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		timeSyncTask.cancel(true);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// access preference parameters
		getPreferences();

		smartphoneData = new SmartphoneData((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
		initWebSocketConnection();
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.activity_mobile_connection_measurements, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.preferences:
			Intent intent = new Intent(MobileConnectionMeasurements.this, PreferencesActivity.class);
			startActivity(intent);
			break;
		case R.id.exit:
			finish();
			break;
		}
		return false;
	}


	private void initWebSocketConnection(){		
		
		// setup event handlers
		senderEventHandler = new SenderEventHandler(this, smartphoneData);
		receiverEventHandler = new ReceiverEventHandler(this, smartphoneData);

		// setup connections handlers
		senderConnectionHandler = new SenderConnectionHandler(this, MOBILE_CONNECTION_MEASUREMENTS, senderConnection, senderEventHandler);
		receiverConnectionHandler = new ReceiverConnectionHandler(this, MOBILE_CONNECTION_MEASUREMENTS, receiverConnection, receiverEventHandler);
		
		// setup receiver connection
		if(receiverConnection.isConnected()){
			receiverConnection.disconnect();
			final String wsuri = "ws://" + serverAddress;
			receiverConnection.connect(wsuri, receiverConnectionHandler);
		}
		else{
			final String wsuri = "ws://" + serverAddress;
			receiverConnection.connect(wsuri, receiverConnectionHandler);			
		}

	}

	private void initView(){

		notificationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

		// access view elements 
		connectButton =  (Button) findViewById(R.id.connectButton);
		sendButton =  (Button) findViewById(R.id.sendButton);
		connectButtonPressed = false;
		sendButtonPressed = false;
		statusText = (TextView) findViewById(R.id.statusline);
		notificationList = (ListView) findViewById(R.id.notificationList);
		notificationList.setAdapter(notificationArrayAdapter);	

		intiConnectButton();
		intiSendButton();
	}

	private void intiConnectButton() {
		connectButton.setText("Connect");
		connectButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (connectButtonPressed == false){
					connectButton.setText("Disconnect");
					connectButtonPressed = true;

					final String wsuri = "ws://" + serverAddress;
					senderConnection.connect(wsuri, senderConnectionHandler);
					statusText.setText("Sender + Receiver connected.");
				}
				else{
					connectButton.setText("Connect");
					connectButtonPressed = false;
					senderConnection.disconnect();	
					statusText.setText("Sender ready to connect.");
				}
			}
		});
	}
	private void intiSendButton() {
		sendButton.setText("Start Sending");
		sendButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (!sendButtonPressed && connectButtonPressed){
					sendButton.setText("Stop Sending");
					sendButtonPressed = true;
					notificationArrayAdapter.add("Starting Measuring Task ...");

					wsTransmissionTask = new WebSocketTransmissionTask();
					wsTransmissionTask.execute();
				}
				else if(sendButtonPressed){
					sendButton.setText("Start Sending");
					sendButtonPressed = false;
					wsTransmissionTask.cancel(true);
					notificationArrayAdapter.add("Stoping Measuring Task ...");   
				}					
				else{
					notificationArrayAdapter.add("WebSocket connection isn't active ...");
				}
			}
		});
	}

	private void getPreferences() {
		// access preferences 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MobileConnectionMeasurements.this);

		this.transmissionInterval = Integer.parseInt(prefs.getString("transmission_interval", "500"));
		this.playloadSize = Integer.parseInt(prefs.getString("playload_size", "500"));
		this.wsReconnect = prefs.getBoolean("ws_reconnect_on", true);
		this.serverAddress = prefs.getString("serveraddress", "141.62.65.131:443");
		this.screenOn = prefs.getBoolean("screen_on", true);
		this.mobileNetworkInfo = prefs.getBoolean("mobile_network_info", true);
		this.martphoneRadioInfo = prefs.getBoolean("smartphone_radio_info", true);
		this.locationInfo = prefs.getBoolean("location_info", true);
		this.loggerOn = prefs.getBoolean("logger_on", false);
		this.loggingInterval = Integer.parseInt(prefs.getString("log_interval", "50"));
		this.ntpServerOn = prefs.getBoolean("ntp_server_on", false);;
		this.ntpServerAddress = prefs.getString("ntpserveraddress", "pool.ntp.org");
		this.timeSyncInterval = Integer.parseInt(prefs.getString("timesync_interval", "1000"));

		if (screenOn == true)
			getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		else
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}
