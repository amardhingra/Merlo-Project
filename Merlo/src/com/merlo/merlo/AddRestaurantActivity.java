package com.merlo.merlo;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.merlo.merlo.ServerConnectionFragment.ServerCommunication;

public class AddRestaurantActivity extends Activity implements
		ServerCommunication {

	EditText input;
	
	private static final String TAG_SERVCON_FRAGMENT = "servcon_fragment_main";
	private ServerConnectionFragment servConFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_restaurant);

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
		
		input = (EditText) findViewById(R.id.inputET);
		
	}

	@Override
	public void getResult(String messageType, String result) {
		if(result.equals("OK")){
			setResult(1);
			finish();
		}
	}
	
	public void add(View v){
		
		servConFragment.start("ADD", getIntent().getStringExtra("Session_token") + input.getText().toString());
		
	}

}
