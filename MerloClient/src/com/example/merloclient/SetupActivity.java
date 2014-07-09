package com.example.merloclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class SetupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

	}

	@SuppressLint("NewApi")
	public void onBackPressed() {

		Intent newUserSetup = new Intent(this, StartScreen.class);

		Bundle bndlanimation = ActivityOptions.makeCustomAnimation(
				getApplicationContext(), R.anim.animate_right_out,
				R.anim.animate_left_in).toBundle();

		startActivity(newUserSetup, bndlanimation);

	}
	
}
