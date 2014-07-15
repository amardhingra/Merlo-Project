package com.merlo.merlo;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ServerConnectionFragment extends Fragment {

	/**
	 * Details of server connection
	 */
	public final static String IPADDR = "192.168.61.206";
	//public final static String IPADDR = "192.168.1.38";
	public final static int PORT = 2000;

	// Reference to calling activity
	private ServerCommunication serverCom;

	// Reference to ASyncTask
	private ServerConnection servCon;

	// Used to check if connection is active
	private boolean isRunning = false;

	/*
	 * This method is called once when the Fragment is first created. Here we
	 * set retainInstance to true
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	// Method that binds the fragment to an activity
	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		// Checking that the activity implements the interface
		if (!(activity instanceof ServerCommunication)) {
			throw new IllegalStateException(
					"Activity must implement the ServerCommunication interface.");
		}

		// Hold a reference to the parent Activity so we can report back the
		// task's current progress and results.
		serverCom = (ServerCommunication) activity;
	}

	// Method that is called when Activity is destroyed
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

		if (!isRunning) {
			isRunning = true;
			servCon = new ServerConnection(messageType, message);
			servCon.execute();
		}
	}

	/**
	 * Cancel the background task.
	 */
	public void cancel() {
		if (servCon != null && isRunning) {
			Log.i("isRunning", isRunning + "");
			servCon.cancel(false);
			servCon = null;
		}

	}

	/***************************/
	/***** BACKGROUND TASK *****/
	/***************************/

	private class ServerConnection extends AsyncTask<Void, Void, String[]> {

		String messageType;
		String message;

		// Constructor that takes the messageType and message
		public ServerConnection(String messageType, String message) {

			this.messageType = messageType;
			this.message = message;

		}

		@Override
		protected String[] doInBackground(Void... ignore) {

			// Inititializing the variables that will be returned;
			String responseType = "";
			String response = "";

			try {

				Socket sock = new Socket(IPADDR, PORT);

				// Creating input and output
				PrintWriter output = new PrintWriter(new OutputStreamWriter(
						sock.getOutputStream()), true);

				BufferedReader input = new BufferedReader(
						new InputStreamReader(sock.getInputStream()));

				// sending the message
				output.println(messageType + "\n" + message);
				output.flush();

				if (messageType.equals("SIGNUP") || messageType.equals("LOGIN")) {
					responseType = input.readLine();
					response = input.readLine();

				}

				else if (messageType.equals("QR")) {
					
					responseType = getImage(sock, "QR.png") + "";
					
				}
				
				else if (messageType.equals("ADD")){
					
					responseType = input.readLine();
					response = input.readLine();
					
				}
				
			} catch (UnknownHostException e) {
				response = "Unable to connect to server 1";
			} catch (IOException e) {
				response = "Unable to connect to server 2";
			}

			String[] responseArr = { responseType, response };

			Log.i("messageType", messageType.toString());
			Log.i("message", message.toString());
			Log.i("responseType", responseType.toString());
			Log.i("response", response.toString());

			return responseArr;
		}

		@Override
		protected void onPostExecute(String[] response) {
			isRunning = false;
			// Proxy the call to the Activity.
			serverCom.getResult(messageType, response[0], response[1]);
		}

		private boolean getImage(Socket socket, String imageName) {
			try {
				// Downloading image as bitmap
				Bitmap bitmap = BitmapFactory.decodeStream(socket
						.getInputStream());

				Log.i("Save", bitmap.toString());

				// Opening a file to write the image to
				FileOutputStream out = getActivity().openFileOutput(imageName,
						Context.MODE_PRIVATE);

				// Saving the file
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

			} catch (Exception e) {
				e.printStackTrace(System.err);
				return false;
			}
			return true;

		}

	}

	/**
	 * Callback interface through which the fragment can report the task's
	 * progress and results back to the Activity.
	 */
	static interface ServerCommunication {

		void getResult(String messageType, String resultType, String result);

	}

}
