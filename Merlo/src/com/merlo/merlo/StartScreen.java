package com.merlo.merlo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class StartScreen extends Activity {
	
	SharedPreferences loginPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);
		
		loginPrefs = getSharedPreferences(LoginPreferences.NAME, 0);
		
		if(loginPrefs.getBoolean(LoginPreferences.LOGGED_IN, false)){
			
			Intent mainActivity = new Intent(this, MainActivity.class);
			startActivity(mainActivity);
			
			overridePendingTransition(R.anim.animate_left_out,
					R.anim.animate_right_in);
			
			finish();
		}
		
	}
	
	public void login(View v){
		
		Intent loginIntent = new Intent(this, LoginActivity.class);
		loginIntent.putExtra("LOGGING_IN", true);
		startActivity(loginIntent);
		overridePendingTransition(R.anim.animate_left_out,
				R.anim.animate_right_in);
		
		finish();
		
	}
	
	public void signup(View v){
		
		Intent loginIntent = new Intent(this, LoginActivity.class);
		loginIntent.putExtra("LOGGING_IN", false);
		startActivity(loginIntent);
		overridePendingTransition(R.anim.animate_left_out,
				R.anim.animate_right_in);
		
		finish();
		
	}
}
