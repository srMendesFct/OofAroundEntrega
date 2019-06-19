package pt.oofaround.resources;

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
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import pt.oofaround.support.JsonArraySupport;
import pt.oofaround.support.MediaSupport;
import pt.oofaround.util.AuthToken;
import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.LocationData;
import pt.oofaround.util.TokenData;

@Path("/location")
@Produces(MediaType.APPLICATION_JSON)
public class LocationResource {

	private static final Logger LOG = Logger.getLogger(LocationResource.class.getName());

	private final Gson g = new Gson();

	FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();
	private final Firestore db = firestore.getService();

	public LocationResource() {
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createLocation(LocationData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "createLocation")) {
			CollectionReference locations = db.collection("locations");
			Query query = locations.whereEqualTo("name", data.name);

			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				return Response.status(420).build(); // name
			}

			Map<String, Object> docData = new HashMap();
			docData.put("name", data.name);
			docData.put("description", data.description);
			docData.put("address", data.address);
			docData.put("latitude", data.latitude);
			docData.put("longitude", data.longitude);
			docData.put("category", data.category);
			docData.put("region", data.region);
			docData.put("score", data.score); // calculate score
			docData.put("nbrVisits", 0);
			docData.put("placeID", data.placeID);

			ApiFuture<WriteResult> newLocation = locations.document(data.name).set(docData);
			MediaSupport.uploadImage(data.name, data.image);
			AuthToken at = new AuthToken(data.usernameR, data.role);
			JsonObject token = new JsonObject();
			token.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(token)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/get")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLocation(LocationData data) throws InterruptedException, ExecutionException {

		LOG.fine("Getting location" + data.name);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getLocation")) {
			CollectionReference locations = db.collection("locations");
			Query query = locations.whereEqualTo("name", data.name);

			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			JsonObject res = new JsonObject();
			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("name", document.getString("name"));
				res.addProperty("description", document.getString("description"));
				res.addProperty("address", document.getString("address"));
				res.addProperty("latitude", document.getString("latitude"));
				res.addProperty("longitude", document.getString("longitude"));
				res.addProperty("category", document.getString("category"));
				res.addProperty("region", document.getString("region"));
				res.addProperty("score", document.getLong("score"));
				res.addProperty("nbrVisits", document.getLong("nbrVisits"));
				res.addProperty("placeID", document.getString("placeID"));
			}

			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/getfromcoord")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getFromCoordinates(LocationData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getFromCoordinates")) {
			CollectionReference locations = db.collection("locations");
			Query query = locations.whereEqualTo("latitude", data.latitude).whereEqualTo("longitude", data.longitude);

			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			JsonObject res = new JsonObject();
			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("name", document.getString("name"));
				res.addProperty("description", document.getString("description"));
				res.addProperty("address", document.getString("address"));
				res.addProperty("latitude", document.getString("latitude"));
				res.addProperty("longitude", document.getString("longitude"));
				res.addProperty("category", document.getString("category"));
				res.addProperty("region", document.getString("region"));
				//res.addProperty("score", document.getLong("score"));
				//res.addProperty("nbrVisits", document.getLong("nbrVisits"));
				//res.addProperty("placeID", document.getString("placeID"));
			}

			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/getcategoryregion")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLocationsByCatAndRegion(LocationData data) throws InterruptedException, ExecutionException {

		LOG.fine("Getting category" + data.name);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getLocationsByCatAndRegion")) {
			CollectionReference locations = db.collection("locations");
			Query query;
			ApiFuture<QuerySnapshot> querySnapshot;
			List<QueryDocumentSnapshot> docs;
			JsonObject res = new JsonObject();

			try {

				if (data.lastName.equalsIgnoreCase("")) {

					if (data.category.equalsIgnoreCase("") && data.region.equalsIgnoreCase("")) {

						query = locations; // .order by ranking quando ranking for implementado

					} else if (data.category.equalsIgnoreCase("")) {

						query = locations.whereEqualTo("region", data.region);

					} else if (data.region.equalsIgnoreCase("")) {

						query = locations.whereEqualTo("category", data.category);

					} else {

						query = locations.whereEqualTo("region", data.region).whereEqualTo("region", data.region);

					}

					querySnapshot = query.get();
					docs = querySnapshot.get().getDocuments();
					res.add("locations", JsonArraySupport.createLocationPropArray(docs, "name", "description",
							"address", "latitude", "longitude", "category", "region", "nbrVisits", "score"));

				} else {

					// TODO para varios
					query = locations.whereEqualTo("name", data.lastName);
					querySnapshot = query.get();
					docs = querySnapshot.get().getDocuments();
					QueryDocumentSnapshot lastDoc = docs.get(0);

					if (data.category.equalsIgnoreCase("") && data.region.equalsIgnoreCase("")) {

						query = locations.orderBy("nbrVisits"); // .order by ranking quando ranking for implementado

					} else if (data.category.equalsIgnoreCase("")) {

						query = locations.whereEqualTo("region", data.region).startAfter(lastDoc).limit(data.limit);

					} else if (data.region.equalsIgnoreCase("")) {

						query = locations.whereEqualTo("category", data.category).startAfter(lastDoc).limit(data.limit);

					} else {

						query = locations.whereEqualTo("region", data.region).whereEqualTo("region", data.region)
								.startAfter(lastDoc).limit(data.limit);

					}

					querySnapshot = query.get();
					docs = querySnapshot.get().getDocuments();
					res.add("locations", JsonArraySupport.createLocationPropArray(docs, "name", "description",
							"address", "latitude", "longitude", "category", "region", "nbrVisits", "score"));

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

	@SuppressWarnings("unused")
	@POST
	@Path("/rate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response visitLocation(LocationData data) throws InterruptedException, ExecutionException {

		// Get location info and update nbr of times visited

		CollectionReference locations = db.collection("locations");
		Query query = locations.whereEqualTo("name", data.name);

		ApiFuture<QuerySnapshot> querySnapshot = query.get();

		Long scoreGainned = null;

		Long nbrVisits = null;

		DocumentReference docRef = null;

		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			scoreGainned = document.getLong("score");
			docRef = document.getReference();
			nbrVisits = document.getLong("nbrVisits") + 1;
		}

		// Get user info and update his score

		ApiFuture<WriteResult> future = docRef.update("nbrVisits", nbrVisits);

		WriteResult result = future.get();

		CollectionReference users = db.collection("users");
		query = users.whereEqualTo("username", data.usernameR);

		querySnapshot = query.get();

		Long oldScore = null;

		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			docRef = document.getReference();
			oldScore = document.getLong("score");
		}

		oldScore += scoreGainned;

		future = docRef.update("score", oldScore);

		result = future.get();

		return Response.ok().entity(g.toJson(scoreGainned)).build();

	}

	@POST
	@Path("/getall")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllLocations(TokenData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getAllLocations")) {
			CollectionReference locations = db.collection("locations");

			ApiFuture<QuerySnapshot> querySnapshot = locations.get();
			JsonObject res = new JsonObject();
			List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

			// res.add("locations", JsonArraySupport.createThreePropArray(docs, "latitude",
			// "longitude", "category"));

			JsonArray array = new JsonArray();
			JsonObject jsObj;

			for (QueryDocumentSnapshot document1 : docs) {
				jsObj = new JsonObject();
				jsObj.addProperty("latitude", document1.get("latitude").toString());
				jsObj.addProperty("longitude", document1.get("longitude").toString());
				jsObj.addProperty("category", document1.get("category").toString());
				jsObj.addProperty("placeID", document1.getString("placeID"));
				array.add(jsObj);
			}

			res.add("locations", array);

			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/getsimple")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLocationSimple(LocationData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getLocationSimple")) {
			CollectionReference locations = db.collection("locations");

			ApiFuture<QuerySnapshot> querySnapshot = locations.whereEqualTo("latitude", data.latitude)
					.whereEqualTo("longitude", data.longitude).get();
			JsonObject res = new JsonObject();
			List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

			JsonArray array = new JsonArray();
			JsonObject jsObj;

			for (QueryDocumentSnapshot document1 : docs) {
				jsObj = new JsonObject();
				jsObj.addProperty("latitude", document1.get("latitude").toString());
				jsObj.addProperty("longitude", document1.get("longitude").toString());
				jsObj.addProperty("category", document1.get("category").toString());
				jsObj.addProperty("placeID", document1.getString("placeID"));
				array.add(jsObj);
			}

			res.add("locations", array);

			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/updateid")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePlaceID(LocationData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "updatePlaceID")) {
			CollectionReference locations = db.collection("locations");

			ApiFuture<QuerySnapshot> querySnapshot = locations.whereEqualTo("latitude", data.latitude)
					.whereEqualTo("longitude", data.longitude).get();
			JsonObject res = new JsonObject();
			List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

			DocumentReference docRef = null;

			for (QueryDocumentSnapshot document1 : docs) {
				docRef = document1.getReference();
			}

			ApiFuture<WriteResult> update = docRef.update("placeID", data.placeID);
			update.get();

			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

}
