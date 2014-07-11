package com.example.merloclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

public class RewardScreen extends MySetupActivity {

	SharedPreferences setupPrefs;
	SharedPreferences.Editor setupPrefsEditor;

	Spinner spinner1;
	Spinner spinner2;
	Spinner spinner3;

	EditText rewardET1;
	EditText rewardET2;
	EditText rewardET3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reward_screen);

		setupPrefs = getSharedPreferences("SETUP_PREFS", 0);
		setupPrefsEditor = setupPrefs.edit();

		spinner1 = (Spinner) findViewById(R.id.rewardSpinner1);
		spinner2 = (Spinner) findViewById(R.id.rewardSpinner2);
		spinner3 = (Spinner) findViewById(R.id.rewardSpinner3);

		rewardET1 = (EditText) findViewById(R.id.rewardET1);
		rewardET2 = (EditText) findViewById(R.id.rewardET2);
		rewardET3 = (EditText) findViewById(R.id.rewardET3);

		restoreData();

	}

	public void next(View v) {

		saveData();

		Intent loginActivity = new Intent(this, LoginActivity.class);

		startActivity(loginActivity, 1);
		
		finish();

	}
	
	public void previous(View v) {

		saveData();

		Intent setupScreen = new Intent(this, SetupActivity.class);

		startActivity(setupScreen, 0);

		finish();

	}
	
	@Override
	public void onBackPressed() {

		super.onBackPressed();
		previous(null);

	}

	private void saveData() {

		String rewardList = rewardET1.getText() + ","
				+ spinner1.getSelectedItemPosition() + "\n"
				+ rewardET2.getText() + ","
				+ spinner2.getSelectedItemPosition() + "\n"
				+ rewardET3.getText() + ","
				+ spinner3.getSelectedItemPosition();

		Log.i("Rewards", rewardList);

		setupPrefsEditor.putString("REWARD_LIST", rewardList);
		setupPrefsEditor.apply();

	}

	private void restoreData() {

		String[] rewards = setupPrefs.getString("REWARD_LIST", "").split("\n");
		
		if(rewards.length > 1){
			
			String[] line1 = rewards[0].split(",");
			String[] line2 = rewards[1].split(",");
			String[] line3 = rewards[2].split(",");
			
			rewardET1.setText(line1[0]);
			rewardET2.setText(line2[0]);
			rewardET3.setText(line3[0]);
			
			spinner1.setSelection(Integer.parseInt(line1[1]));
			spinner2.setSelection(Integer.parseInt(line2[1]));
			spinner3.setSelection(Integer.parseInt(line3[1]));
		}
		

	}

	@Override
	public void onPause() {
		super.onPause();
		saveData();
	}

}
