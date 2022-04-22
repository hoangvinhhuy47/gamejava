package games.minigame.database.document;

import games.minigame.database.MongoMiniGameConst;
import games.minigame.database.model.MWheelUserHistory;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;

/**
 * Created by tuanhoang on 8/17/17.
 */
@CollectionClass(MongoMiniGameConst.COLL_WHEEL_HISTORY)
public class WheelUserHistory extends MGDocument implements MongoMiniGameConst {
    @DocumentField(FIELD_WH_SESSION_ID)
    @DocumentFieldPK
    long sessionId = 0;

    @DocumentField(FIELD_WH_USER_ID)
    int userId = 0;

    @DocumentField(FIELD_WH_TIME_STAMP)
    int timestamp = 0;

    @DocumentField(FIELD_WH_ID_RS_WHEEL_1)
    int idResultWheel1 = 0;

    @Override
    public MGDocument updateDB() throws IllegalAccessException {
        MWheelUserHistory.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        MWheelUserHistory.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    //region getter - setter

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
        changed.put(FIELD_WH_SESSION_ID, sessionId);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        changed.put(FIELD_WH_USER_ID, sessionId);
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
        changed.put(FIELD_WH_TIME_STAMP, sessionId);
    }

    public int getIdResultWheel1() {
        return idResultWheel1;
    }

    public void setIdResultWheel1(int idResultWheel1) {
        this.idResultWheel1 = idResultWheel1;
        changed.put(FIELD_WH_ID_RS_WHEEL_1, sessionId);
    }

    //endregion
}
