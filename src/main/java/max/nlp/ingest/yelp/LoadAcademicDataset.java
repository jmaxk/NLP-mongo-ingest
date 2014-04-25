package max.nlp.ingest.yelp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import max.nlp.dal.yelp.Business;
import max.nlp.dal.yelp.Review;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class LoadAcademicDataset {
	static String review = "/home/jmaxk/resources/yelp/yelp_academic_dataset_review.json";
	static String business = "/home/jmaxk/resources/yelp/yelp_academic_dataset_business.json";

	public static void main(String[] args) {
		Map<String, Business> businesses = loadBusinesses();
		System.out.println(businesses.entrySet().size());
		Map<String, List<Review>> reviews = loadReview(businesses);
		for(Entry<String, List<Review>> e : reviews.entrySet()){
			System.out.println(e.getKey() + " " + e.getValue().size());
		}
	}

	public static Map<String, List<Review>> loadReview(
			Map<String, Business> businessesById) {
		Map<String, List<Review>> reviewsIndexedByState = new HashMap<String, List<Review>>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(new File(
					review)));
			String line = "";
			while ((line = b.readLine()) != null) {
				JsonParser parser = new JsonParser();
				JsonObject obj = parser.parse(line).getAsJsonObject();
				String businessID = obj.get("business_id").toString();
				String state = businessesById.get(businessID).getState();
				List<Review> reviewsForState = reviewsIndexedByState
						.getOrDefault(state, new ArrayList<Review>());
				String text = obj.get("text").toString();
				reviewsForState.add(new Review(text));
				reviewsIndexedByState.put(state, reviewsForState);

			}
			b.close();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reviewsIndexedByState;
	}

	public static Map<String, Business> loadBusinesses() {
		Map<String, Business> businessesById = new HashMap<String, Business>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(new File(
					business)));
			String line = "";
			while ((line = b.readLine()) != null) {
				JsonParser parser = new JsonParser();
				JsonObject obj = parser.parse(line).getAsJsonObject();
				String businessID = obj.get("business_id").toString();
				String state = obj.get("state").toString();
				businessesById.put(businessID, new Business(businessID, state));
				
			}
			b.close();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return businessesById;
	}
}
