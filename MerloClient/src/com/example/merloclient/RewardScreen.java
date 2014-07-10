package com.example.merloclient;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;

public class RewardScreen extends MySetupActivity {

	SharedPreferences setupPrefs;
	SharedPreferences.Editor setupPrefsEditor;
	
	
	String[] rewards;
	
	FragmentManager fm;
	
	FragmentTransaction ft;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reward_screen);
		
		setupPrefs = getSharedPreferences("SETUP_PREFS", 0);
		setupPrefsEditor = setupPrefs.edit();
		
		rewards = setupPrefs.getString("REWARD_LIST", "").split("\n");
		
		
		
	}

	public void next(View v) {

		saveRewards();

		Intent rewardsScreen = new Intent(this, RewardScreen.class);

		startActivity(rewardsScreen);

	}
	
	private void saveRewards(){
		
		
	}

}
