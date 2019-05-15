package pt.oofaround.util;

import java.nio.charset.StandardCharsets;
import org.glassfish.jersey.internal.util.Base64;
import com.google.common.hash.Hashing;

public class AuthToken {

	private static final String SECRET = "99999996289";

	private long creationDate;
	private String hashedKey;
	private String payload;
	public String username;
	public String role;
	public long expirationDate;
	public String tokenID;

	public static final long EXPIRATION_TIME = 300000;

	public AuthToken() {

	}

	public AuthToken(String username, String role) {
		this.username = username;
		this.creationDate = System.currentTimeMillis();
		this.expirationDate = this.creationDate + AuthToken.EXPIRATION_TIME;
		this.role = role;
		this.payload = Base64.encodeAsString(username) + Base64.encodeAsString(role);
		this.hashedKey = Hashing.hmacSha512(SECRET.getBytes()).hashString(payload, StandardCharsets.UTF_8).toString();
		this.tokenID = this.payload + "." + Base64.encodeAsString(this.hashedKey);
	}

}
