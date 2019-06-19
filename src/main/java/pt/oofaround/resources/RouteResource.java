package pt.oofaround.resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import pt.oofaround.support.JsonArraySupport;
import pt.oofaround.util.AuthToken;
import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.RouteData;

@Path("/route")
@Produces(MediaType.APPLICATION_JSON)
public class RouteResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());

	private final Gson g = new Gson();

	FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();
	private final Firestore db = firestore.getService();

	public RouteResource() {
	}

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createRouteFromArray(RouteData data) throws InterruptedException, ExecutionException {

		LOG.fine("Creating route named " + data.name);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "createRouteFromArray")) {

			ApiFuture<QuerySnapshot> querySnapshot = db.collection("routes").whereEqualTo("name", data.name)
					.whereEqualTo("creatorUsername", data.usernameR).get();

			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				return Response.status(Status.FOUND).entity("Route name already in use.").build();
			}

			JsonObject res = new JsonObject();
			Map<String, Object> docData = new HashMap();
			Set<String> cats = new HashSet<String>();
			List<GeoPoint> locationsList = new LinkedList<GeoPoint>();
			List<String> names = new LinkedList<String>();
			List<String> placeIDs = new LinkedList<String>();
			Map<String, Integer> catMap = new HashMap<String, Integer>();

			for (int i = 0; i < data.locationNames.length; i++) {
				if (data.locationNames[i].category != "undefined")
					cats.add(data.locationNames[i].category);
				names.add(data.locationNames[i].name);
				placeIDs.add(data.locationNames[i].placeId);
				locationsList.add(new GeoPoint(data.locationNames[i].latitude, data.locationNames[i].longitude));
				catMap.putIfAbsent(data.locationNames[i].category, 1);
			}

			docData.put("name", data.name);
			docData.put("description", data.description);
			docData.put("creatorUsername", data.creatorUsername);
			docData.put("locationsCoords", locationsList);
			docData.put("locationsNames", names);
			docData.put("placeIDs", placeIDs);

			docData.put("categories", catMap);
			docData.put("rating", (double) 0);
			docData.put("numberRates", 0);
			docData.put("status", "ok");
			docData.put("1", 0);
			docData.put("2", 0);
			docData.put("3", 0);
			docData.put("4", 0);
			docData.put("5", 0);

			ApiFuture<WriteResult> newUser = db.collection("routes").document().set(docData);
			AuthToken at = new AuthToken(data.usernameR, data.role);

			res.addProperty("tokenID", at.tokenID);
			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteRoute(RouteData data) throws InterruptedException, ExecutionException {
		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "deleteRoute")) {
			try {
				ApiFuture<QuerySnapshot> querySnapshot = db.collection("routes").whereEqualTo("name", data.name)
						.whereEqualTo("creatorUsername", data.usernameR).get();

				for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
					document.getReference().delete().get();
				}

				return Response.ok().build();
			} catch (Exception e) {
				return Response.status(Status.NOT_FOUND).entity("Route doesn't exist.").build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/get")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getRoute(RouteData data) throws InterruptedException, ExecutionException {

		LOG.fine("Getting location" + data.name);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getRoute")) {
			// CollectionReference routes = db.collection("routes");

			List<GeoPoint> locationsCoords;
			List<String> locationsNames;
			List<String> placeIDs;

			ApiFuture<QuerySnapshot> querySnapshot = db.collection("routes").whereEqualTo("name", data.name)
					.whereEqualTo("creatorUsername", data.creatorUsername).get();
			JsonObject res = new JsonObject();
			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				res.addProperty("name", document.getString("name"));
				res.addProperty("description", document.getString("description"));
				res.addProperty("creatorUsername", document.getString("creatorUsername"));

				locationsNames = (List<String>) document.get("locationsNames");
				locationsCoords = (List<GeoPoint>) document.get("locationsCoords");
				placeIDs = (List<String>) document.get("placeIDs");

				JsonArray array = new JsonArray();
				JsonObject jsObj;

				for (int i = 0; i < locationsNames.size(); i++) {
					jsObj = new JsonObject();
					jsObj.addProperty("name", locationsNames.get(i));
					jsObj.addProperty("latitude", locationsCoords.get(i).getLatitude());
					jsObj.addProperty("longitude", locationsCoords.get(i).getLongitude());
					jsObj.addProperty("placeIDs", placeIDs.get(i));
					array.add(jsObj);
				}

				res.add("locations", array);
				res.add("categories", JsonArraySupport
						.createOnePropArrayFromFirestoreArray((List<String>) document.get("categories"), "categories"));
				res.addProperty("rating", document.getDouble("rating"));
				res.addProperty("status", document.getString("status"));
			}
			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);

			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/listall")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listAllRoutes(RouteData data) throws InterruptedException, ExecutionException {
		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "listAllRoutes")) {
			try {
				ApiFuture<QuerySnapshot> querySnapshot;

				if (data.categories == null)
					querySnapshot = db.collection("routes").get();
				else if (data.categories.length == 1)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1).get();
				else if (data.categories.length == 2)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1).get();
				else if (data.categories.length == 3)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1)
							.whereEqualTo("categories." + data.categories[2], 1).get();
				else if (data.categories.length == 4)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1)
							.whereEqualTo("categories." + data.categories[2], 1)
							.whereEqualTo("categories." + data.categories[3], 1).get();
				else if (data.categories.length == 5)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1)
							.whereEqualTo("categories." + data.categories[2], 1)
							.whereEqualTo("categories." + data.categories[3], 1)
							.whereEqualTo("categories." + data.categories[4], 1).get();
				else if (data.categories.length == 6)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1)
							.whereEqualTo("categories." + data.categories[2], 1)
							.whereEqualTo("categories." + data.categories[3], 1)
							.whereEqualTo("categories." + data.categories[4], 1)
							.whereEqualTo("categories." + data.categories[5], 1).get();
				else if (data.categories.length == 7)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1)
							.whereEqualTo("categories." + data.categories[2], 1)
							.whereEqualTo("categories." + data.categories[3], 1)
							.whereEqualTo("categories." + data.categories[4], 1)
							.whereEqualTo("categories." + data.categories[5], 1)
							.whereEqualTo("categories." + data.categories[6], 1).get();
				else if (data.categories.length == 8)
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1)
							.whereEqualTo("categories." + data.categories[2], 1)
							.whereEqualTo("categories." + data.categories[3], 1)
							.whereEqualTo("categories." + data.categories[4], 1)
							.whereEqualTo("categories." + data.categories[5], 1)
							.whereEqualTo("categories." + data.categories[6], 1)
							.whereEqualTo("categories." + data.categories[7], 1).get();
				else
					querySnapshot = db.collection("routes").whereEqualTo("categories." + data.categories[0], 1)
							.whereEqualTo("categories." + data.categories[1], 1)
							.whereEqualTo("categories." + data.categories[2], 1)
							.whereEqualTo("categories." + data.categories[3], 1)
							.whereEqualTo("categories." + data.categories[4], 1)
							.whereEqualTo("categories." + data.categories[5], 1)
							.whereEqualTo("categories." + data.categories[6], 1)
							.whereEqualTo("categories." + data.categories[7], 1)
							.whereEqualTo("categories." + data.categories[8], 1).get();

				List<String> nameList = new LinkedList<String>();
				for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
					nameList.add(document.getString("name"));
				}

				return Response.ok().entity(g.toJson(nameList)).build();
			} catch (Exception e) {
				String s = "";
				for (StackTraceElement ss : e.getStackTrace()) {
					s += "   " + ss.toString();
				}
				return Response.status(Status.FORBIDDEN).entity(s).build();
				// return Response.status(Status.NOT_FOUND).entity("Route doesn't
				// exist.").build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	@POST
	@Path("/rate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response ratePlace(RouteData data) throws NumberFormatException, InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getLocationsByCatAndRegion")) {
			CollectionReference locations = db.collection("locations");

			ApiFuture<QuerySnapshot> querySnapshot = locations.whereEqualTo("name", data.name).get();
			double rate = 0;
			long nbrRates = 0;
			long newRate = 0;
			DocumentReference docRef = null;

			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				docRef = document.getReference();
				nbrRates = document.getLong("nbrRates") + 1;
				newRate = document.getLong(data.rating) + 1;
				rate = (document.getLong("1") * 1 + document.getLong("2") * 2 + document.getLong("3") * 3
						+ document.getLong("4") * 4 + document.getLong("5") * 5 + Integer.valueOf(data.rating))
						/ nbrRates;
			}

			Map<String, Object> docData = new HashMap();

			docData.put("nbrRates", nbrRates);
			docData.put(data.rating, newRate);

			ApiFuture<WriteResult> future = docRef.update(docData);

			WriteResult result = future.get();

			JsonObject res = new JsonObject();
			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);
			res.addProperty("rating", newRate);

			return Response.ok().entity(g.toJson(res)).build();
		} else {
			return Response.status(Status.FORBIDDEN).build();
		}
	}

}
