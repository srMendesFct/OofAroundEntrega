package pt.oofaround.resources;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.oofaround.support.JsonArraySupport;
import pt.oofaround.util.AuthToken;
import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.ChangePasswordData;
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
	@SuppressWarnings("unchecked")
	@POST
	@Path("/self")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserInfo(UserData data) throws InterruptedException, ExecutionException {

		LOG.fine("Listing user" + data.usernameR);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getUserInfo")) {
			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", data.usernameR);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			JsonObject res = new JsonObject();
			for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("score", document.get("score").toString());
				res.addProperty("username", document.getString("username"));
				res.addProperty("email", document.getString("email"));
				res.addProperty("country", document.getString("country"));
				res.addProperty("cellphone", document.getString("cellphone"));
				List<String> routes = (List<String>) document.get("routes");
				if (routes != null)
					res.add("routes", JsonArraySupport.createOnePropArrayFromFirestoreArray(routes, "routeName"));
				// comment
				if (document.getBoolean("privacy"))
					res.addProperty("privacy", "private");
				else
					res.addProperty("privacy", "public");
			}
			// AuthToken at = new AuthToken(data.usernameR, data.role);
			// res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/routes")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserRoutes(UserData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getUserRoutes")) {
			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", data.usernameR);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			JsonObject res = new JsonObject();
			for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
				List<String> routes = (List<String>) document.get("routes");
				if (routes != null)
					res.add("routes", JsonArraySupport.createOnePropArrayFromFirestoreArray(routes, "routeName"));
				else {
					return Response.status(Status.NO_CONTENT).build();
				}
			}
			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
	@Path("/alterself")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response alterUserInfo(UserData data) throws InterruptedException, ExecutionException {

		Map<String, Object> docData = new HashMap();

		docData.put("email", data.email);
		docData.put("country", data.country);
		docData.put("cellphone", data.cellphone);
		docData.put("privacy", data.privacy);

		ApiFuture<WriteResult> alterInfo = db.collection("users").document(data.usernameR).set(docData,
				SetOptions.merge());
		alterInfo.get();

		JsonObject res = new JsonObject();

		// AuthToken at = new AuthToken(data.usernameR, data.role);
		// res.addProperty("tokenID", at.tokenID);

		return Response.ok().entity(g.toJson(res)).build();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
	@Path("/alterpassword")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response alterPassword(ChangePasswordData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "alterPassword")) {

			String passEnc = Hashing.sha512().hashString(data.oldPassword, StandardCharsets.UTF_8).toString();

			if (db.collection("users").document(data.usernameR).get().get().getString("password").equals(passEnc)) {

				Map<String, Object> docData = new HashMap();
				passEnc = Hashing.sha512().hashString(data.password, StandardCharsets.UTF_8).toString();

				docData.put("password", passEnc);

				ApiFuture<WriteResult> alterInfo = db.collection("users").document(data.usernameR).set(docData,
						SetOptions.merge());
				alterInfo.get();

				JsonObject res = new JsonObject();

				// AuthToken at = new AuthToken(data.usernameR, data.role);
				// res.addProperty("tokenID", at.tokenID);

				return Response.ok().entity(g.toJson(res)).build();
			} else
				return Response.status(Status.FORBIDDEN).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
	@Path("/alterotherrole")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response alterOtherRole(UserData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "alterOtherRole")) {
			Map<String, Object> docData = new HashMap();

			docData.put("role", data.newRole);

			ApiFuture<WriteResult> alterInfo = db.collection("users").document(data.username).set(docData,
					SetOptions.merge());
			alterInfo.get();

			JsonObject res = new JsonObject();

			// AuthToken at = new AuthToken(data.usernameR, data.role);
			// res.addProperty("tokenID", at.tokenID);

			return Response.ok().entity(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/other")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getOtherUserInfo(UserData data) throws InterruptedException, ExecutionException {

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
			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

}
