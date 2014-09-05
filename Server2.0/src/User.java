import com.mongodb.BasicDBObject;

public class User extends BasicDBObject{

	private static final long serialVersionUID = 6586133803631010225L;

	public User(String username, String password, String email, String userID){
		
		put(USERNAME, username);
		put(PASSWORD, password);
		put(EMAIL, email);
		put(USER_ID, userID);
		
	}
	
	// basic information
	public final static String RESTAURANTS = "restaurants";
	public final static String RESTAURANT_LIST = "rest_list";
	public final static String REST_POINTS = "rest_points";

	// login tags
	public final static String USERNAME = "username";
	public final static String PASSWORD = "password";
	public final static String EMAIL = "emailID";
	public final static String USER_ID = "user_id";

	// sessionID tags
	public final static String SESSION_ID = "session_id";
	public final static String SESSION_USERNAME = "session_username";
	public final static String SESSION_TIME = "session_time";
	public final static long SESSION_TIME_OUT = 300000;

	// ID generators tags
	public final static int SESSION_ID_SIZE = 32;
	public final static int ID_SIZE = 32;

}
