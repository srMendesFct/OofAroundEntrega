package pt.oofaround.util;

public class UploadImageData {

	public String name;
	public byte[] image;
	
	public UploadImageData(){
	}

	public UploadImageData(String name, byte[] image) {
		this.name = name;
		this.image = image;
	}
}
