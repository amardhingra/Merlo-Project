
public class Identifier {

	int hashCode;
	int userID;
    String emailID;
	
	public Identifier(String identifier){
		String [] split = identifier.split(" ");
		hashCode = Integer.parseInt(split[0]);
		emailID = split[1];
		userID = Integer.parseInt(split[2]);
	}

	public Identifier(int hc, String email, int uid) {
		this.hashCode = hc;
		emailID = email;
		this.userID = uid;
	}

	public int getHashCode() {return hashCode;}
	public int getUserID() { return userID;}
}
