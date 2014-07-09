package com.merlo.merlo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

public class StartScreen extends Activity implements
		ServerConnectionFragment.ServerCommunication {

	TableRow usernameTR;
	TableRow passwordTR;
	TableRow emailTR;
	TextView error;
	Button forgotPassword;

	ImageView logo;

	EditText username;
	EditText password;
	EditText email;

	TextView loggingIn;
	TextView signingUp;

	ProgressBar spinner;
	
	Button loginButton;
	Button signupButton;
	Button actionButton;

	LinearLayout buttons;

	InputMethodManager imm;

	private static final String TAG_SERVCON_FRAGMENT = "servcon_fragment_start";
	private ServerConnectionFragment servConFragment;

	SharedPreferences temp_prefs;
	SharedPreferences.Editor tempPrefEditor;
	private static final String TAG_TEMP_LOGIN_PREFS = "temp_login_prefs";

	SharedPreferences loginPrefs;
	SharedPreferences.Editor loginPrefsEditor;
	private static final String TAG_LOGIN_PREFS = "login_prefs";

	private static final String TAG_AUTO_LOGIN = "auto_login";
	private static final String TAG_USERNAME = "username_text";
	private static final String TAG_PASSWORD = "password_text";
	private static final String TAG_EMAIL = "email_text";
	private static final String TAG_SESSION_ID = "session_id";
	private static final String TAG_QR = "qr_code";

	boolean tryingLogin = false;
	boolean tryingSignup = false;

	// valid email checker
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private Pattern pattern;
	private Matcher matcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);

		Log.i("onCreate()", "creating");

		// hiding the action bar
		ActionBar ab = getActionBar();
		ab.hide();

		// getting the keyboard to resize the screen
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// checking for a saved frgment
		FragmentManager fm = getFragmentManager();
		servConFragment = (ServerConnectionFragment) fm
				.findFragmentByTag(TAG_SERVCON_FRAGMENT);

		// If the Fragment is non-null, then it is being retained
		// over a configuration change.
		if (servConFragment == null) {
			servConFragment = new ServerConnectionFragment();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(servConFragment, TAG_SERVCON_FRAGMENT);
			ft.commit();
		}

		// creating the editTexts
		username = (EditText) findViewById(R.id.usernameET);
		password = (EditText) findViewById(R.id.passwordET);
		email = (EditText) findViewById(R.id.emailET);

		// setting any saved data from a previous state
		temp_prefs = getSharedPreferences(TAG_TEMP_LOGIN_PREFS, 0);
		username.setText(temp_prefs.getString(TAG_USERNAME, ""));
		password.setText(temp_prefs.getString(TAG_PASSWORD, ""));
		email.setText(temp_prefs.getString(TAG_EMAIL, ""));

		// creating the Views of the login dialogue
		loggingIn = (TextView) findViewById(R.id.logging_in);
		signingUp = (TextView) findViewById(R.id.signing_up);
		usernameTR = (TableRow) findViewById(R.id.usernameTR);
		passwordTR = (TableRow) findViewById(R.id.passwordTR);
		emailTR = (TableRow) findViewById(R.id.emailTR);
		error = (TextView) findViewById(R.id.error);
		forgotPassword = (Button) findViewById(R.id.forgot_password);

		logo = (ImageView) findViewById(R.id.logo);
		
		spinner = (ProgressBar) findViewById(R.id.progressBar);

		// creating the actionButton and layout button row
		actionButton = (Button) findViewById(R.id.actionButton);

		buttons = (LinearLayout) findViewById(R.id.startButtons);

		loginPrefs = getSharedPreferences(TAG_LOGIN_PREFS, 0);
		// auto logging in
		if (loginPrefs.getBoolean(TAG_AUTO_LOGIN, false)) {
			username.setText(loginPrefs.getString(TAG_USERNAME, ""));
			password.setText(temp_prefs.getString(TAG_PASSWORD, ""));

			String uname = username.getText().toString();
			String pword = password.getText().toString();
			String message = uname + "\n" + pword + "\n" + 
							loginPrefs.getString(TAG_QR, "CODE") + "\n";
			servConFragment.start("LOGIN", message);
		}

	}

	// setting necassary views visible and invisible
	public void login(View v) {

		loggingIn.setVisibility(View.VISIBLE);
		usernameTR.setVisibility(View.VISIBLE);
		passwordTR.setVisibility(View.VISIBLE);
		error.setVisibility(View.VISIBLE);
		error.setText("");
		forgotPassword.setVisibility(View.VISIBLE);

		logo.setVisibility(View.GONE);

		actionButton.setText(R.string.login);
		actionButton.setBackgroundResource(R.color.Blue);

		actionButton.setVisibility(View.VISIBLE);
		buttons.setVisibility(View.GONE);

		tryingLogin = true;
	}

	public void signup(View v) {

		signingUp.setVisibility(View.VISIBLE);
		usernameTR.setVisibility(View.VISIBLE);
		passwordTR.setVisibility(View.VISIBLE);
		emailTR.setVisibility(View.VISIBLE);
		error.setVisibility(View.VISIBLE);
		error.setText("");

		logo.setVisibility(View.GONE);

		actionButton.setText(R.string.signup);
		actionButton.setBackgroundResource(R.color.Yellow);

		actionButton.setVisibility(View.VISIBLE);
		buttons.setVisibility(View.GONE);

		tryingSignup = true;

	}

	// starting server communication
	public void completeAction(View v) {

		String uname = username.getText().toString();
		String pword = password.getText().toString();
		String em = email.getText().toString();
		if (uname.length() < 6)
			error.setText("Username must be at least 6 letters");
		else if (pword.length() < 8)
			error.setText("Password must be at least 8 letters");
		else if (!isValidEmail(em))
			error.setText("Invalid emailID");
		else {
			spinner.setVisibility(View.VISIBLE);
			if (tryingSignup) {

				String message = uname + "\n" + pword + "\n" + em + "\n";
				servConFragment.start("SIGNUP", message);
			} else if (tryingLogin) {

				String message = uname + "\n" + pword + "\n"
						+ loginPrefs.getString(TAG_QR, "CODE") + "\n";
				servConFragment.start("LOGIN", message);
			}
		}

	}

	// exit login/signup mode or exiting the app
	public void onBackPressed() {

		if (tryingLogin || tryingSignup) {

			tryingLogin = false;
			tryingSignup = false;

			logo.setVisibility(View.VISIBLE);

			loggingIn.setVisibility(View.INVISIBLE);
			signingUp.setVisibility(View.INVISIBLE);

			usernameTR.setVisibility(View.INVISIBLE);
			passwordTR.setVisibility(View.INVISIBLE);
			emailTR.setVisibility(View.INVISIBLE);

			error.setVisibility(View.GONE);
			error.setText("");
			forgotPassword.setVisibility(View.GONE);

			actionButton.setVisibility(View.GONE);
			buttons.setVisibility(View.VISIBLE);
			
			spinner.setVisibility(View.GONE);

			servConFragment.cancel();
		}

		else
			finish();

	}

	// returning the message that the server sends
	@SuppressLint("NewApi")
	@Override
	public void getResult(String messageType, String response) {

		spinner.setVisibility(View.GONE);
		String[] splitResponse = response.split("\n");
		Log.i("String[]", Arrays.toString(splitResponse));
		if (splitResponse[0].equals("OK")) {

			loginPrefsEditor = loginPrefs.edit();
			loginPrefsEditor.putBoolean(TAG_AUTO_LOGIN, true);
			loginPrefsEditor.putString(TAG_SESSION_ID, splitResponse[1]);
			loginPrefsEditor.putString(TAG_USERNAME, username.getText()
					.toString());
			loginPrefsEditor.putString(TAG_PASSWORD, password.getText()
					.toString());
			if (splitResponse[2].equals("true"))
				loginPrefsEditor.putString(TAG_QR, "RECIEVED");
			loginPrefsEditor.apply();

			Intent main = new Intent(this, MainActivity.class);
			if (tryingSignup)
				main.putExtra("COUNT", 0);
			else{
				main.putExtra("COUNT",
						Integer.parseInt(response.split("\n")[3]));
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 4; i < splitResponse.length - 4; i++)
					list.add(splitResponse[i]);
			}
			startActivity(main);
			finish();
		} else if (splitResponse[0].equals("USERNAME_ERROR")) {
			if (splitResponse[1].equals("TAKEN")) {
				error.setText("That username has been taken");
				forgotPassword.setVisibility(View.VISIBLE);
			} else
				error.setText("Incorrect username or password");
		} else if (splitResponse[0].equals("PASSWORD_ERROR"))
			error.setText("Incorrect username or password");

	}

	// storing temporary data
	protected void onPause() {

		super.onPause();
		temp_prefs = getSharedPreferences(TAG_TEMP_LOGIN_PREFS, 0);
		tempPrefEditor = temp_prefs.edit();
		tempPrefEditor.putString(TAG_USERNAME, username.getText().toString());
		tempPrefEditor.putString(TAG_PASSWORD, password.getText().toString());
		tempPrefEditor.putString(TAG_EMAIL, email.getText().toString());
		tempPrefEditor.apply();

	}

	public void forgotPassword(View v) {

		finish();

	}

	private boolean isValidEmail(String em) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(em);
		return matcher.matches();
	}

}
