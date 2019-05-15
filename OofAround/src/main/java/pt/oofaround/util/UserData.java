package pt.oofaround.util;

public class UserData {

	public String username;
	public String tokenID;
	public String role;
	public String usernameR;

	public UserData() {
	}

	public UserData(String username, String tokenID, String role) {
		this.username = username;
		this.tokenID = tokenID;
		this.role = role;
	}

	public UserData(String username, String tokenID, String role, String usernameR) {
		this.username = username;
		this.tokenID = tokenID;
		this.role = role;
		this.usernameR = usernameR;
	}

}
