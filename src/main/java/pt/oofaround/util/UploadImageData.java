package pt.oofaround.util;

public class UploadImageData {

	public String name;
	public byte[] image;
	public String usernameR;
	public String tokenID;
	public String role;

	public UploadImageData() {
	}

	public UploadImageData(String name, byte[] image, String usernameR, String tokenID, String role) {
		this.name = name;
		this.image = image;
		this.usernameR = usernameR;
		this.tokenID = tokenID;
		this.role = role;
	}
	
	public UploadImageData(String name, String usernameR, String tokenID, String role) {
		this.name = name;
		this.usernameR = usernameR;
		this.tokenID = tokenID;
		this.role = role;
	}

}
