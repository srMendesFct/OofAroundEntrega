package pt.oofaround.support;

import java.util.List;

import javax.ws.rs.NotFoundException;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonArraySupport {

	public JsonArraySupport() {
	}

	public static JsonArray createOnePropArray(List<QueryDocumentSnapshot> docs, String property) {

		JsonArray array = new JsonArray();
		JsonObject jsObj;

		if (docs.isEmpty())
			throw new NotFoundException();
		for (QueryDocumentSnapshot document1 : docs) {
			jsObj = new JsonObject();
			jsObj.addProperty(property, document1.get(property).toString());
			array.add(jsObj);
		}
		return array;
	}

	public static JsonArray createOnePropArrayFromFirestoreArray(List<String> docs, String property) {

		JsonArray array = new JsonArray();
		JsonObject jsObj;
		
		for (String document1 : docs) {
			jsObj = new JsonObject();
			jsObj.addProperty(property, document1);
			array.add(jsObj);
		}
		return array;
	}
	
	public static JsonArray createLocationPropArray(List<QueryDocumentSnapshot> docs, String property1,
			String property2, String property3, String property4, String property5, String property6, String property7,
			String property8, String property9) {

		JsonArray array = new JsonArray();
		JsonObject jsObj;

		if (docs.isEmpty())
			throw new NotFoundException();
		for (QueryDocumentSnapshot document1 : docs) {
			jsObj = new JsonObject();
			jsObj.addProperty(property1, document1.get(property1).toString());
			jsObj.addProperty(property2, document1.get(property2).toString());
			jsObj.addProperty(property3, document1.get(property3).toString());
			jsObj.addProperty(property4, document1.get(property4).toString());
			jsObj.addProperty(property5, document1.get(property5).toString());
			jsObj.addProperty(property6, document1.get(property6).toString());
			jsObj.addProperty(property7, document1.get(property7).toString());
			//jsObj.addProperty(property8, document1.get(property8).toString());
			//jsObj.addProperty(property9, document1.get(property9).toString());

			array.add(jsObj);
		}
		return array;
	}

	public static JsonArray createTwoPropArray(List<QueryDocumentSnapshot> docs, String property1, String property2) {

		JsonArray array = new JsonArray();
		JsonObject jsObj;

		if (docs.isEmpty())
			throw new NotFoundException();
		for (QueryDocumentSnapshot document1 : docs) {
			jsObj = new JsonObject();
			jsObj.addProperty(property1, document1.get(property1).toString());
			jsObj.addProperty(property2, document1.get(property2).toString());
			array.add(jsObj);
		}
		return array;
	}

	public static JsonArray createThreePropArray(List<QueryDocumentSnapshot> docs, String property1, String property2,
			String property3) {

		JsonArray array = new JsonArray();
		JsonObject jsObj;

		if (docs.isEmpty())
			throw new NotFoundException();
		for (QueryDocumentSnapshot document1 : docs) {
			jsObj = new JsonObject();
			jsObj.addProperty(property1, document1.get(property1).toString());
			jsObj.addProperty(property2, document1.get(property2).toString());
			jsObj.addProperty(property3, document1.get(property3).toString());
			array.add(jsObj);
		}
		return array;
	}

}
