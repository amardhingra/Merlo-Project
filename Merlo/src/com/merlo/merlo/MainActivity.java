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
import android.widget.TextView;

@SuppressWarnings("unused")
public class MainActivity extends Activity implements ServerCommunication {

	// variables requred for viewPager
	int count = 1;

	ViewPager mViewPager;
	SectionsPagerAdapter mSectionsPagerAdapter;

	// Declaring buttons
	ImageButton qrButton;
	ImageButton button2;
	ImageButton button3;
	ImageButton button4;
	ImageButton button5;

	// Shared preferences
	SharedPreferences loginPrefs;
	SharedPreferences.Editor loginPrefsEditor;

	// server connection
	private static final String TAG_SERVCON_FRAGMENT = "servcon_fragment_main";
	private ServerConnectionFragment servConFragment;

	ArrayList<String> arr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// opening the login preferencess
		loginPrefs = getSharedPreferences(LoginPreferences.NAME, 0);
		loginPrefsEditor = loginPrefs.edit();

		// declaring the buttons
		qrButton = (ImageButton) findViewById(R.id.qr_button);
		button2 = (ImageButton) findViewById(R.id.button2);
		button3 = (ImageButton) findViewById(R.id.button3);
		button4 = (ImageButton) findViewById(R.id.button4);
		button5 = (ImageButton) findViewById(R.id.button5);

		arr = new ArrayList<String>();
		
		// Create the adapter that will return a fragment for each of the
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setClipToPadding(false);
		mViewPager.setAdapter(mSectionsPagerAdapter);

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

		// checking if the qr image has already been downloaded
		// if it hasn't start downloading it
		if (!loginPrefs.getBoolean(LoginPreferences.QR, false))
			servConFragment.start("QR", getSessionToken());
		// if it has try setting it as the image for the qr button
		else
			try {

				Bitmap bitmap = BitmapFactory
						.decodeStream(openFileInput("QR.png"));
				if (bitmap != null)
					qrButton.setImageBitmap(bitmap);

			} catch (FileNotFoundException e) {
				// if the file wasn't found try getting it again
				servConFragment.start("QR", getSessionToken());
			}

	}

	/**
	 * Method that gets called when server connection finishes
	 */
	@Override
	public void getResult(String messageType, String resultType, String result) {

		if (messageType.equals("QR")) {
			if (result.equals("true")) {
				try {
					Bitmap bitmap = BitmapFactory
							.decodeStream(openFileInput("QR.png"));
					if (bitmap != null) {
						// set the image
						qrButton.setImageBitmap(bitmap);

						// The image has been downloaded
						loginPrefsEditor.putBoolean(LoginPreferences.QR, true);
						loginPrefsEditor.apply();
					}
				}
				// This cannot happen unless there is a problem with the devices
				// memory
				catch (FileNotFoundException e) {
				}
			}
		} else if(messageType.equals("ADD")){
			if(resultType.equals("OK")){
				count++;
				mSectionsPagerAdapter.notifyDataSetChanged();
			}
		}

	}

	// TODO: Button methods
	
	public void displayQR(View v) {

		DialogFragment dialog = new DisplayQRFragment();
		dialog.show(getFragmentManager(), "");
	}

	// TODO: SectionsPagerAdapter

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public float getPageWidth(int position) {
			return 0.99f;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			if (position == (count - 1))
				return AddFragment.newInstance();

			return CardFragment.newInstance("jhfhkjhdthg");
		}

		@Override
		public int getCount() {
			// Return the number of data items
			return count;
		}

		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

	}

	// TODO: Fragments for ViewPager

	/**
	 * Default card fragment for displaying information
	 * 
	 */
	public static class CardFragment extends Fragment {
		
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static CardFragment newInstance(String name) {
			CardFragment fragment = new CardFragment();
			Bundle args = new Bundle();
			args.putString("Position", name);
			fragment.setArguments(args);
			return fragment;
		}

		public CardFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			Bundle args = getArguments();
			
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			TextView nameTV = (TextView) rootView.findViewById(R.id.nameTV);
			nameTV.setText(args.getString("Position", "failed"));
			
			return rootView;
		}
	}

	/**
	 * Fragment that allows the user to add a restaurant to their list
	 * 
	 */
	public static class AddFragment extends Fragment {

		public static AddFragment newInstance() {
			return new AddFragment();
		}

		public AddFragment() {
		}

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add, container,
					false);

			return rootView;
		}

	}

	public void addCard(View v) {

		
		 Intent newRest = new Intent(this, AddRestaurantActivity.class); 
		 startActivityForResult(newRest, 0);
		 
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 0) {
			count += resultCode;
		}
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	/**************************************************************
	 * TODO: Options menu
	 **************************************************************/

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
		if (id == R.id.logout) {
			loginPrefsEditor.clear();
			loginPrefsEditor.apply();
			Intent previous = new Intent(this, StartScreen.class);

			startActivity(previous);
			overridePendingTransition(R.anim.animate_right_out,
					R.anim.animate_left_in);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**************************************************************
	 * HELPER METHODS: Don't change
	 **************************************************************/

	private String getSessionToken() {
		return loginPrefs.getString(LoginPreferences.USERNAME, "") + "\n"
				+ loginPrefs.getString(LoginPreferences.SESSION_ID, "") + "\n";
	}

}
