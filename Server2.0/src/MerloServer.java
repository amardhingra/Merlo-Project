import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MerloServer {

	private boolean DEBUG = true;

	// class contains a port number and server socket
	ServerSocket userSocket, clientSocket;

	// ID generators
	SessionID gen;

	// database files
	MongoClient mongoClient;
	DB database;
	DBCollection users;
	DBCollection sessionIDS;
	DBCollection restaurants;
	DBCollection cards;

	// hash code generator
	MessageDigest hasher;

	public MerloServer(int userPortNumber, int clientPortNumber)
			throws FileNotFoundException, UnknownHostException,
			NoSuchAlgorithmException {

		// hash code generator
		hasher = MessageDigest.getInstance("SHA-256");

		// connecting to MongoDB
		mongoClient = new MongoClient();
		database = mongoClient.getDB("Merlo-test");

		// getting databases
		users = database.getCollection("users");
		sessionIDS = database.getCollection("sessionIDs");
		restaurants = database.getCollection("restaurants");
		cards = database.getCollection("cards");
		
		// emptying the databases if we are testing
		if (DEBUG) {
			//users.drop();
			//sessionIDS.drop();
			restaurants.drop();
			cards.drop();
			
			restaurants.insert(new Client("Milano", "2880 Broadway, New York NY", "milano", "jhb32784uhiuehr8q3nnj", "", "", null, "dkjsfnkjdkcbkjsdjkbdsj"));
			restaurants.insert(new Client("Test", "2880 Broadway, New York NY", "milano", "a", "", "", null, "dkjsfnkjdkcbkjsdjkbdsj"));

		}
		
		// attempts to create sockets
		try {
			userSocket = new ServerSocket(userPortNumber);
			clientSocket = new ServerSocket(clientPortNumber);

		} catch (IOException e) {
			System.out.println("Error creating Socket. Exiting");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Server started\n");

	}

	public void run() {

		new AcceptUserConnections(userSocket).start();
		new AcceptClientConnections(clientSocket).start();

	}

	private class AcceptUserConnections extends Thread {

		private ServerSocket sock;

		public AcceptUserConnections(ServerSocket userSocket) {
			sock = userSocket;
			System.out.println("Waiting for user connections");
		}

		public void run() {
			for (;;) {
				try {

					Socket incomingSocket = sock.accept();
					System.out.println("Handling user connection from: "
							+ incomingSocket.getInetAddress().toString() + " at "
							+ new Date());
					new HandleUser(incomingSocket, users, sessionIDS,
							restaurants, cards, hasher).start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	private class AcceptClientConnections extends Thread {

		private ServerSocket sock;

		public AcceptClientConnections(ServerSocket clientSocket) {
			sock = clientSocket;
			System.out.println("Waiting for client connections");
		}

		public void run() {
			for (;;) {
				try {

					Socket incomingSocket = sock.accept();
					System.out.println("Handling client connection from: "
							+ incomingSocket.getInetAddress().toString() + " at "
							+ new Date());
					new HandleClient(incomingSocket, users, sessionIDS,
							restaurants, cards, hasher).start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void main(String[] args) throws FileNotFoundException,
			UnknownHostException, NoSuchAlgorithmException {

		MerloServer server = new MerloServer(2000, 4000);
		server.run();

	}

}
