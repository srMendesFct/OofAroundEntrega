package pt.oofaround.util;

public class ChangePasswordData {
	
	public String password;
	public String oldPassword;
	public String tokenID;
	public String role;
	public String usernameR;

	public ChangePasswordData() {
	}
	
	public ChangePasswordData(String password, String tokenID, String role, String usernameR, String oldPassword) {
		this.password = password;
		this.oldPassword = oldPassword;
		this.tokenID = tokenID;
		this.role = role;
		this.usernameR = usernameR;
	}
}
