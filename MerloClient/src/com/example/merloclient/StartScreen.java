package com.example.merloclient;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);

		removeActionBar();

	}

	private void removeActionBar() {
		ActionBar ab = getActionBar();
		ab.hide();
	}

	@SuppressLint("NewApi")
	public void newUser(View v) {

		Intent newUserSetup = new Intent(this, SetupActivity.class);

		Bundle bndlanimation = ActivityOptions.makeCustomAnimation(
				getApplicationContext(), R.anim.animate_left_out,
				R.anim.animate_right_in).toBundle();
		
		startActivity(newUserSetup, bndlanimation);
	}

	@SuppressLint("NewApi")
	public void existingUser(View v) {

		Intent existingUserLogin = new Intent(this, LoginActivity.class);

		Bundle bndlanimation = ActivityOptions.makeCustomAnimation(
				getApplicationContext(), R.anim.animate_left_out,
				R.anim.animate_right_in).toBundle();
		startActivity(existingUserLogin, bndlanimation);

	}
}
