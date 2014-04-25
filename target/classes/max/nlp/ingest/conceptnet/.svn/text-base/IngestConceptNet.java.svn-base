package max.nlp.ingest.conceptnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
@SuppressWarnings("deprecation")
public class IngestConceptNet {


	//DL: http://conceptnet5.media.mit.edu/downloads/current/flat_json_20130529.tar.bz2
	private static String CONCEPTNET_DIRECTORY = "C:\\Users\\max.kaufmann\\Downloads\\flat_json_20130529.tar\\flat_json_20130529\\";

	public static void main(String[] args) {
		try {

			File[] files = new File(CONCEPTNET_DIRECTORY).listFiles();
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("conceptnet-dump");
			for (File f : files) {
				System.out.println("Processing file: " + f.toString());
				addToMongoCollectionAsOne(f, db);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}

	public static void addToMongoCollectionAsOne(File f,DB db) {

		try {
		
			BufferedReader b = new BufferedReader(new FileReader(f));
			String line = "";
			while ((line = b.readLine()) != null) {
				DBObject dbObject = (DBObject) JSON.parse(line);
				DBCollection collection = db.getCollection("conceptnet");
				collection.insert(dbObject);
			}
			b.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addToMongoCollectionAsMulti(File f,DB db) {

		try {
		
			BufferedReader b = new BufferedReader(new FileReader(f));
			String line = "";
			while ((line = b.readLine()) != null) {
				DBObject dbObject = (DBObject) JSON.parse(line);
				String collName = dbObject.get("rel").toString().replaceAll("/r/","");
				DBCollection collection = db.getCollection(collName);
				collection.insert(dbObject);
			}
			b.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}