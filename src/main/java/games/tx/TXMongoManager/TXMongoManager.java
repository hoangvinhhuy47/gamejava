package games.tx.TXMongoManager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import games.core.database.MongoManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Random;


public class TXMongoManager {
    public static TXMongoManager GetInstance;
    private String databaseName = "TAi_XIU";
    private String collection = "RESULT_OF_SESSION";
    private Document finaldocument;

    public void CreateDatabase(String databaseName) { // Đã tạo "TAi_XIU"
        MongoManager.getInstance().setDatabase(databaseName);
    }

    public void CreateCollection(String collectionname) {
        MongoDatabase db = MongoManager.getInstance().getClient().getDatabase(databaseName);
        db.createCollection(collectionname);
    }

    public Document CreateDocument() { //  1
        Document document = new Document();
        return document;
    }

    public void addDocumentInfo(Document doc, String key, Object value) { // 2 - 3 - 4 ...
        doc.append(key, value);
    }

    public void insertDocumentToCollection(Document document) { // 5
        MongoDatabase db = MongoManager.getInstance().getClient().getDatabase(databaseName);
        MongoCollection<Document> TXcollection = db.getCollection(collection);
        TXcollection.insertOne(document);
    }

    public static void main(String[] args) {
//        for (int i = 0; i < 35; i++) {
//            Random random1 = new Random();
//            int randomFinal1 = 1 + random1.nextInt(6);
//            Random random2 = new Random();
//            int randomFinal2 = 1 + random2.nextInt(6);
//            Random random3 = new Random();
//            int randomFinal3 = 1 + random3.nextInt(6);
//            int sum = randomFinal1 + randomFinal2 + randomFinal3;
//            ConnectMongoDB connectDB = new ConnectMongoDB();
//            connectDB.AddResult(sum, randomFinal1, randomFinal2, randomFinal3, true);
//        }


    }
}
