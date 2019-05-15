package pt.oofaround.util;

import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;

public class AuthenticationTool {

	public AuthenticationTool() {
	}

	public static boolean authenticate(String tokenID, String username, String role, String action)
			throws InterruptedException, ExecutionException {

		AuthToken at = new AuthToken(username, role);

		if (!at.tokenID.equalsIgnoreCase(tokenID))
			return false;

		FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround")
				.build();

		Firestore db = firestore.getService();

		CollectionReference perm = db.collection("permissions");

		Query query = perm.whereEqualTo(FieldPath.documentId(), role);

		ApiFuture<QuerySnapshot> querySnapshot = query.get();

		DocumentSnapshot document = querySnapshot.get().getDocuments().get(0);

		return document.getBoolean(action);
	}

}
