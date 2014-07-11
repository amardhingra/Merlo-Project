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
	DBCollection users, sessionIDS, restaurants;
	MessageDigest hasher;
	QRCodeWriter writer;
	SessionID gen;

	public HandleUser(Socket userSocket, DBCollection usersDB,
			DBCollection sessionIDSDB, DBCollection restDB, MessageDigest hash) {

		writer = new QRCodeWriter();
		socket = userSocket;
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

	}

	public void run() {

		try {

			// Get the option and display it to the screen
			String option = input.readLine();
			System.out.println(option);

			/**
			 * HANDLING SIGNUP REQUESTS
			 */
			if (option.equals("SIGNUP")) {

				String username = input.readLine();
				String password = input.readLine();
				String emailID = input.readLine();

				BasicDBObject usernameQuery = new BasicDBObject(User.USERNAME,
						username);
				BasicDBObject emailQuery = new BasicDBObject(User.EMAIL,
						emailID);

				// checking if the username has been taken
				if (users.count(usernameQuery) != 0)
					output.write("USERNAME_ERROR\nTAKEN\n");

				// checking if the email id has been taken
				else if (users.count(emailQuery) != 0)
					output.write("EMAIL_ERROR\nTAKEN\n");

				// if we've reached this point we have a valid new user
				else {

					// generating the userid and session id
					String uid = getUniqueUserID();
					String sessionID = getUniqueSessionID();

					// add the sessionID to the database
					saveSessionID(sessionID, username);

					// creating a new mongoDB entry and saving the data
					BasicDBObject newUser = new BasicDBObject();
					newUser.put(User.USERNAME, username);
					newUser.put(User.PASSWORD, hash(password));
					newUser.put(User.EMAIL, emailID);
					newUser.put(User.RESTAURANTS, new BasicDBObject());
					newUser.put(User.USER_ID, uid);
					newUser.put(User.RESTAURANT_LIST, new ArrayList<String>());
					users.insert(newUser);

					// sending the response back to the user
					output.write("OK\n" + sessionID + "\n");
					output.flush();

					// generating and saving the qr code to the system
					BitMatrix byteMatrix = writer.encode(uid + "",
							BarcodeFormat.QR_CODE, 600, 600);

					BufferedImage img = MatrixToImageWriter
							.toBufferedImage(byteMatrix);
					ImageIO.write(img, Const.QR_IMAGE_TYPE, new File("users/" + uid
							+ ".png"));

					// sending the user their QR code
					ImageIO.write(img, "png", socket.getOutputStream());

				}
			}

			/**
			 * HANDLING LOGIN REQUESTS
			 */
			else if (option.equals("LOGIN")) {

				// get the username and password
				String username = input.readLine();
				String password = input.readLine();
				String qrNeeded = input.readLine();

				// getting a cursor to the user from the db
				DBCursor userSearch = users.find(new BasicDBObject(
						User.USERNAME, username));

				// checking if the username exists
				if (userSearch.count() == 0) {
					output.write("USERNAME_ERROR\nINVALID\n");
				}

				else {
					// getting the actual user entry since we know the user
					// exists
					BasicDBObject user = (BasicDBObject) userSearch.next();

					// checking the password matches the hashed password
					if (((String) user.get(User.PASSWORD))
							.equals(hash(password))) {

						// generating a session ID and saving it
						String sessionID = getUniqueSessionID();
						saveSessionID(sessionID, username);
						// sending the text data
						output.write("OK\n" + sessionID + "\n"
								+ getAllRestaurants(user));
						output.flush();

						// sending the qr code if required
						if (qrNeeded.equals("CODE"))
							ImageIO.write(
									ImageIO.read(new File("users/"
											+ user.get(User.USER_ID) + ".png")),
									Const.QR_IMAGE_TYPE, socket.getOutputStream());
					}

					else
						output.write("PASSWORD_ERROR\nINVALID\n");
				}
			}

			else {

				/**
				 * VERIFYING USERS
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
					 * USER WANTS ALL RESTAURANTS
					 */
					if (option.equals("FECTHALL"))
						output.write(getAllRestaurants(user));

					/**
					 * ADDING A RESTAURANT TO THE USERS LIST OF RESTAURANTS
					 */
					// adding a restaurant to a users list
					else if (option.equals("ADD")) {

						// get the restaurant name
						String restaurantName = input.readLine();

						// checking if restaurant exists
						if (restaurants.find(
								new BasicDBObject("name", restaurantName))
								.count() > 0) {

							// updating the list of restaurants for the user
							@SuppressWarnings("unchecked")
							ArrayList<String> restList = (ArrayList<String>) user
									.get(User.RESTAURANT_LIST);
							restList.add(restaurantName);

							user.put(User.RESTAURANT_LIST, restList);

							// creating and adding a new DBObject
							// representing a restaurant
							BasicDBObject userRests = (BasicDBObject) user
									.get(User.RESTAURANTS);
							userRests.put(restaurantName, new Integer(0));
							user.put(User.RESTAURANTS, userRests);

							// updating the user with the new information
							users.update(
									users.find(
											new BasicDBObject(User.USERNAME,
													username)).next(), user);

							// sending acknowledgment
							output.write("OK\n");
							// ImageIO.write(
							// ImageIO.read(new File("images/"
							// + restaurantName + ".png")),
							// QR_IMAGE_TYPE, socket.getOutputStream());
						}

						else
							output.write("INVALID\n");

					}

					/**
					 * SENDING THE USER THIER QRCODE
					 */
					else if (option.equals("QR")) {

						// reading the image into a buffered image and
						// writing
						// it to the socket
						BufferedImage image = ImageIO.read(new File("users/"
								+ (String) user.get(User.USER_ID) + ".png"));
						ImageIO.write(image, "png", socket.getOutputStream());

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
			}

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
			BasicDBObject restGroup = (BasicDBObject) user.get(User.RESTAURANTS);

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
