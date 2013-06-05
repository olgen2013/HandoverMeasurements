package com.example.mobilemeasuringapp;

import org.json.JSONException;
import org.json.JSONObject;

import utils.Logger;
import utils.ShellInterface;
import utils.SmartphoneData;
import utils.SntpClient;
import utils.WSConnectionHandler;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

public class PureWebSocket extends Activity {

	// WebSocket variables
	public final String MOBILE_CONNECTION_MEASUREMENTS = "moma/latencyAndThroughtput";
	public final int MAX_SIZE_NOTIFICATION_ARRAY = 20;
	public boolean connectedToServer = false;
	public boolean serverthreadrunning = false;
	

	WebSocketConnection wsConnection = new WebSocketConnection();
	public WSConnectionHandler wsConnectionHandler;

	public boolean subscribed = false;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pure_web_socket);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);

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

		//smartphoneData = new SmartphoneData((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));

		// create and start time synchronization 
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.activity_pure_web_socket, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.preferences:
			Intent intent = new Intent(PureWebSocket.this, PreferencesActivity.class);
			startActivity(intent);
			break;
		case R.id.exit:
			finish();
			break;
		}
		return false;

	}

	
	private void initWebSocketConnection(){		
		wsConnectionHandler = new WSConnectionHandler(this, wsConnection, smartphoneData);
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
					try {wsConnection.connect(wsuri, wsConnectionHandler);} 
					catch (WebSocketException e) {e.printStackTrace();}
					statusText.setText("Sender + Receiver connected.");
					
					sendButton.setVisibility(Button.VISIBLE);
					
					if(loggerOn)
						Logger.createLogFile("moma");
				}
				else{
					connectButton.setText("Connect");
					connectButtonPressed = false;
					
					if(serverthreadrunning){
						wsConnection.sendTextMessage("Stop Sending".toString());
						serverthreadrunning = false;
						sendButton.setText("Start Sending");
						sendButtonPressed = false;
					}
						
					wsConnection.disconnect();
					
					statusText.setText("Sender ready to connect.");
					sendButton.setVisibility(Button.INVISIBLE);
					
					if(loggerOn)
						Logger.close();
				}
			}
		});
	}
	private void intiSendButton() {
		sendButton.setVisibility(Button.INVISIBLE);
		sendButton.setText("Start Sending");
		sendButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				JSONObject configObj = new JSONObject();
				try {
					configObj.put("playloadSize", playloadSize);
					configObj.put("transmissionInterval", transmissionInterval);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				if (!sendButtonPressed && connectButtonPressed){
					sendButton.setText("Stop Sending");
					sendButtonPressed = true;
					notificationArrayAdapter.add("Starting Measurement ...");

					wsConnection.sendTextMessage(configObj.toString());
					serverthreadrunning = true;

				}
				else if(sendButtonPressed){
					sendButton.setText("Start Sending");
					sendButtonPressed = false;
					wsConnection.sendTextMessage(configObj.toString());
					serverthreadrunning = false;
					notificationArrayAdapter.add("Stoping Measurement ...");   
				}					
				else{
					notificationArrayAdapter.add("WebSocket connection isn't active ...");
				}
			}
		});
	}

	private void getPreferences() {
		// access preferences 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PureWebSocket.this);

		this.transmissionInterval = Integer.parseInt(prefs.getString("transmission_interval", "500"));
		this.playloadSize = Integer.parseInt(prefs.getString("playload_size", "500"));
		this.wsReconnect = prefs.getBoolean("ws_reconnect_on", true);
		this.serverAddress = prefs.getString("serveraddress", "141.62.65.131:443");
		this.screenOn = prefs.getBoolean("screen_on", true);
		this.mobileNetworkInfo = prefs.getBoolean("mobile_network_info", true);
		this.martphoneRadioInfo = prefs.getBoolean("smartphone_radio_info", true);
		this.locationInfo = prefs.getBoolean("location_info", true);
		this.loggerOn = prefs.getBoolean("logger_on", true);
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
