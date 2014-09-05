import com.mongodb.BasicDBObject;

public class Card extends BasicDBObject {

	private static final long serialVersionUID = -4080018376401077464L;

	public static final String LAST_VISITED = "LV";
	public static final String USER_ID = "UID";
	public static final String CLIENT_ID = "CID";
	public static final String POINTS = "PTS";
	
	public Card(String userID, String clientID){
		put(USER_ID, userID);
		put(CLIENT_ID, clientID);
	}
	
}
