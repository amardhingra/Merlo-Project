import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class HandleUser extends Thread {

	private boolean DEBUG = true;

	// Data types
	Socket socket;
	BufferedReader input;
	PrintWriter output;
	DBCollection users, sessionIDS, restaurants, cards;
	MessageDigest hasher;
	QRCodeWriter writer;
	SessionID gen;

	public HandleUser(Socket userSocket, DBCollection usersDB,
			DBCollection sessionIDSDB, DBCollection restDB,
			DBCollection cardsDB, MessageDigest hash) {

		writer = new QRCodeWriter();
		socket = userSocket;
		this.users = usersDB;
		this.sessionIDS = sessionIDSDB;
		this.restaurants = restDB;
		this.cards = cardsDB;
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

	}

	public void run() {

		try {

			// Get the option and display it to the screen
			String option = input.readLine();
			System.out.println(option);

			/**
			 * TODO:HANDLING SIGNUP REQUESTS
			 */
			if (option.equals("SIGNUP")) {

				String username = input.readLine();
				String password = input.readLine();
				String emailID = input.readLine();
				/*
				 * BasicDBObject usernameQuery = new
				 * BasicDBObject(User.USERNAME, username); BasicDBObject
				 * emailQuery = new BasicDBObject(User.EMAIL, emailID);
				 */
				// checking if the username has been taken
				if (users.count(new BasicDBObject(User.USERNAME, username)) != 0)
					output.write("ERR\nUSERNAME\n");

				// checking if the email id has been taken
				else if (users.count(new BasicDBObject(User.EMAIL, emailID)) != 0)
					output.write("ERR\nEMAIL\n");

				// if we've reached this point we have a valid new user
				else {

					// generating the userid and session id
					String uid = getUniqueUserID();
					String sessionID = getUniqueSessionID();

					// add the sessionID to the database
					saveSessionID(sessionID, username);

					// creating a new mongoDB entry and saving the data
					users.insert(new User(username, hash(password), emailID,
							uid));

					// sending the response back to the user
					output.write("OK\n" + sessionID + "\n");
					output.flush();

					// generating and saving the qr code to the system
					BitMatrix byteMatrix = writer.encode(uid + "",
							BarcodeFormat.QR_CODE, 600, 600);

					BufferedImage img = MatrixToImageWriter
							.toBufferedImage(byteMatrix);
					ImageIO.write(img, Const.QR_IMAGE_TYPE, new File("users/"
							+ uid + ".png"));

				}
			}

			/**
			 * TODO: HANDLING LOGIN REQUESTS
			 */
			else if (option.equals("LOGIN")) {

				// get the username and password
				String username = input.readLine();
				String password = hash(input.readLine());

				// getting a cursor to the user from the db
				DBCursor userSearch = users.find(new BasicDBObject(
						User.USERNAME, username));

				// checking if the username exists
				if (userSearch.count() == 0) {
					output.write("ERR\nUSERNAME\n");
				}

				else {
					// getting the actual user entry since we know the user
					// exists
					BasicDBObject user = (BasicDBObject) userSearch.next();

					// checking the password matches the hashed password
					if (user.getString(User.PASSWORD).equals(password)) {

						// generating a session ID and saving it
						String sessionID = getUniqueSessionID();
						saveSessionID(sessionID, username);
						// sending the text data
						output.write("OK\n" + sessionID + "\n");
						output.flush();
					} else
						output.write("ERR\nPASSWORD\n");
				}
			}

			else {

				/**
				 * TODO: VERIFYING USERS
				 */

				// if they aren't logging in or signing up we need to verify
				// them
				String username = input.readLine();
				String sessionID = input.readLine();

				boolean isValidSessionID = validateSessionID(sessionID,
						username);

				// verifying that they have a valid sessionID and it is
				// within the time
				if (isValidSessionID) {

					// getting the user object
					DBObject user = users.find(
							new BasicDBObject(User.USERNAME, username)).next();

					/**
					 * TODO: ADDING A RESTAURANT TO THE USERS LIST OF
					 * RESTAURANTS
					 */
					if (option.equals("FIND")) {

						String query = input.readLine();
						String response = "";

						DBCursor c;
						// if the user wants all blank query
						if (query.equals("ALL")) {
							c = restaurants.find();
						} else {
							// search for words which contain the phrase
							c = restaurants.find(new BasicDBObject(Client.NAME,
									new BasicDBObject("$regex", query)));
						}
						
						// adding each name to the string
						while (c.hasNext())
							response += ((BasicDBObject) c.next())
									.getString(Client.NAME) + ",";

						System.out.println(response);

						output.write("OK\n"
								+ response.substring(0, response.length() - 1)
								+ "\n");
						output.flush();
					}

					// TODO: adding a restaurant to a users list
					else if (option.equals("ADD")) {

						// get the restaurant name
						String restaurantName = input.readLine();

						String userID = (String) users.findOne(
								new BasicDBObject(User.USERNAME, username))
								.get(User.USER_ID);

						String clientID = (String) restaurants.findOne(
								new BasicDBObject(Client.NAME, restaurantName))
								.get(Client.USER_ID);

						Card nc = new Card(userID, clientID);

						if (cards.find(nc).count() == 0) {
							nc.put(Card.POINTS, 0);
							nc.put(Card.LAST_VISITED, 0);
							cards.insert(nc);
							output.write("OK\n" + restaurantName);
							output.flush();
						}

						else {
							output.write("TAKEN\n" + restaurantName);
							output.flush();
						}
					}
					/**
					 * TODO: SENDING THE USER THIER QRCODE
					 */
					else if (option.equals("QR")) {

						System.out.println("Sending QR");

						// reading the image into a buffered image and
						// writing it to the socket
						String filePath = "users/"
								+ (String) user.get(User.USER_ID) + ".png";

						BufferedImage image;
						boolean sent = false;
						while (sent)
							try {
								image = ImageIO.read(new File(filePath));
								ImageIO.write(image, "png",
										socket.getOutputStream());
								sent = true;
							} catch (IOException e) {
							}

					}

					/**
					 * HADLING IMAGE REQUESTS
					 */

					else if (option.equals("IMG")) {

						// getting the image name
						String imageName = input.readLine();

						// reading the image into a buffered image and
						// writing it to the socket
						BufferedImage image = ImageIO.read(new File("images/"
								+ imageName + ".png"));
						ImageIO.write(image, "png", socket.getOutputStream());

					}

				} else
					output.write("EXPIRED\n");

			}

			output.flush();
			socket.close();

			if (DEBUG) {
				DBCursor cursor = users.find();
				while (cursor.hasNext())
					System.out.println(cursor.next());

				cursor = sessionIDS.find();
				while (cursor.hasNext())
					System.out.println(cursor.next());

				System.out.println("CARDS");
				cursor = cards.find();
				while (cursor.hasNext())
					System.out.println(cursor.next());

				cursor = restaurants.find();
				while (cursor.hasNext())
					System.out.println(cursor.next());
			}
			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriterException e) {
			e.printStackTrace();
		}

	}

	private boolean validateSessionID(String sessionID, String username) {

		BasicDBObject sessionToken = new BasicDBObject();
		sessionToken.put(User.SESSION_ID, sessionID);
		sessionToken.put(User.SESSION_USERNAME, username);

		DBCursor sessionSearch = sessionIDS.find(sessionToken);

		// checking for valid session ID
		// make sure there is one entry
		if (sessionSearch.count() == 1) {
			BasicDBObject sessionTok = (BasicDBObject) sessionSearch.next();

			// make sure time is less than Session timeout
			if (System.currentTimeMillis()
					- ((Long) sessionTok.get(User.SESSION_TIME)) < User.SESSION_TIME_OUT) {
				// if all conditions are met we have a valid
				// session id
				return true;
			} else {
				// otherwise discard the saved session token
				sessionIDS.remove(sessionTok);
			}
		}

		return false;
	}

	private void saveSessionID(String sessionID, String username) {

		BasicDBObject userTokens = new BasicDBObject(User.SESSION_USERNAME,
				username);
		sessionIDS.findAndRemove(userTokens);

		// saving the session ID
		BasicDBObject sessionTok = new BasicDBObject();
		sessionTok.put(User.SESSION_ID, sessionID);
		sessionTok.put(User.SESSION_USERNAME, username);
		sessionTok.put(User.SESSION_TIME, System.currentTimeMillis());
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

	private String getUniqueSessionID() {

		String sessionID = gen.generateID(User.SESSION_ID_SIZE);

		// making sure the generated session ID doesn't already exist
		while (sessionIDS.find(new BasicDBObject(User.SESSION_ID, sessionID))
				.count() > 0)
			sessionID = gen.generateID(User.SESSION_ID_SIZE);

		return sessionID;

	}

	private String getUniqueUserID() {

		String sessionID = gen.generateID(User.ID_SIZE);

		// making sure the generated session ID doesn't already exist
		while (users.find(new BasicDBObject(User.USER_ID, sessionID)).count() > 0)
			sessionID = gen.generateID(User.ID_SIZE);

		return sessionID;

	}

	private String getAllRestaurants(DBObject user) {

		// getting the arraylist of the names of all the restaurants
		@SuppressWarnings("unchecked")
		ArrayList<String> restList = (ArrayList<String>) user
				.get(User.RESTAURANT_LIST);

		// if there are no restaurants return 0
		if (restList.size() == 0)
			return "0\n";

		// otherwise we have to format each restaurant
		else {
			// the first line indicates the number of restaurants
			String allRestaurants = restList.size() + "\n";

			// get the group of restaurants
			BasicDBObject restGroup = (BasicDBObject) user
					.get(User.RESTAURANTS);

			// iterating through the list of restaurant names
			for (String restaurant : restList) {
				// getting each restaurant
				BasicDBObject rest = (BasicDBObject) restGroup.get(restaurant);

				// adding the formatted string to the overall result
				allRestaurants += restaurant + " " + rest.get(User.REST_POINTS)
						+ "\n";
			}
			return allRestaurants;
		}
	}

}
