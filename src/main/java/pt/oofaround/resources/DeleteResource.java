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
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.oofaround.util.AuthToken;
import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.DeleteData;

@SuppressWarnings("unused")
@Path("/delete")
@Produces(MediaType.APPLICATION_JSON)
public class DeleteResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();
	private final Firestore db = firestore.getService();

	public DeleteResource() {

	}

	@POST
	@Path("/self")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doSudoku(DeleteData data) throws InterruptedException, ExecutionException {

		LOG.fine("Delete attempted by " + data.username);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "doSudoku")) {
			try {
				ApiFuture<WriteResult> orderSixtySix = db.collection("users").document(data.username).delete();
				orderSixtySix.get();
				return Response.ok().build();
			} catch (Exception e) {
				return Response.status(Status.NOT_FOUND).entity("User doesn't exist.").build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doDeleteOther(DeleteData data) throws InterruptedException, ExecutionException {

		LOG.fine("Delete attempted by " + data.username);

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "doDeleteOther")) {
			try {
				ApiFuture<WriteResult> orderSixtySix = db.collection("users").document(data.username).delete();
				AuthToken at = new AuthToken(data.usernameR, data.role);
				JsonObject token = new JsonObject();
				token.addProperty("username", at.username);
				token.addProperty("role", at.role);
				token.addProperty("tokenID", at.tokenID);
				return Response.ok(g.toJson(token)).build();
			} catch (Exception e) {
				return Response.status(Status.NOT_FOUND).entity("User doesn't exist.").build();
			}
		} else
			return Response.status(Status.FORBIDDEN).entity("Invalid permissions.").build();
	}
}
