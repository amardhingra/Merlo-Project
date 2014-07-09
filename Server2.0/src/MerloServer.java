import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MerloServer {

	private boolean DEBUG = true;

	// class contains a port number and server socket
	int serverPortNumber;
	ServerSocket serverSocket;
	
	//ID generators
	SessionIdentifierGenerator gen;
	private final static int USER_SESSION_ID_SIZE = 32;
	private final static int USER_ID_SIZE = 16;
	private final static int CLIENT_ID_SIZE = 16;

	// database files
	MongoClient mongoClient;
	DB database;
	DBCollection users;
	DBCollection sessionIDS;
	DBCollection restaurants;

	// user tags
	private final static String USERNAME = "username";
	private final static String PASSWORD = "password";
	private final static String EMAIL = "emailID";
	private final static String RESTAURANTS = "restaurants";
	private final static String USER_ID = "user_id";

	// sessionID tags
	private final static String SESSION_ID = "session_id";
	private final static String SESSION_USERNAME = "session_username";
	private final static String SESSION_TIME = "session_time";

	// user RESTAURANT tags
	private final static String RESTAURANT_LIST = "rest_list";
	private final static String REST_POINTS = "rest_points";

	// client tags
	private final static String REST_NAME = "rest_name";
	private final static String REST_ADDR = "rest_addr";
	private final static String REST_TYPE = "rest_type";
	private final static String REST_ID = "rest_id";
	private final static String REST_REWARDS = "rest_rewards";

	// other strings
	private final static String QR_IMAGE_TYPE = "png";

	// constants
	private final static long SESSION_TIME_OUT = 60000;

	// hash code generator
	MessageDigest hasher;

	public MerloServer(int portNumber) throws FileNotFoundException,
			UnknownHostException, NoSuchAlgorithmException {

		gen = new SessionIdentifierGenerator();

		hasher = MessageDigest.getInstance("SHA-256");

		mongoClient = new MongoClient();
		database = mongoClient.getDB("Merlo-test");
		users = database.getCollection("users");
		sessionIDS = database.getCollection("sessionIDs");
		restaurants = database.getCollection("restaurants");

		// emptying the databases if we are testing
		if (DEBUG) {
			users.drop();
			sessionIDS.drop();
			restaurants.drop();
			restaurants.insert(new BasicDBObject("name", "test"));
		}

		// sets port number
		serverPortNumber = portNumber;

		// attempts to create socket
		try {
			serverSocket = new ServerSocket(serverPortNumber);
		} catch (IOException e) {
			System.out.println("Error creating Socket. Exiting");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Server started");

	}

	public void run() {

		// serve endlessly
		for (;;) {

			try {

				// accepting incoming connections and handling them
				// on separate threads
				Socket clientSocket = serverSocket.accept();
				System.out.println("Handling connection from: "
						+ clientSocket.getInetAddress().toString() + " at "
						+ new Date());
				new HandleClient(clientSocket).start();

			} catch (IOException e) {
				System.out.println("User connection failed");
				e.printStackTrace();
			}

		}

	}

	private class HandleClient extends Thread {

		// contains a socket, buffered reader and printwriter
		Socket socket;
		BufferedReader input;
		PrintWriter output;

		public HandleClient(Socket clientSocket) {

			socket = clientSocket;

			try {

				// creating the input and output streams
				input = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);

			} catch (IOException e) {
				System.out.println("Unable to communicate with client");
				e.printStackTrace();
			}

		}

		public void run() {

			try {

				// Get the option and display it to the screen
				String option = input.readLine();
				System.out.println(option);

				
				if (option.equals("CLIENT")){
					
					String nextLine = input.readLine();
					
					if (nextLine.equals("CREATE")){
						
						String name = input.readLine();
						String address = input.readLine();
						String type = input.readLine();
						String username = input.readLine();
						String password = hash(input.readLine());
						String email = input.readLine();
						int numberOfRewards = Integer.parseInt(input.readLine());
						ArrayList<String> rewards = new ArrayList<String>();
						for (int i = 0; i < numberOfRewards; i++)
							rewards.add(input.readLine());
						
						//TODO: check if exists
						
						BasicDBObject newRest = new BasicDBObject();
						newRest.put(USERNAME, username);
						newRest.put(PASSWORD, password);
						newRest.put(EMAIL, email);
						newRest.put(REST_ID, getUniqueClientID());
						newRest.put(REST_NAME, name);
						newRest.put(REST_ADDR, address);
						newRest.put(REST_TYPE, type);
						newRest.put(REST_REWARDS, rewards);
						restaurants.insert(newRest);
						
					}
					/*
					 * TODO: MOVE THIS TO SECOND SERVER
					 */

					// Adding a point to a restaurant that already exists
					else if (option.equals("ADD_POINT")) {
/*
						// Getting the name of the restaurant
						String restaurantName = input.readLine();

						// Getting the restaurant and updating the number of
						// points
						BasicDBObject userRests = (BasicDBObject) user
								.get(RESTAURANTS);
						BasicDBObject rest = (BasicDBObject) userRests
								.get(restaurantName);
						rest.put(
								REST_POINTS,
								new Integer(
										(Integer) rest.get(REST_POINTS) + 1));

						// Putting the updating entries back into the
						// database
						userRests.put(restaurantName, rest);
						user.put(RESTAURANTS, userRests);
						users.update(
								users.find(
										new BasicDBObject(USERNAME,
												username)).next(), user);

						// Sending acknowledgement
						output.write("OK\n"
								+ ((Integer) rest.get(REST_POINTS)) + "\n");*/
					}
					
					
				}
				/**
				 * HANDLING SIGNUP REQUESTS
				 */
				if (option.equals("SIGNUP")) {

					String username = input.readLine();
					String password = input.readLine();
					String emailID = input.readLine();

					BasicDBObject usernameQuery = new BasicDBObject(USERNAME,
							username);
					BasicDBObject emailQuery = new BasicDBObject(EMAIL, emailID);

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

						// saving the session ID
						BasicDBObject sessionTok = new BasicDBObject();
						sessionTok.put(SESSION_ID, sessionID);
						sessionTok.put(SESSION_USERNAME, username);
						sessionTok
								.put(SESSION_TIME, System.currentTimeMillis());
						sessionIDS.insert(sessionTok);

						// generating and saving the qr code to the system
						QRCodeWriter writer = new QRCodeWriter();
						BitMatrix byteMatrix = writer.encode(uid + "",
								BarcodeFormat.QR_CODE, 600, 600);

						BufferedImage img = MatrixToImageWriter
								.toBufferedImage(byteMatrix);
						ImageIO.write(img, QR_IMAGE_TYPE, new File("users/"
								+ uid + ".png"));

						// creating a new mongoDB entry and saving the data
						BasicDBObject newUser = new BasicDBObject();
						newUser.put(USERNAME, username);
						newUser.put(PASSWORD, hash(password));
						newUser.put(EMAIL, emailID);
						newUser.put(RESTAURANTS, new BasicDBObject());
						newUser.put(USER_ID, uid);
						newUser.put(RESTAURANT_LIST, new ArrayList<String>());
						users.insert(newUser);

						// sending the response back to the user
						output.write("OK\n" + sessionID + "\n");
						output.flush();

						// sending the user their QR code
						ImageIO.write(img, "png", socket.getOutputStream());

					}
				}

				/**
				 * 
				 * HANDLING LOGIN REQUESTS
				 * 
				 */
				else if (option.equals("LOGIN")) {

					// get the username and password
					String loginName = input.readLine();
					String password = input.readLine();
					String qrNeeded = input.readLine();

					// getting a cursor to the user from the db
					DBCursor userSearch = users.find(new BasicDBObject(
							USERNAME, loginName));

					// checking if the username exists
					if (userSearch.count() == 0) {
						output.write("USERNAME_ERROR\nINVALID\n");
					}

					else {
						// getting the actual user entry since we know the user
						// exists
						BasicDBObject user = (BasicDBObject) userSearch.next();

						// checking the password matches the hashed password
						if (((String) user.get(PASSWORD))
								.equals(hash(password))) {

							// generating a session ID and saving it
							String sessionID = getUniqueSessionID();
							BasicDBObject sessionTok = new BasicDBObject();
							sessionTok.put(SESSION_ID, sessionID);
							sessionTok
									.put(SESSION_USERNAME, user.get(USERNAME));
							sessionTok.put(SESSION_TIME,
									System.currentTimeMillis());
							sessionIDS.insert(sessionTok);

							// sending the text data
							output.write("OK\n" + sessionID + "\n"
									+ getAllRestaurants(user));
							output.flush();

							// sending the qr code if required
							System.out.println(qrNeeded);
							if (qrNeeded.equals("CODE"))
								ImageIO.write(
										ImageIO.read(new File("users/"
												+ user.get(USER_ID) + ".png")),
										QR_IMAGE_TYPE, socket.getOutputStream());
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

					DBCursor sessionSearch = sessionIDS.find(new BasicDBObject(
							SESSION_ID, sessionID));

					boolean isValidSessionID = false;
					// checking for valid session ID
					// make sure there is one entry
					if (sessionSearch.count() == 1) {
						BasicDBObject sessionTok = (BasicDBObject) sessionSearch
								.next();

						// make sure username matches
						// make sure time is less than Session timeout
						if (username.equals((String) sessionTok
								.get(SESSION_USERNAME)))
							if (System.currentTimeMillis()
									- ((Long) sessionTok.get(SESSION_TIME)) < SESSION_TIME_OUT) {

								// if all conditions are met we have a valid
								// session id
								isValidSessionID = true;
							}
					}

					// verifying that they have a valid sessionID and it is
					// within the time
					if (isValidSessionID) {

						/**
						 * GETTING THE DBObject REPRESENTING THE USER
						 */
						// getting the user object
						DBObject user = users.find(
								new BasicDBObject(USERNAME, username)).next();

						// sending all the restaurants
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
									new BasicDBObject("name", restaurantName)).count() > 0) {

								// updating the list of restaurants for the user
								@SuppressWarnings("unchecked")
								ArrayList<String> restList = (ArrayList<String>) user
										.get(RESTAURANT_LIST);
								restList.add(restaurantName);
								user.put(RESTAURANT_LIST, restList);

								// creating and adding a new DBObject
								// representing a restaurant
								BasicDBObject userRests = (BasicDBObject) user
										.get(RESTAURANTS);
								userRests.put(restaurantName,
										new BasicDBObject(REST_POINTS,
												new Integer(0)));
								user.put(RESTAURANTS, userRests);

								// updating the user with the new information
								users.update(
										users.find(
												new BasicDBObject(USERNAME,
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

						else if (option.equals("QR")) {

							// reading the image into a buffered image and
							// writing
							// it to the socket
							BufferedImage image = ImageIO.read(new File(
									"users/" + (String) user.get(USER_ID)
											+ ".png"));
							ImageIO.write(image, "png",
									socket.getOutputStream());

						}

						/**
						 * HADLING IMAGE REQUESTS
						 */

						else if (option.equals("IMG")) {

							// getting the image name
							String imageName = input.readLine();

							// reading the image into a buffered image and
							// writing
							// it to the socket
							BufferedImage image = ImageIO.read(new File(
									"images/" + imageName + ".png"));
							ImageIO.write(image, "png",
									socket.getOutputStream());

						}

					} else
						output.write("EXPIRED\n");

				}

				output.flush();
				socket.close();

				if (!DEBUG) {
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

			String sessionID = gen.nextSessionId(USER_SESSION_ID_SIZE);

			// making sure the generated session ID doesn't already exist
			while (sessionIDS.find(new BasicDBObject(SESSION_ID, sessionID))
					.count() > 0)
				sessionID = gen.nextSessionId(USER_SESSION_ID_SIZE);

			return sessionID;

		}
		
		private String getUniqueUserID() {

			String sessionID = gen.nextSessionId(USER_ID_SIZE);

			// making sure the generated session ID doesn't already exist
			while (users.find(new BasicDBObject(USER_ID, sessionID))
					.count() > 0)
				sessionID = gen.nextSessionId(USER_ID_SIZE);

			return sessionID;

		}
		
		private String getUniqueClientID() {

			String sessionID = gen.nextSessionId(CLIENT_ID_SIZE);

			// making sure the generated session ID doesn't already exist
			while (users.find(new BasicDBObject(REST_ID, sessionID))
					.count() > 0)
				sessionID = gen.nextSessionId(CLIENT_ID_SIZE);

			return sessionID;

		}

		private String getAllRestaurants(DBObject user) {

			// getting the arraylist of the names of all the restaurants
			@SuppressWarnings("unchecked")
			ArrayList<String> restList = (ArrayList<String>) user
					.get(RESTAURANT_LIST);

			// if there are no restaurants return 0
			if (restList.size() == 0)
				return "0\n";

			// otherwise we have to format each restaurant
			else {
				// the first line indicates the number of restaurants
				String allRestaurants = restList.size() + "\n";

				// get the group of restaurants
				BasicDBObject restGroup = (BasicDBObject) user.get(RESTAURANTS);

				// iterating through the list of restaurant names
				for (String restaurant : restList) {
					// getting each restaurant
					BasicDBObject rest = (BasicDBObject) restGroup
							.get(restaurant);

					// adding the formatted string to the overall result
					allRestaurants += restaurant + " " + rest.get(REST_POINTS)
							+ "\n";
				}
				return allRestaurants;
			}
		}

	}

	public static void main(String[] args) throws FileNotFoundException,
			UnknownHostException, NoSuchAlgorithmException {

		MerloServer server = new MerloServer(2000);
		server.run();

	}

}
