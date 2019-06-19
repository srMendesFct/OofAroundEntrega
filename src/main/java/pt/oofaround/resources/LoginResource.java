package pt.oofaround.resources;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.oofaround.util.AuthToken;
import pt.oofaround.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();
	private final Firestore db = firestore.getService();

	public LoginResource() {

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) throws InterruptedException, ExecutionException {

		CollectionReference users = db.collection("users");
		Query query = users.whereEqualTo("username", data.username);

		ApiFuture<QuerySnapshot> querySnapshot = query.get();

		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			String passEnc = Hashing.sha512().hashString(data.password, StandardCharsets.UTF_8).toString();
			if (passEnc.equals(document.get("password"))) {
				AuthToken at = new AuthToken(document.get("username").toString(), document.get("role").toString());
				JsonObject token = new JsonObject();
				token.addProperty("username", at.username);
				token.addProperty("role", at.role);
				token.addProperty("tokenID", at.tokenID);
				return Response.ok(g.toJson(token)).build();
			}
		}

		query = users.whereEqualTo("email", data.username);

		querySnapshot = query.get();

		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			String passEnc = Hashing.sha512().hashString(data.password, StandardCharsets.UTF_8).toString();
			if (passEnc.equals(document.get("password"))) {
				AuthToken at = new AuthToken(document.get("username").toString(), document.get("role").toString());
				JsonObject token = new JsonObject();
				token.addProperty("username", at.username);
				token.addProperty("role", at.role);
				token.addProperty("tokenID", at.tokenID);
				return Response.ok(g.toJson(token)).build();
			}
		}

		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();

	}

}
