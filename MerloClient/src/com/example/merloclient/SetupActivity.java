package com.example.merloclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SetupActivity extends MySetupActivity {

	EditText nameET;
	EditText addressET;
	EditText typeET;
	
	SharedPreferences setupPrefs;
	SharedPreferences.Editor setupPrefsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		
		setupPrefs = getSharedPreferences("SETUP_PREFS", 0);
		setupPrefsEditor = setupPrefs.edit();
		
		nameET = (EditText) findViewById(R.id.nameET);
		addressET = (EditText) findViewById(R.id.addressET);
		typeET = (EditText) findViewById(R.id.typeET);
		
		nameET.setText(setupPrefs.getString("NAME", ""));
		addressET.setText(setupPrefs.getString("ADDRESS", ""));
		typeET.setText(setupPrefs.getString("TYPE", ""));
	}
	
	public void next(View v){
		
		setupPrefsEditor.putString("NAME", nameET.getText().toString());
		setupPrefsEditor.putString("ADDRESS", addressET.getText().toString());
		setupPrefsEditor.putString("TYPE", typeET.getText().toString());
		setupPrefsEditor.apply();
		
		Intent rewardsScreen = new Intent(this, RewardScreen.class);
		
		startActivity(rewardsScreen);
		
	}
	

}
