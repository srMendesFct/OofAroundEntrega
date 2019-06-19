package pt.oofaround.util;

public class UserData {

	public String username;
	public String password;
	public String email;
	public String country;
	public String cellphone;
	public boolean privacy;
	public String tokenID;
	public String role;
	public String usernameR;
	public String newRole;

	public UserData(String username, String email, String country, String cellphone, boolean privacy, String tokenID,
			String role, String usernameR) {
		this.username = username;
		this.email = email;
		this.country = country;
		this.cellphone = cellphone;
		this.privacy = privacy;
		this.tokenID = tokenID;
		this.role = role;
		this.usernameR = usernameR;
	}

	public UserData() {
	}

	public UserData(String username, String tokenID, String role) {
		this.username = username;
		this.tokenID = tokenID;
		this.role = role;
	}

	// alter other user role
	public UserData(String username, String tokenID, String role, String usernameR, String newRole) {
		this.username = username;
		this.tokenID = tokenID;
		this.role = role;
		this.usernameR = usernameR;
		this.newRole = newRole;
	}

}
