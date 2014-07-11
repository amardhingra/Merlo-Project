package com.merlo.merlo;

import android.app.Activity;
import android.content.Intent;

public class MySetupActivity extends Activity {

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.animate_right_out,
				R.anim.animate_left_in);
	}

	public void startActivity(Intent intent, int direction) {
		super.startActivity(intent);

		if (direction == 1)
			overridePendingTransition(R.anim.animate_left_out,
					R.anim.animate_right_in);
		else
			overridePendingTransition(R.anim.animate_right_out,
					R.anim.animate_left_in);
	}

}