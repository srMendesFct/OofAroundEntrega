package pt.oofaround.resources;

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
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.UserData;


@Path("/userinfo")
@Produces(MediaType.APPLICATION_JSON)
public class UserInfoResource {

	private static final Logger LOG = Logger.getLogger(UserInfoResource.class.getName());

	private final Gson g = new Gson();

	FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();
	private final Firestore db = firestore.getService();

	public UserInfoResource() {
	}

	// information of the own user, does not require usernameR for token
	@POST
	@Path("/self")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserInfo(UserData data) throws InterruptedException, ExecutionException {

		LOG.fine("Listing user" + data.username);

		if (AuthenticationTool.authenticate(data.tokenID, data.username, data.role, "getUserInfo")) {
			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", data.username);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			JsonObject res = new JsonObject();
			for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("score", document.get("score").toString());
				res.addProperty("username", document.getString("username"));
				res.addProperty("email", document.getString("email"));
				res.addProperty("country", document.getString("country"));
				res.addProperty("cellphone", document.getString("cellphone"));
				if (document.getBoolean("privacy"))
					res.addProperty("privacy", "private");
				else
					res.addProperty("privacy", "public");
			}
			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@POST
	@Path("/other")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getOtherUserInfo(UserData data) throws InterruptedException, ExecutionException {

		LOG.fine("Listing user" + data.username);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getOtherUserInfo")) {
			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", data.username);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			JsonObject res = new JsonObject();
			for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("score", document.get("score").toString());
				res.addProperty("username", document.getString("username"));
				res.addProperty("email", document.getString("email"));
				res.addProperty("country", document.getString("country"));
				res.addProperty("cellphone", document.getString("cellphone"));
				if (document.getBoolean("privacy"))
					res.addProperty("privacy", "private");
				else
					res.addProperty("privacy", "public");
			}
			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

}
