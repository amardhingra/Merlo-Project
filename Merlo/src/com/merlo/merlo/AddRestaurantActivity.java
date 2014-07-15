package com.merlo.merlo;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.merlo.merlo.ServerConnectionFragment.ServerCommunication;

@SuppressWarnings("unused")
public class AddRestaurantActivity extends Activity implements
		ServerCommunication {

	AutoCompleteTextView input;

	String[] names;
	ArrayAdapter<String> arrAdap;

	ServerConnectionFragment servCon;

	// Shared preferences
	SharedPreferences loginPrefs;
	SharedPreferences.Editor loginPrefsEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_restaurant);

		// opening the login preferencess
		loginPrefs = getSharedPreferences(LoginPreferences.NAME, 0);
		loginPrefsEditor = loginPrefs.edit();

		names = new String[0];
		
		input = (AutoCompleteTextView) findViewById(R.id.inputAdd);
		input.addTextChangedListener(watcher);
		

		arrAdap = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, names);
		input.setAdapter(arrAdap);
		
		FragmentManager fm = getFragmentManager();
		servCon = (ServerConnectionFragment) fm.findFragmentByTag("SERVCON");

		if (servCon == null) {
			FragmentTransaction ft = fm.beginTransaction();
			servCon = new ServerConnectionFragment();
			ft.add(servCon, "SERVCON");
			ft.commit();
		}

	}

	@Override
	public void getResult(String messageType, String resultType, String result) {
		if (resultType.equals("OK")) {
			if (messageType.equals("ADD")) {
				setResult(1);
				finish();
			} else if (messageType.equals("FIND")){
				names = result.split(",");
				arrAdap.notifyDataSetChanged();
			}
		}
	}

	public void add(View v) {

	}

	public void search(View v) {

		servCon.start("FIND", getSessionToken() + input.getText());
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() > 4)
				if (true) {
				}

		}
	};

	/**************************************************************
	 * HELPER METHODS: Don't change
	 **************************************************************/

	private String getSessionToken() {
		return loginPrefs.getString(LoginPreferences.USERNAME, "") + "\n"
				+ loginPrefs.getString(LoginPreferences.SESSION_ID, "") + "\n";
	}

}
