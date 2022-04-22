package games.minigame.database.model;

import games.core.database.MongoManager;
import games.minigame.database.MongoMiniGameConst;
import games.minigame.database.document.WheelUserHistory;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 8/17/17.
 */
@CollectionClass(MongoMiniGameConst.COLL_WHEEL_HISTORY)
public class MWheelUserHistory extends MGModelAdapter<WheelUserHistory> implements MongoMiniGameConst{
    public static MWheelUserHistory instance;
    long sessionIdCur = 0;
    public static MWheelUserHistory getInstance() {
        if (instance == null) instance = new MWheelUserHistory();
        return instance;
    }

    protected MWheelUserHistory() {
        super(MongoManager.getInstance().getDatabase());
        sessionIdCur = this.getCountCollection();
    }

    public synchronized void saveWheelUserHistory(WheelUserHistory wheelUserHistory) throws NoSuchFieldException, IllegalAccessException {
        long sessionID = ++sessionIdCur;
        wheelUserHistory.setSessionId(sessionID);
        this.saveToMongo(wheelUserHistory);
    }

    public List<WheelUserHistory> getListWheelHistoryByUserId(int userID, int limit) {
        List<Document> listDoc = this.mgCollection.find(new Document(FIELD_WH_USER_ID, userID))
                .sort(new Document(FIELD_WH_TIME_STAMP, 1)).limit(limit).into(new ArrayList<>());
        List<WheelUserHistory> listWheelHistory = new ArrayList<>();
        for (Document document : listDoc) {
            WheelUserHistory wheelUserHistory = new WheelUserHistory();
            try {
                this.createByDocument(wheelUserHistory, document);
                listWheelHistory.add(wheelUserHistory);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return listWheelHistory;
    }
}
