package com.example.merloclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class StartScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);

		SharedPreferences setupPrefs = getSharedPreferences("SETUP_PREFS", 0);
		
		// If they are already logged in skip all login/setup screens
		if(setupPrefs.getBoolean("LOGGED_IN", false)){
			
			Intent mainScreen = new Intent(this, MainActivity.class);
			startActivity(mainScreen);
			overridePendingTransition(R.anim.animate_left_out,
					R.anim.animate_right_in);
			
			finish();
		}
		

	}

	public void newUser(View v) {

		Intent newUserSetup = new Intent(this, SetupActivity.class);
		startActivity(newUserSetup);
		overridePendingTransition(R.anim.animate_left_out,
				R.anim.animate_right_in);
		
		finish();
	}

	public void existingUser(View v) {

		Intent existingUserLogin = new Intent(this, LoginActivity.class);
		existingUserLogin.putExtra("LOGIN", true);
		startActivity(existingUserLogin);
		overridePendingTransition(R.anim.animate_left_out,
				R.anim.animate_right_in);

		finish();
		
	}
}
