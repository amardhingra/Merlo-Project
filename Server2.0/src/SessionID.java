import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionID {

	// sessionID tags
	public final static String SESSION_ID = "session_id";
	public final static String SESSION_USERNAME = "session_username";
	public final static String SESSION_TIME = "session_time";

	private SecureRandom random = new SecureRandom();

	public String generateID(int n) {
		return new BigInteger(128, random).toString(n);
	}
}
