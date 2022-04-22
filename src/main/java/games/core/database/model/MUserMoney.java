package games.core.database.model;

import games.core.database.MongoConst;
import games.core.database.MongoManager;
import games.core.database.document.UserMoney;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@CollectionClass(MongoConst.COLL_USER_MONEY)
public class MUserMoney extends MGModelAdapter<UserMoney> implements MongoConst
{
    private MUserMoney() {
        super(MongoManager.getInstance().getDatabase());
    }
    public static MUserMoney instance;

    public static MUserMoney getInstance()
    {
        if (instance == null) instance = new MUserMoney();
        return instance;
    }

    public List<UserMoney> getRankByKey(int limit, String key) throws IllegalAccessException
    {
        List<Document> listTop = this.mgCollection.find().sort(new Document(key, -1)).limit(limit).into(new ArrayList<>());
        List<UserMoney> listUserMoney = new ArrayList<>();
        for (Document document : listTop) {
            UserMoney userMoney = new UserMoney();
            this.createByDocument(userMoney, document);
            listUserMoney.add(userMoney);
        }
        return listUserMoney;
    }
}
