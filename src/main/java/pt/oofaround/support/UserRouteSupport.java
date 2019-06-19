package pt.oofaround.support;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

public class UserRouteSupport {

	public static int addRouteToProfile(String name, String routeName, List<String> existingRoutes)
			throws InterruptedException, ExecutionException {

		if (!existingRoutes.contains(routeName)) {
			existingRoutes.add(routeName);
			FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround")
					.build();
			Firestore db = firestore.getService();

			CollectionReference users = db.collection("users");
			Query query = users.whereEqualTo("username", name);
			ApiFuture<QuerySnapshot> querySnapshot = query.get();
			DocumentReference docRef;

			for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
				docRef = document.getReference();
				ApiFuture<WriteResult> future = docRef.update("routes", existingRoutes);
				future.get();
			}
			return 1;
		} else {
			return 0;
		}
	}
	
}
