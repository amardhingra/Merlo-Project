package com.merlo.merlo;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class PersistentConnectionFragment extends Fragment {

	public final static String IPADDR = "192.168.1.38";
	public final static int PORT = 2000;

	private PersistentServerCommunication serverCom;
	private PersistentServerConnection servCon;
	private boolean isRunning = false;

	/**
	 * This method is called once when the Fragment is first created. Here we
	 * set retainInstance to true
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	/**
	 * Hold a reference to the parent Activity so we can report the task's
	 * current progress and results. The Android framework will pass us a
	 * reference to the newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		if (!(activity instanceof PersistentServerCommunication)) {
			throw new IllegalStateException(
					"Activity must implement the ServerCommunication interface.");
		}

		// Hold a reference to the parent Activity so we can report back the
		// task's
		// current progress and results.
		serverCom = (PersistentServerCommunication) activity;
		// Log.i("servCom", serverCom.toString());
	}
	
	/**
	 * Note that this method is <em>not</em> called when the Fragment is being
	 * retained across Activity instances. It will, however, be called when its
	 * parent Activity is being destroyed for good (such as when the user clicks
	 * the back button, etc.).
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		cancel();
	}
	
	/*****************************/
	/***** TASK FRAGMENT API *****/
	/*****************************/

	/**
	 * Start the background task.
	 */
	public void start(String messageType, String message) {
		Log.i("Starting", "servcon");
		if (!isRunning) {
			isRunning = true;
			servCon = new PersistentServerConnection(messageType, message);
			servCon.execute();
		}
	}

	/**
	 * Cancel the background task.
	 */
	public void cancel() {
		if (servCon!=null && isRunning) {
			Log.i("isRunning", isRunning+"");
			servCon.cancel(false);
			servCon = null;
		}

	}
	
	/***************************/
	/***** BACKGROUND TASK *****/
	/***************************/

	/**
	 * A task that performs some background work and proxies
	 * progress updates and results back to the Activity.
	 */
	private class PersistentServerConnection extends AsyncTask<Void, String, String> {

		String messageType;
		String message;

		public PersistentServerConnection(String messageType, String message) {

			this.messageType = messageType;
			this.message = message;
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			isRunning = false;
			// Proxy the call to the Activity.
			serverCom.getResult(messageType, result);
		}
		
	}
	
	
	/**
	 * Callback interface through which the fragment can report the task's
	 * progress and results back to the Activity.
	 */
	static interface PersistentServerCommunication {

		void update();
		
		void getResult(String messageType, String result);

	}


}
