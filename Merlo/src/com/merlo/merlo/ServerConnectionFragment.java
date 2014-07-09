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

	public final static String IPADDR = "192.168.61.206";
	public final static int PORT = 2000;

	private ServerCommunication serverCom;
	private ServerConnection servCon;
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
		if (!(activity instanceof ServerCommunication)) {
			throw new IllegalStateException(
					"Activity must implement the ServerCommunication interface.");
		}

		// Hold a reference to the parent Activity so we can report back the
		// task's
		// current progress and results.
		serverCom = (ServerCommunication) activity;
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
			servCon = new ServerConnection(messageType, message);
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
	private class ServerConnection extends AsyncTask<Void, Void, String> {

		String messageType;
		String message;

		public ServerConnection(String messageType, String message) {

			this.messageType = messageType;
			this.message = message;
		}

		/**
		 * Note that we do NOT call the callback object's methods directly from
		 * the background thread, as this could result in a race condition.
		 */
		@Override
		protected String doInBackground(Void... ignore) {
			String reply = "";

			try {

				Socket sock = new Socket(IPADDR, PORT);

				Log.i("Connected to", sock.getInetAddress().toString());
				// Do o.println to write to send the server data
				PrintWriter output = new PrintWriter(new OutputStreamWriter(
						sock.getOutputStream()), true);

				// Receive data through r.readLine
				BufferedReader input = new BufferedReader(
						new InputStreamReader(sock.getInputStream()));

				// sending the message
				output.println(messageType + "\n" + message);

				String response = input.readLine();

				Log.i("Response", response);

				if (messageType.equals("SIGNUP")) {

					String responseCode = input.readLine();
					if (response.equals("OK")) {
						
						
						boolean downloaded = getImage(sock, "QRCODE.png");
						sock.close();
						
						return response + "\n" + responseCode + "\n"
								+ downloaded;
					}

					sock.close();
					return response + "\n" + responseCode;
				}

				else if (messageType.equals("LOGIN")) {

					if (response.equals("USERNAME_ERROR")
							|| response.equals("PASSWORD_ERROR")) {
						response += "\n" + input.readLine();
						sock.close();
						return response;
					}

					String sessionID = input.readLine();
					int numberOfLines = Integer.parseInt(input.readLine());
					String restaurants = "";
					for (int i = 0; i < numberOfLines; i++)
						restaurants += input.readLine() + "\n";

					boolean downloaded = false;
					if (message.split("\n")[2].equals("CODE")){
						downloaded = getImage(sock, "QRCODE.png");
						

					}

					sock.close();
					
					return response + "\n" + sessionID + "\n" + downloaded + "\n" 
							+ numberOfLines + "\n" + restaurants;
				}

				else {

					if (response.equals("EXPIRED")) {
						/*
						 * TODO: automatically re-login
						 */
					}

					if (messageType.equals("FETCHALL")) {

						int numberOfLines = Integer.parseInt(input.readLine());
						String restaurants = "";
						for (int i = 0; i < numberOfLines; i++)
							restaurants += input.readLine() + "\n";

						sock.close();
						return restaurants;

					}
					
					else if (messageType.equals("ADD")) {
						
						//boolean downloadedImage = getImage(sock, message+".png");
						
						sock.close();
						return response;// + "\n" + downloadedImage;

					}

					else if (messageType.equals("IMG")) {

						getImage(sock, message+".png");
						sock.close();

					}
					
					else if (messageType.equals("QR")) {

						getImage(sock, "QRCODE.png");
						sock.close();

					}

				}
				sock.close();

			} catch (UnknownHostException e) {
				reply = "Unable to connect to server 1";
			} catch (IOException e) {
				reply = "Unable to connect to server 2";
			}

			return reply;
		}

		@Override
		protected void onPostExecute(String result) {
			isRunning = false;
			// Proxy the call to the Activity.
			serverCom.getResult(messageType, result);
		}

		private boolean getImage(Socket socket, String imageName){
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(socket
						.getInputStream());
				FileOutputStream out = getActivity().openFileOutput(imageName, Context.MODE_PRIVATE);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e){
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

		void getResult(String messageType, String result);

	}

}
