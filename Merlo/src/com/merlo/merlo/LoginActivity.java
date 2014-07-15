package com.merlo.merlo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;

public class LoginActivity extends MySetupActivity implements
		ServerConnectionFragment.ServerCommunication {

	private final static String TAG_SERVCON_FRAGMENT = "TAG_SERVCON_FRAG";
	private final static String NL = "\n";

	Intent callingIntent;

	SharedPreferences loginPrefs;
	SharedPreferences.Editor loginPrefsEditor;

	Button loginButton;
	Button forgotPassword;

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

		loginPrefs = getSharedPreferences(LoginPreferences.NAME, 0);
		loginPrefsEditor = loginPrefs.edit();

		loginButton = (Button) findViewById(R.id.loginButton);
		forgotPassword = (Button) findViewById(R.id.forgotPasswordButton);

		usernameET = (EditText) findViewById(R.id.usernameET);
		passwordET = (EditText) findViewById(R.id.passwordET);
		emailET = (EditText) findViewById(R.id.emailET);

		restoreData();

		callingIntent = getIntent();

		if (!callingIntent.getBooleanExtra("LOGGING_IN", false)) {

			creatingUser = true;

			emailTR = (TableRow) findViewById(R.id.emailTR);
			emailTR.setVisibility(View.VISIBLE);

			forgotPassword.setVisibility(View.GONE);

			loginButton.setText(getResources().getString(R.string.signup));
			loginButton.setBackgroundColor(getResources().getColor(
					R.color.Green));

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
			String message = usernameET.getText() + NL 
						   + passwordET.getText() + NL
						   + emailET.getText();

			servConFragment.start("SIGNUP", message);

		} else {

			String message = usernameET.getText() + NL 
						   + passwordET.getText() + NL;

			Log.i("Starting", "servcon");
			servConFragment.start("LOGIN", message);
		}

	}

	public void previous(View v) {

		servConFragment.cancel();

		saveData();

		Intent previous = new Intent(this, StartScreen.class);

		startActivity(previous, 0);

		finish();

	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		previous(null);

	}

	private void saveData() {

		loginPrefsEditor.putString(LoginPreferences.USERNAME, usernameET.getText().toString());
		loginPrefsEditor.putString(LoginPreferences.PASSWORD, passwordET.getText().toString());
		loginPrefsEditor.putString(LoginPreferences.EMAIL, emailET.getText().toString());
		loginPrefsEditor.apply();

	}

	private void restoreData() {

		usernameET.setText(loginPrefs.getString(LoginPreferences.USERNAME, ""));
		passwordET.setText(loginPrefs.getString(LoginPreferences.PASSWORD, ""));
		emailET.setText(loginPrefs.getString(LoginPreferences.EMAIL, ""));

	}

	public void forgotPassword(View v) {
	}

	@Override
	public void getResult(String messageType, String resultType, String result) {

		if(resultType.equals("OK")){

			loginPrefsEditor.putString(LoginPreferences.SESSION_ID, result);
			loginPrefsEditor.putBoolean(LoginPreferences.LOGGED_IN, true);
			loginPrefsEditor.apply();
			
			Intent mainScreen = new Intent(this, MainActivity.class);
			startActivity(mainScreen, 1);
			finish();

		} else if (resultType.equals("ERR")){
			
			// TODO: deal with error conditions
			
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		saveData();
	}

}
