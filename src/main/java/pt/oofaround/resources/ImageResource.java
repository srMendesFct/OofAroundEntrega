package pt.oofaround.resources;

import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import com.google.api.gax.paging.Page;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobGetOption;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.oofaround.support.JsonArraySupport;
import pt.oofaround.support.MediaSupport;
import pt.oofaround.util.AuthToken;
import pt.oofaround.util.AuthenticationTool;
import pt.oofaround.util.UploadImageData;

@Path("/images")
@Produces(MediaType.APPLICATION_JSON)
public class ImageResource {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ImageResource.class.getName());

	private static final String BUCKET = "oofaround.appspot.com";

	private final Gson g = new Gson();

	public ImageResource() {
	}

	@POST
	@Path("/uploadprofile")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response uploadImage(UploadImageData data) throws InterruptedException, ExecutionException {
		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "uploadImage")) {

			MediaSupport.uploadImage(data.name + "_profile", data.image);

			AuthToken at = new AuthToken(data.usernameR, data.role);
			JsonObject token = new JsonObject();
			token.addProperty("username", at.username);
			token.addProperty("role", at.role);
			token.addProperty("tokenID", at.tokenID);
			return Response.ok(g.toJson(token)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/uploadfoto")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response uploadToProfileFolder(UploadImageData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "uploadToProfileFolder")) {
			FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround")
					.build();
			Firestore db = firestore.getService();

			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", data.name);

			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			long nbrPics = 0;
			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				nbrPics = (long) document.get("numberPhotos");
				ApiFuture<WriteResult> future = document.getReference().update("numberPhotos", nbrPics + 1);
				future.get();
			}
			MediaSupport.uploadImage(data.name + "/" + nbrPics, data.image);

			AuthToken at = new AuthToken(data.usernameR, data.role);
			JsonObject token = new JsonObject();
			token.addProperty("username", at.username);
			token.addProperty("role", at.role);
			token.addProperty("tokenID", at.tokenID);
			return Response.ok(g.toJson(token)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/getList")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getList(UploadImageData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getList")) {
			StorageOptions storage = StorageOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();

			Storage db = storage.getService();

			Page<Blob> list = db.list(BUCKET, BlobListOption.prefix(data.name + "/"));
			Iterator<Blob> it = list.iterateAll().iterator();
			List<String> blobs = new LinkedList<String>();
			// it.next();
			while (it.hasNext())
				blobs.add(it.next().getName());

			// Blob blob = db.get(BlobId.of(BUCKET, data.username + "/" + "0"));
			AuthToken at = new AuthToken(data.usernameR, data.role);
			JsonObject token = new JsonObject();
			token.add("names", JsonArraySupport.createOnePropArrayFromFirestoreArray(blobs, "name"));
			token.addProperty("username", at.username);
			token.addProperty("role", at.role);
			token.addProperty("tokenID", at.tokenID);
			return Response.ok(g.toJson(token)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/get")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getImage(UploadImageData data) throws InterruptedException, ExecutionException {

		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "getImage")) {

			StorageOptions storage = StorageOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();

			Storage db = storage.getService();

			try {
			BlobId blobId = BlobId.of(BUCKET, data.name);

			Blob blob = db.get(blobId, BlobGetOption.fields(Storage.BlobField.MEDIA_LINK));

			// Blob blob = db.get(BlobId.of(BUCKET, data.username + "/" + "0"));

			String s = Base64.getEncoder().encodeToString(blob.getContent());

			AuthToken at = new AuthToken(data.usernameR, data.role);
			JsonObject token = new JsonObject();
			token.addProperty("image", s);
			token.addProperty("username", at.username);
			token.addProperty("role", at.role);
			token.addProperty("tokenID", at.tokenID);
			return Response.ok(g.toJson(token)).build();
			
			}catch(Exception e) {
				BlobId blobId = BlobId.of(BUCKET, "profile_generic.png");

				Blob blob = db.get(blobId, BlobGetOption.fields(Storage.BlobField.MEDIA_LINK));

				String s = Base64.getEncoder().encodeToString(blob.getContent());
				
				AuthToken at = new AuthToken(data.usernameR, data.role);
				JsonObject token = new JsonObject();
				token.addProperty("image", s);
				token.addProperty("username", at.username);
				token.addProperty("role", at.role);
				token.addProperty("tokenID", at.tokenID);
				return Response.ok().entity(g.toJson(token)).build();
			}
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/deleteimage")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteImage(UploadImageData data) throws InterruptedException, ExecutionException {
		if (AuthenticationTool.authenticate(data.tokenID, data.usernameR, data.role, "deleteImage")) {

			StorageOptions storage = StorageOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();

			Storage db = storage.getService();
			
			BlobId blobId = BlobId.of(BUCKET, data.name);
			db.delete(blobId);

			AuthToken at = new AuthToken(data.usernameR, data.role);
			JsonObject token = new JsonObject();
			token.addProperty("username", at.username);
			token.addProperty("role", at.role);
			token.addProperty("tokenID", at.tokenID);
			return Response.ok(g.toJson(token)).build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

}