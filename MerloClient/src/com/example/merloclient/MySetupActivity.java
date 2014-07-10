package com.example.merloclient;

import android.app.Activity;
import android.content.Intent;

public class MySetupActivity extends Activity {

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.animate_right_out,
				R.anim.animate_left_in);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.animate_left_out,
				R.anim.animate_right_in);
	}
	
	@Override
	public void finish() {
		 super.finish();
		    overridePendingTransition(R.anim.animate_right_out,
					R.anim.animate_left_in);
		
	}

}