package com.merlo.merlo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

public class LoginData {

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private static final String TAG_USERNAME = "username_text";
	private static final String TAG_PASSWORD = "password_text";
	private static final String TAG_AUTH_TOKEN = "device_id";
	private static final String TAG_AUTO_LOGIN = "auto_login";

	@SuppressLint("CommitPrefEdits")
	public LoginData(SharedPreferences preferences) {
		prefs = preferences;
		editor = prefs.edit();
	}

	public boolean autoLogin() {
		return prefs.getBoolean(TAG_AUTO_LOGIN, false);
	}
	
	public void setAutoLogin(boolean autoLogin){
		editor.putBoolean(TAG_AUTO_LOGIN, autoLogin);
		editor.apply();
	}

	public String getUsername() {
		return prefs.getString(TAG_USERNAME, null);
	}

	public String getPassword() {
		return prefs.getString(TAG_PASSWORD, null);
	}

	public String getAuthToken() {
		return prefs.getString(TAG_AUTH_TOKEN, null);
	}

	public String getLoginToken() {
		return "LOGIN\n" + getUsername() + "\n" + getPassword() + "\n";
	}

	public String getSignupToken(String email) {
		return "SIGNUP\n" + getUsername() + "\n" + getPassword() + "\n" + email
				+ "\n";
	}

	public String getAuthMessage() {
		return getUsername() + "\n" + getAuthToken() + "\n";
	}

	public void putUsername(String username) {
		editor.putString(TAG_USERNAME, username);
		editor.apply();
	}

	public void putPassword(String password) {
		editor.putString(TAG_PASSWORD, password);
		editor.apply();
	}

	public void putAuthToken(String authToken) {
		editor.putString(TAG_AUTH_TOKEN, authToken);
		editor.apply();
	}

	public final static String IPADDR = "192.168.1.38";
		public final static int PORT = 2000;
}
