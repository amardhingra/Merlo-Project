package com.example.merloclient;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends Activity implements
		ServerConnectionFragment.ServerCommunication {

	SharedPreferences setupPrefs;
	SharedPreferences.Editor setupPrefsEditor;
	
	ServerConnectionFragment servCon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupPrefs = getSharedPreferences("SETUP_PREFS", 0);
		setupPrefsEditor = setupPrefs.edit();
		
		FragmentManager fm = getFragmentManager();
		servCon = (ServerConnectionFragment) fm.findFragmentByTag("SERVCON");

		if (servCon == null) {
			FragmentTransaction ft = fm.beginTransaction();
			servCon = new ServerConnectionFragment();
			ft.add(servCon, "SERVCON");
			ft.commit();
		}
	}

	public void startScanner(View v){
		
		Intent scan = new Intent(this, ScannerActivity.class);
		startActivityForResult(scan, 0);
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		
		if(requestCode == 0){
			if(resultCode == RESULT_OK){
				servCon.start("ADD_POINT", getSessionToken() + data.getStringExtra("USERID"));
			}
				
		}
		
	}
	
	private String getSessionToken() {
		return setupPrefs.getString("USERNAME", "") + "\n" 
				+ setupPrefs.getString("SESSION_ID", "") + "\n";
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			setupPrefsEditor.putBoolean("LOGGED_IN", false);
			setupPrefsEditor.apply();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void getResult(String messageType, String resultType, String result) {
		// TODO Auto-generated method stub
		
	}

}
