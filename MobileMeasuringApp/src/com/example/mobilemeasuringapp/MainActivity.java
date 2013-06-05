package com.example.mobilemeasuringapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.preferences:
			Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
			startActivity(intent);
			break;
		case R.id.exit:
			finish();
			break;
		}
		return false;
	}
	
	public void startMobileConnectionMeasurements(View view){
	    Intent intent = new Intent(this, MobileConnectionMeasurements.class);
	    startActivity(intent);
	}
	
	public void startPureWebSocket(View view){
	    Intent intent = new Intent(this, PureWebSocket.class);
	    startActivity(intent);
	}
	
	public void startSensorData(View view){
	    Intent intent = new Intent(this, SensorData.class);
	    startActivity(intent);
	}
}
