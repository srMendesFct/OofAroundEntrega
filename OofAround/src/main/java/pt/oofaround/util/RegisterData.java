package pt.oofaround.util;

public class RegisterData {

	public String username;
	public String password;
	public String email;
	public String country;
	public String cellphone;
	public boolean privacy;
	public String tokenID;
	public String role;
	public String usernameR;

	public RegisterData() {
	}

	public RegisterData(String username, String password, String email, String country, String cellphone,
			String privacy) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.country = country;
		this.cellphone = cellphone;
		if (privacy.equalsIgnoreCase("true")) {
			this.privacy = true;
		} else {
			this.privacy = false;
		}
	}

	public RegisterData(String username, String password, String email, String country, String cellphone,
			String privacy, String tokenID, String role, String usernameR) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.country = country;
		this.cellphone = cellphone;
		this.tokenID = tokenID;
		this.role = role;
		this.usernameR = usernameR;
		if (privacy.equalsIgnoreCase("true")) {
			this.privacy = true;
		} else {
			this.privacy = false;
		}
	}

}
