import java.util.ArrayList;

import com.mongodb.BasicDBObject;

public class Client extends BasicDBObject {

	private static final long serialVersionUID = -3939654042971892268L;
	public Client(String name, String address, String type, String username,
			String password, String emailID, ArrayList<Reward> rewards, String userID) {

		put(NAME, name);
		put(ADDRESS, address);
		put(TYPE, type);
		put(USERNAME, username);
		put(PASSWORD, password);
		put(EMAIL, emailID);
		put(REWARDS, rewards);
		put(USER_ID, userID);
		
	}

	// Basic information tags
	public final static String NAME = "NAME";
	public final static String ADDRESS = "ADDR";
	public final static String TYPE = "TYPE";
	public final static String USER_ID = "CID";

	// Login information tags
	public final static String USERNAME = "UNAME";
	public final static String PASSWORD = "PWORD";
	public final static String EMAIL = "EM";

	// Reward tags
	public final static String REWARDS = "RWRD_LIST";

	// Session tags
	public final static String SESSION_ID = "cl_session_id";
	public final static String SESSION_USERNAME = "session_username";
	public final static String SESSION_TIME = "session_time";
	public final static long SESSION_TIME_OUT = 300000000;

	// Sizes
	public final static int SESSION_ID_SIZE = 32;
	public final static int ID_SIZE = 32;

}
