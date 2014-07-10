package com.example.merloclient;

import android.app.FragmentTransaction;
import android.content.Intent;
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

		loginButton = (Button) findViewById(R.id.loginButton);

		usernameET = (EditText) findViewById(R.id.usernameET);
		passwordET = (EditText) findViewById(R.id.passwordET);
		emailET = (EditText) findViewById(R.id.emailET);

		emailTR = (TableRow) findViewById(R.id.emailTR);

		callingIntent = getIntent();

		if (!callingIntent.getBooleanExtra("LOGIN", false)) {
			creatingUser = true;
			emailTR.setVisibility(View.VISIBLE);
			loginButton.setText(getResources().getString(
					R.string.complete_setup));
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

	@Override
	public void getResult(String messageType, String result) {

	}

	public void login(View v) {

		if (creatingUser) {
			String message = callingIntent.getStringExtra("MESSAGE") + NL
					+ usernameET.getText() + NL
					+ passwordET + NL 
					+ emailET+ NL;
			
			servConFragment.start("CREATE", message);
		}
		else{
			String message = usernameET.getText() + NL
					+ passwordET + NL;
			
			servConFragment.start("LOGIN", message);
		}

	}
	
	public void forgotPassword(View v){}

}
