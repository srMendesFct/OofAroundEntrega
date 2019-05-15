package pt.oofaround.resources;

import java.util.HashMap;
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
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.oofaround.util.AuthToken;
import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.LocationData;

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
			docData.put("coordinates", new GeoPoint(data.latitude, data.longitude));
			docData.put("category", data.category);

			ApiFuture<WriteResult> newLocation = locations.document(data.name).set(docData);
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
				res.addProperty("address", document.getString("description"));
				res.addProperty("latitude", document.getGeoPoint("coordinates").getLatitude());
				res.addProperty("longitude", document.getGeoPoint("coordinates").getLongitude());
				res.addProperty("category", document.getString("category"));
			}
			AuthToken at = new AuthToken(data.usernameR, data.role);
			res.addProperty("tokenID", at.tokenID);
			
			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

}
