package pt.oofaround.cronjobs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;

import pt.oofaround.util.LocationData;

@Path("/crontask")
@Produces(MediaType.APPLICATION_JSON)
public class CronTasks {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CronTasks.class.getName());

	FirestoreOptions firestore = FirestoreOptions.getDefaultInstance().toBuilder().setProjectId("oofaround").build();
	private final Firestore db = firestore.getService();

	public CronTasks() {
	}

	@GET
	@Path("/ranking")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response rank(LocationData data) throws InterruptedException, ExecutionException {

		CollectionReference flags = db.collection("flag");

		Query query = flags.whereEqualTo(com.google.cloud.firestore.FieldPath.documentId(), "ranking");

		ApiFuture<QuerySnapshot> querySnapshot = query.get();

		boolean flag = false;

		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			flag = document.getBoolean("flag");
		}
		if (flag) {

			// alterar a flag para false no fim da execucao

			List<QueryDocumentSnapshot> userDocs = db.collection("users").orderBy("score").get().get().getDocuments();

			if (!userDocs.isEmpty()) {

				List<QueryDocumentSnapshot> rankingDocs = db.collection("rankings").orderBy("score").get().get()
						.getDocuments();

				Map<String, Object> docData;

				int compValue = Integer.compare(userDocs.size(), rankingDocs.size());

				if (compValue == 0) {

					Iterator<QueryDocumentSnapshot> itUsers = userDocs.iterator();
					Iterator<QueryDocumentSnapshot> itRankings = rankingDocs.iterator();

					int i = 0;

					DocumentSnapshot user;
					DocumentSnapshot rank;

					while (itUsers.hasNext()) {
						user = itUsers.next();
						rank = itRankings.next();

						docData = new HashMap<>();
						if (user.get("username") != rank.get("username") || user.get("score") != rank.get("score")) {
							docData.put("username", user.get("username"));
							docData.put("score", user.get("score"));

							ApiFuture<WriteResult> future = db.collection("rankings").document(String.valueOf(i++))
									.set(docData, SetOptions.merge());
							future.get();
						}
					}
				} else if (compValue > 0) {

					Iterator<QueryDocumentSnapshot> itUsers = userDocs.iterator();
					Iterator<QueryDocumentSnapshot> itRankings = rankingDocs.iterator();

					int i = 0;

					DocumentSnapshot user;
					DocumentSnapshot rank;

					while (itRankings.hasNext()) {
						user = itUsers.next();
						rank = itRankings.next();

						docData = new HashMap<>();
						if (user.get("username") != rank.get("username") || user.get("score") != rank.get("score")) {
							docData.put("username", user.get("username"));
							docData.put("score", user.get("score"));

							ApiFuture<WriteResult> future = db.collection("rankings").document(String.valueOf(i++))
									.set(docData, SetOptions.merge());
							future.get();
						}
					}
					while (itUsers.hasNext()) {
						itUsers.next();
						ApiFuture<WriteResult> future = db.collection("rankings").document(String.valueOf(i++))
								.delete();
						future.get();
					}
				} else if (compValue < 0) {

					Iterator<QueryDocumentSnapshot> itUsers = userDocs.iterator();
					Iterator<QueryDocumentSnapshot> itRankings = rankingDocs.iterator();

					int i = 0;
					DocumentSnapshot user;
					DocumentSnapshot rank;
					while (itUsers.hasNext()) {
						user = itUsers.next();
						rank = itRankings.next();

						docData = new HashMap<>();
						if (user.get("username") != rank.get("username") || user.get("score") != rank.get("score")) {
							docData.put("username", user.get("username"));
							docData.put("score", user.get("score"));

							ApiFuture<WriteResult> future = db.collection("rankings").document(String.valueOf(i++))
									.set(docData, SetOptions.merge());
							future.get();
						}
					}
					while (itRankings.hasNext()) {
						itRankings.next();
						ApiFuture<WriteResult> future = db.collection("rankings").document(String.valueOf(i++))
								.delete();
						future.get();
					}
				}
			}
		}
		return Response.ok().build();
	}

}
