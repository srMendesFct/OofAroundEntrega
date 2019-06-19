package pt.oofaround.util;

public class CreateRouteData {

	public String placeId;
	public String name;
	public double latitude;
	public double longitude;
	public String category;

	public CreateRouteData() {
	}

	public CreateRouteData(String placeId, String name, double latitude, double longitude, String category) {
		this.placeId = placeId;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.category = category;
	}

}
