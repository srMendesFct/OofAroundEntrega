package pt.oofaround.util;

public class RankingData {

	public String tokenID;
	public String usernameR;
	public String role;
	public String username;
	public int lastRequest;
	public int limit;
	public String lastUsername;

	public RankingData() {
	}

	public RankingData(String tokenID, String usernameR, String role, String username, String lastRequest, String limit,
			String lastUsername) {
		this.tokenID = tokenID;
		this.usernameR = usernameR;
		this.role = role;
		this.username = username;
		this.lastRequest = Integer.parseInt(lastRequest);
		this.limit = Integer.parseInt(limit);
		this.lastUsername = lastUsername;
	}
}
