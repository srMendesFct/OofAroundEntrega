
package pt.oofaround.resources;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import pt.oofaround.util.AuthToken;
import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.RankingData;
import pt.oofaround.util.TokenData;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON)
public class ListingResource {
	private static final Logger LOG = Logger.getLogger(ListingResource.class.getName());

	private final Gson g = new Gson();

	private FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround")
			.build();
	private final Firestore db = firestore.getService();

	public ListingResource() {
	}

	@POST
	@Path("/users")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllUsers(TokenData data) throws InterruptedException, ExecutionException {
		LOG.fine("Listing users");

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getAllUsers")) {
			CollectionReference users = db.collection("users");
			try {
				Query query = users;
				ApiFuture<QuerySnapshot> querySnapshot = query.get();
				JsonObject res = new JsonObject();
				int i = 0;
				for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
					res.addProperty("user" + i++, document.getString("username"));
				}

				AuthToken at = new AuthToken(data.usernameR, data.role);
				res.addProperty("username", at.username);
				res.addProperty("role", at.role);
				res.addProperty("tokenID", at.tokenID);
				return Response.ok(g.toJson(res)).build();
			} catch (Exception e) {
				return Response.status(Status.FORBIDDEN).entity(e.toString()).build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@POST
	@Path("/publicusers")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getPublicUsers(TokenData data) throws InterruptedException, ExecutionException {
		LOG.fine("Listing users");

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getPublicUsers")) {
			CollectionReference users = db.collection("users");
			try {
				Query query = users;
				ApiFuture<QuerySnapshot> querySnapshot = query.get();
				JsonObject res = new JsonObject();
				int i = 0;
				for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
					res.addProperty("user" + i++, document.getString("username"));
				}

				AuthToken at = new AuthToken(data.usernameR, data.role);
				res.addProperty("usernameR", at.username);
				res.addProperty("role", at.role);
				res.addProperty("expirationDate", at.expirationDate);
				res.addProperty("tokenID", at.tokenID);
				return Response.ok(g.toJson(res)).build();
			} catch (Exception e) {
				return Response.status(Status.FORBIDDEN).entity("Failed get").build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@POST
	@Path("/publicranking")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getPublicRankings(RankingData data)
			throws InterruptedException, ExecutionException, TimeoutException {
		LOG.fine("Listing users");

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getPublicRankings")) {
			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", data.username);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			JsonObject res = new JsonObject();
			for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("ownScore", document.get("score").toString());
			}

			try {
				Query sortedUsers;
				ApiFuture<QuerySnapshot> queryRes;
				List<QueryDocumentSnapshot> docs;
				JsonArray scores = new JsonArray();
				JsonObject jsObj;

				if (data.lastRequest == 0) {
					sortedUsers = users.orderBy("score", Direction.DESCENDING).limit(data.limit);
					queryRes = sortedUsers.get();
					docs = queryRes.get().getDocuments();
					if (docs.isEmpty())
						return Response.status(404).build();
					for (QueryDocumentSnapshot document1 : docs) {
						jsObj = new JsonObject();
						jsObj.addProperty("username", document1.getString("username"));
						jsObj.addProperty("score", document1.get("score").toString());
						scores.add(jsObj);
					}
					res.add("scores", scores);
				} else {
					sortedUsers = users.whereEqualTo("username", data.lastUsername);
					queryRes = sortedUsers.get();
					docs = queryRes.get().getDocuments();
					QueryDocumentSnapshot lastDoc = docs.get(0);
					sortedUsers = users.orderBy("score", Direction.DESCENDING).startAfter(lastDoc).limit(data.limit);
					queryRes = sortedUsers.get();
					docs = queryRes.get().getDocuments();
					if (docs.isEmpty())
						return Response.status(404).build();
					for (QueryDocumentSnapshot document1 : docs) {
						jsObj = new JsonObject();
						jsObj.addProperty("username", document1.getString("username"));
						jsObj.addProperty("score", document1.get("score").toString());
						scores.add(jsObj);
					}
					res.add("scores", scores);
				}
				AuthToken at = new AuthToken(data.usernameR, data.role);
				res.addProperty("tokenID", at.tokenID);

				return Response.ok(g.toJson(res)).build();
			} catch (Exception e) {
				String s = "";
				for (StackTraceElement ss : e.getStackTrace()) {
					s += "   " + ss.toString();
				}
				return Response.status(Status.FORBIDDEN).entity(s).build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@POST
	@Path("/rankingtest")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTestRanking(RankingData data) throws InterruptedException, ExecutionException, TimeoutException {
		LOG.fine("Listing users");

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getPublicRankings")) {
			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", data.username);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			JsonObject res = new JsonObject();
			for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("ownScore", document.get("score").toString());
			}

			try {
				Query sortedUsers;
				ApiFuture<QuerySnapshot> queryRes;
				List<QueryDocumentSnapshot> docs;

				if (data.lastRequest == 0) {
					sortedUsers = users.orderBy("score", Direction.DESCENDING).whereEqualTo("privacy", false)
							.limit(data.limit);
					queryRes = sortedUsers.get();
					docs = queryRes.get().getDocuments();
					for (QueryDocumentSnapshot document1 : docs) {
						res.addProperty(document1.getString("username"), document1.get("score").toString());
					}
				} else {
					sortedUsers = users.whereEqualTo("username", data.lastUsername);
					queryRes = sortedUsers.get();
					docs = queryRes.get().getDocuments();
					QueryDocumentSnapshot lastDoc = docs.get(0);
					sortedUsers = users.orderBy("score", Direction.DESCENDING).whereEqualTo("privacy", false)
							.startAfter(lastDoc).limit(data.limit);
					queryRes = sortedUsers.get();
					docs = queryRes.get().getDocuments();
					for (QueryDocumentSnapshot document1 : docs) {
						res.addProperty(document1.getString("username"), document1.get("score").toString());
					}
				}

				AuthToken at = new AuthToken(data.usernameR, data.role);
				res.addProperty("tokenID", at.tokenID);

				return Response.ok(g.toJson(res)).build();
			} catch (Exception e) {
				String s = "";
				for (StackTraceElement ss : e.getStackTrace()) {
					s += "   " + ss.toString();
				}
				return Response.status(Status.FORBIDDEN).entity(s).build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}
}
