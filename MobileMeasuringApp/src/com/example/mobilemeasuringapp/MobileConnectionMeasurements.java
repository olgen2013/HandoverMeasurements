package com.example.mobilemeasuringapp;

import org.json.JSONException;
import org.json.JSONObject;

import utils.ReceiverEventHandler;
import utils.SenderEventHandler;
import utils.SmartphoneData;
import utils.WebSocketConnectionHandler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
    public final String MOBILE_CONNECTION_MEASUREMENTS = "mobileConnectionMeasurements/latencyAndThroughtput";
    public final int MAX_SIZE_NOTIFICATION_ARRAY = 20;
    public boolean connectedToServer = false;
    
    public final Wamp receiverConnection = new WampConnection();
    public WebSocketConnectionHandler receiverConnectionHandler;
    public ReceiverEventHandler receiverEventHandler;
    
    public final Wamp senderConnection = new WampConnection();
    public WebSocketConnectionHandler senderConnectionHandler;
    public SenderEventHandler senderEventHandler;
    
    public boolean subscribed = false;
    private WebSocketTransmissionTask wsTransmissionTask;

    // smartphone data 
    private SmartphoneData smartphoneData;
    // location manager for GPS
    private LocationManager locationManager;
    
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
	public boolean buttonPressed;
	private TextView statusText;
	private ListView notificationList;

 
	
	
    private class WebSocketTransmissionTask extends AsyncTask <Void,Void,Void> {
    	@Override
    	protected Void doInBackground(Void... arg0) {
    		boolean init = true;
    		JSONObject staticEvent;
    		
			while (true) {
//				if (connectedToServer == true) {
//					if (init) {
//						notificationArrayAdapter.add("start sending measuring packages ...");
//						init = false;
//					}
//					
//					staticEvent = new JSONObject();
//		    		try {
//		                staticEvent.put("latitude", smartphoneData.getLatitude());
//		                staticEvent.put("longitude", smartphoneData.getLongitude());
//		                staticEvent.put("gpsdate", smartphoneData.getDate());
//						staticEvent.put("gpsspeed", smartphoneData.getSpeed());
//						staticEvent.put("timestamp_sender", smartphoneData.getDate().getTime());
//						staticEvent.put("networktype", smartphoneData.getNetworkType());
//					} catch (JSONException e1) {
//						e1.printStackTrace();
//					}
					
//		    		senderConnection.publish(MOBILE_CONNECTION_MEASUREMENTS, staticEvent.toString());
					
					senderConnection.publish(MOBILE_CONNECTION_MEASUREMENTS, "TASKTEST");
									
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
							
//				if (isCancelled()) {
//					if (connectedToServer == true) {
//						mConversationArrayAdapter.add("stop sending latency packages ...");
//					}
//					break;
//				}
//			}
//			return null;
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
        initWebSocketConnection();              
      
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// access preference parameters
		getPreferences();
		
        smartphoneData = new SmartphoneData((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
        //initWebSocketConnection();
        
//        if(wsTransmissionTask.isCancelled()){
//            wsTransmissionTask = new WebSocketTransmissionTask();
//            wsTransmissionTask.execute();
//        }
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
		
				
		receiverEventHandler = new ReceiverEventHandler(this, smartphoneData);
		senderEventHandler = new SenderEventHandler(this, smartphoneData);
		subscribed = false;
		receiverConnectionHandler = new WebSocketConnectionHandler(this, MOBILE_CONNECTION_MEASUREMENTS, receiverConnection, receiverEventHandler);
		subscribed = true;
		senderConnectionHandler = new WebSocketConnectionHandler(this, MOBILE_CONNECTION_MEASUREMENTS, senderConnection, senderEventHandler);
		
		
		final String wsuri = "ws://" + serverAddress;
		receiverConnection.connect(wsuri, receiverConnectionHandler);
		senderConnection.connect(wsuri, senderConnectionHandler);
	}
		
	private void initView(){
		
		// TODO: impl. button listener or starting / stopping measuring
		
		notificationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		
		// access view elements 
		connectButton =  (Button) findViewById(R.id.connectButton);
		buttonPressed = false;
		statusText = (TextView) findViewById(R.id.statusline);
		notificationList = (ListView) findViewById(R.id.notificationList);
		notificationList.setAdapter(notificationArrayAdapter);	
		
		intiConnectButton();
	}
	
//	private void intiStatusText() {
//		statusText.setText("Ready");
//	}
	
	private void intiConnectButton() {
		connectButton.setText("Connect");
		connectButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (buttonPressed == false){
					connectButton.setText("Disconnect");
					buttonPressed = true;
					notificationArrayAdapter.add("Starting Measuring Task ...");
						
					wsTransmissionTask = new WebSocketTransmissionTask();
			        wsTransmissionTask.execute();
				}
				else{
					connectButton.setText("Connect");
					buttonPressed = false;
					senderConnection.disconnect();
			        boolean canceled = wsTransmissionTask.cancel(true);
			        notificationArrayAdapter.add("Stoping Measuring Task ..." + canceled);
			        
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
