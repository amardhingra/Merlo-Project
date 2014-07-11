import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;

import com.google.zxing.qrcode.QRCodeWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class HandleClient extends Thread {

	// Data types
	Socket socket;
	BufferedReader input;
	PrintWriter output;
	DBCollection users, sessionIDS, restaurants;
	MessageDigest hasher;
	QRCodeWriter writer;
	SessionID gen;

	public HandleClient(Socket clientSocket, DBCollection usersDB,
			DBCollection sessionIDSDB, DBCollection restDB, MessageDigest hash) {

		writer = new QRCodeWriter();

		socket = clientSocket;
		this.users = usersDB;
		this.sessionIDS = sessionIDSDB;
		this.restaurants = restDB;
		this.hasher = hash;
		gen = new SessionID();

		try {
			// creating the input and output streams
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);

		} catch (IOException e) {
			System.out.println("Unable to communicate with client");
		}

		System.out.println("HandleClient Thread created");
	}

	public void run() {

		System.out.println("handling client");
		try {

			// Get the option and display it to the screen
			String option = input.readLine();
			System.out.println(option);

			/**
			 * HANDLING SIGNUP REQUESTS
			 */
			if (option.equals("SIGNUP")) {

				String name = input.readLine();
				String address = input.readLine();
				String type = input.readLine();
				String username = input.readLine();
				String password = hash(input.readLine());
				String email = input.readLine();
				ArrayList<String> rewards = new ArrayList<String>();
				int number = Integer.parseInt(input.readLine());
				for (int i = 0; i < number; i++) {
					rewards.add(input.readLine());
				}
				String sessionID = getUniqueSessionID();
				String userID = getUniqueUserID();

				saveSessionID(sessionID, username);

				BasicDBObject newClient = new BasicDBObject();
				newClient.put(Client.NAME, name);
				newClient.put(Client.ADDRESS, address);
				newClient.put(Client.TYPE, type);
				newClient.put(Client.USERNAME, username);
				newClient.put(Client.PASSWORD, password);
				newClient.put(Client.EMAIL, email);
				newClient.put(Client.REWARDS, rewards);
				newClient.put(Client.SESSION_ID, sessionID);
				newClient.put(Client.USER_ID, userID);

				restaurants.insert(newClient);

				output.write("OK\n" + sessionID);

			} else if (option.equals("LOGIN")) {

				String username = input.readLine();
				String password = hash(input.readLine());

				BasicDBObject query = new BasicDBObject(Client.USERNAME,
						username);

				DBCursor search = restaurants.find(query);

				if (search.count() == 1) {

					DBObject user = search.next();
					if (((String) user.get(Client.PASSWORD)).equals(password)) {
						
						String sessionID = getUniqueSessionID();
						saveSessionID(sessionID, username);
						
						output.write("OK\n" + sessionID);
						
					} else {
						output.write("INVALID\nPASSWORD");
					}

				} else {
					output.write("INVALID\nUSERNAME");
				}

			}

			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private String getUniqueSessionID() {

		String sessionID = gen.generateID(Client.SESSION_ID_SIZE);

		// making sure the generated session ID doesn't already exist
		while (sessionIDS.find(new BasicDBObject(Client.SESSION_ID, sessionID))
				.count() > 0)
			sessionID = gen.generateID(Client.SESSION_ID_SIZE);

		return sessionID;

	}

	private String getUniqueUserID() {

		String sessionID = gen.generateID(Client.ID_SIZE);

		// making sure the generated session ID doesn't already exist
		while (restaurants.find(new BasicDBObject(Client.USER_ID, sessionID))
				.count() > 0)
			sessionID = gen.generateID(Client.ID_SIZE);

		return sessionID;

	}

	private void saveSessionID(String sessionID, String username) {

		// saving the session ID
		BasicDBObject sessionTok = new BasicDBObject();
		sessionTok.put(Client.SESSION_ID, sessionID);
		sessionTok.put(Client.USERNAME, username);
		sessionTok.put(Client.SESSION_TIME, System.currentTimeMillis());
		sessionIDS.insert(sessionTok);

	}

	private String hash(String toHash) {

		try {
			byte[] hash = hasher.digest(toHash.getBytes("UTF-8"));

			// convert the byte to hex format method 1
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
			return sb.toString();

		} catch (Exception e) {
		}

		return null;
	}
}
