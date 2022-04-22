package games.tx.TXMongoManager;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import games.core.database.MongoManager;
import org.bson.Document;

public class ConnectMongoDB {
    private String connectionToMongoDB = "mongodb://localhost17017";
    //-------------------------
    private String databaseName = "taixiu";
    private String collectionName = "betHistory";
    private MongoCollection collection;
    public MongoCollection getCollection (){
        return this.collection;
    }
    //-------------------------
    private long idGenerator;

    public void CreateDatabase()
    {
        MongoDatabase database = MongoManager.getInstance().getClient().getDatabase(databaseName);
        database.createCollection(collectionName);
    }

    public MongoCollection EstablishConnectionToMongoDB()
    {
        MongoDatabase database = MongoManager.getInstance().getClient().getDatabase(databaseName);
        database.createCollection(collectionName); // Tạo trước khi get collection
        this.collection = database.getCollection(collectionName);
        return this.collection;
    }

    public void AddResult (int finalresult, int dice1, int dice2, int dice3, boolean tai_xiu){
        EstablishConnectionToMongoDB();
        idGenerator = this.collection.count() + 1;
        DiceResult result = new DiceResult();
        result.setFinalResult(finalresult);
        result.setDice1(dice1);
        result.setDice2(dice2);
        result.setDice3(dice3);
        result.setTai_xiu(tai_xiu);
        result.setId(String.format("#%d", idGenerator));

        //-----------------------
        createResult(result);
    }

    private DiceResult createResult (DiceResult result) {
        if (result.getId() != null){
            this.collection.insertOne(new Document()
                    .append("final",result.getFinalResult())
                    .append("dice1",result.getDice1())
                    .append("dice2",result.getDice2())
                    .append("dice3",result.getDice3())
                    .append("tai_xiu",result.gettai_xiu())
                    .append("_id",result.getId()));
            //System.out.println(result.getFinalResult());
        }
        return result;
    }
}
