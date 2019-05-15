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
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
	public Response createRoute(RouteData data) throws InterruptedException, ExecutionException {
		
		LOG.fine("Creating route named " + data.name);
		
		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "createRoute")) {
			CollectionReference routes = db.collection("routes");
			Query query = routes.whereEqualTo("name", data.name);

			ApiFuture<QuerySnapshot> querySnapshot = query.get();

			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				return Response.status(Status.FOUND).entity("Route name already in use.").build();
			}

			JsonObject res = new JsonObject();
			Map<String, Object> docData = new HashMap();
			String[] cat = {"lixo", "maisLixo"};
			
			docData.put("name", data.name);
			docData.put("description", data.description);
			docData.put("creatorUsername", data.creatorUsername);
			docData.put("locationsNames", data.locationNames);
			docData.put("categories", cat);
			docData.put("rating", (float) 0);
			docData.put("numberRates", 0);
			docData.put("status", "ok");
			
			ApiFuture<WriteResult> newUser = routes.document(data.name).set(docData);
			AuthToken at = new AuthToken(data.usernameR, data.role);
			
			res.addProperty("tokenID", at.tokenID);
			return Response.ok(g.toJson(res)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

}
