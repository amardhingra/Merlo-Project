package com.example.merloclient;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;

public class LoginActivity extends MySetupActivity implements
		ServerConnectionFragment.ServerCommunication {

	private final static String TAG_SERVCON_FRAGMENT = "TAG_SERVCON_FRAG";
	private final static String NL = "\n";

	Intent callingIntent;

	SharedPreferences setupPrefs;
	SharedPreferences.Editor setupPrefsEditor;	
	
	Button loginButton;

	EditText usernameET;
	EditText passwordET;
	EditText emailET;

	TableRow emailTR;

	boolean creatingUser = false;

	ServerConnectionFragment servConFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		setupPrefs = getSharedPreferences("SETUP_PREFS", 0);
		setupPrefsEditor = setupPrefs.edit();
		
		loginButton = (Button) findViewById(R.id.loginButton);

		usernameET = (EditText) findViewById(R.id.usernameET);
		passwordET = (EditText) findViewById(R.id.passwordET);
		emailET = (EditText) findViewById(R.id.emailET);

		restoreData();
		
		callingIntent = getIntent();

		if (!callingIntent.getBooleanExtra("LOGIN", false)) {
			
			creatingUser = true;
			
			emailTR = (TableRow) findViewById(R.id.emailTR);
			emailTR.setVisibility(View.VISIBLE);
			
			loginButton.setText(getResources().getString(
					R.string.complete_setup));
			loginButton.setBackgroundColor(getResources().getColor(R.color.Green));
			
		}

		FragmentManager fm = getFragmentManager();
		servConFragment = (ServerConnectionFragment) fm
				.findFragmentByTag(TAG_SERVCON_FRAGMENT);

		if (servConFragment == null) {
			servConFragment = new ServerConnectionFragment();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(servConFragment, TAG_SERVCON_FRAGMENT);
			ft.commit();
		}
	}

	public void next(View v) {

		saveData();
		
		if (creatingUser) {
			String message = setupPrefs.getString("NAME", "") + NL
					+ setupPrefs.getString("ADDRESS", "") + NL
					+ setupPrefs.getString("TYPE", "") + NL
					+ usernameET.getText() + NL
					+ passwordET.getText() + NL 
					+ emailET.getText() + NL
					+ "3\n" + setupPrefs.getString("REWARD_LIST", "") + NL;
			
			servConFragment.start("SIGNUP", message);
			
		}
		else{
			
			String message = usernameET.getText() + NL
					+ passwordET.getText() + NL;
			
			servConFragment.start("LOGIN", message);
		}

	}
	
	public void previous(View v) {

		servConFragment.cancel();
		
		saveData();
		
		Intent previous;
		
		if(creatingUser)
			previous = new Intent(this, RewardScreen.class);
		else
			previous = new Intent(this, StartScreen.class);

		startActivity(previous, 0);

		finish();

	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		previous(null);

	}

	private void saveData() {

		setupPrefsEditor.putString("USERNAME", usernameET.getText().toString());
		setupPrefsEditor.putString("PASSWORD", passwordET.getText().toString());
		setupPrefsEditor.putString("EMAIL", emailET.getText().toString());
		setupPrefsEditor.apply();

	}
	
	private void restoreData() {
		
		usernameET.setText(setupPrefs.getString("USERNAME", ""));
		passwordET.setText(setupPrefs.getString("PASSWORD", ""));
		emailET.setText(setupPrefs.getString("EMAIL", ""));
		
	}
	
	public void forgotPassword(View v){}
	
	@Override
	public void getResult(String messageType, String resultType, String result) {
		
		if (resultType.equals("OK")){
			
			setupPrefsEditor.putString("SESSION_ID", result);
			setupPrefsEditor.putBoolean("LOGGED_IN", true);
			setupPrefsEditor.apply();
			
			Intent mainScreen = new Intent(this, MainActivity.class);
			startActivity(mainScreen, 1);
			finish();
			
		}
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		saveData();
	}

}
