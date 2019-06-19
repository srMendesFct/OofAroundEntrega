package pt.oofaround.support;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class MediaSupport {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MediaSupport.class.getName());

	private static final String BUCKET = "oofaround.appspot.com";

	public MediaSupport() {
	}

	@SuppressWarnings("unused")
	public static void uploadImage(String name, byte[] image) {
		StorageOptions storage = StorageOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();

		Storage db = storage.getService();

		BlobId blobId = BlobId.of(BUCKET, name);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();

		Blob blob = db.create(blobInfo, image);
	}

	public static int getNumberPhotos(String username) throws InterruptedException, ExecutionException {
		FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround")
				.build();
		Firestore db = firestore.getService();

		CollectionReference users = db.collection("users");
		Query query = users.whereEqualTo("username", username);

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		QueryDocumentSnapshot document = querySnapshot.get().getDocuments().get(0);

		return (Integer) document.get("numberPhotos");
	}
}
