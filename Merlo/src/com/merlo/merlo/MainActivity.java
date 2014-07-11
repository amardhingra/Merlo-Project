package com.merlo.merlo;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.merlo.merlo.ServerConnectionFragment.ServerCommunication;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MainActivity extends Activity implements ServerCommunication {

	SectionsPagerAdapter mSectionsPagerAdapter;

	int count;

	ViewPager mViewPager;

	ImageButton qrButton;

	SharedPreferences loginPrefs;
	SharedPreferences.Editor loginPrefsEditor;
	private static final String TAG_LOGIN_PREFS = "login_prefs";

	private static final String TAG_SERVCON_FRAGMENT = "servcon_fragment_main";
	private ServerConnectionFragment servConFragment;

	private static final String TAG_USERNAME = "username_text";
	private static final String TAG_SESSION_ID = "session_id";
	private static final String TAG_QR = "qr_code";

	ArrayList<String> list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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

		count = getIntent().getIntExtra("COUNT", 0) + 1;
		list = getIntent().getStringArrayListExtra("LIST");
		//TODO: DEAL WITH LIST

		loginPrefs = getSharedPreferences(TAG_LOGIN_PREFS, 0);
		if (loginPrefs.getString(TAG_QR, "CODE").equals("CODE"))
			servConFragment.start("QR\n" + getSessionToken(), "");

		qrButton = (ImageButton) findViewById(R.id.qr_button);

		try {
			Bitmap bitmap = BitmapFactory
					.decodeStream(openFileInput("QRCODE.png"));
			qrButton.setImageBitmap(bitmap);
		} catch (FileNotFoundException e) {
			Log.i("Image", "not found");
		}

		// Create the adapter that will return a fragment for each of the
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setClipToPadding(false);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	public void addPoint(View v) {

		servConFragment.start("ADD_POINT", getSessionToken() + "test\n");
		
	}

	public void refresh(View v) {

		servConFragment.start("FETCH_ALL", getSessionToken());
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 0) {
			count += resultCode;
		}
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public float getPageWidth(int position) {
			return 0.975f;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			if (position == (count - 1))
				return AddFragment.newInstance();

			return CardFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return count;
		}

		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class CardFragment extends Fragment {

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static CardFragment newInstance(int position) {
			CardFragment fragment = new CardFragment();
			Bundle args = new Bundle();
			args.putInt("Position", 0);
			fragment.setArguments(args);
			return fragment;
		}

		public CardFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			return rootView;
		}
	}

	public static class AddFragment extends Fragment {

		public static AddFragment newInstance() {
			return new AddFragment();
		}

		public AddFragment() {}

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add, container,
					false);

			return rootView;
		}

	}

	public void addRestaurant(View v) {
		Intent newRest = new Intent(this, AddRestaurantActivity.class);
		newRest.putExtra("Session_token", getSessionToken());
		startActivityForResult(newRest, 0);
	}

	public void displayQR(View v) {

		DialogFragment dialog = new DisplayQRFragment();
		dialog.show(getFragmentManager(), "");
	}

	private String getSessionToken() {
		return loginPrefs.getString(TAG_USERNAME, "") + "\n"
				+ loginPrefs.getString(TAG_SESSION_ID, "") + "\n";
	}

	@Override
	public void getResult(String messageType, String result) {
		// TODO Auto-generated method stub

	}

}
