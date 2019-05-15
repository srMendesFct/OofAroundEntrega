package pt.oofaround.util;

public class LocationData {

	public String tokenID;
	public String usernameR;
	public String role;
	public String name;
	public String description;
	public String address;
	public double latitude;
	public double longitude;
	public String category;
	// Open hours and cupons later

	public LocationData() {
	}

	public LocationData(String tokenID, String usernameR, String role, String name, String description, String address,
			String category, String longitude, String latitude) {
		this.tokenID = tokenID;
		this.usernameR = usernameR;
		this.role = role;
		this.name = name;
		this.description = description;
		this.address = address;
		this.category = category;
		this.latitude = Double.parseDouble(latitude);
		this.longitude = Double.parseDouble(longitude);
	}

	public LocationData(String tokenID, String usernameR, String role, String name) {
		this.tokenID = tokenID;
		this.usernameR = usernameR;
		this.role = role;
		this.name = name;
	}

}
