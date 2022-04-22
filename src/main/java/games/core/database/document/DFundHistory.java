package games.core.database.document;

import games.core.database.MongoConst;
import games.core.database.model.MFundHistory;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;


@CollectionClass(MongoConst.COLL_FUND_HISTORY)
public class DFundHistory extends MGDocument implements MongoConst {

    public static int TYPE_IN = 1;
    public static int TYPE_OUT = 0;
    @DocumentFieldPK
    @DocumentField(FIELD_DEFAULT_ID)
    private int id;

    @DocumentField(FIELD_FUND_TYPE)
    private int type;

    @DocumentField(FIELD_FUND_BEFORE_VALUE)
    private long beforeValue;

    @DocumentField(FIELD_FUND_VALUE)
    private long value;

    @DocumentField(FIELD_FUND_AFTER_VALUE)
    private long afterValue;


    @DocumentField(FIELD_TIMESTAMP)
    private int timestamp;

    @DocumentField(FIELD_FUND_ADMIN)
    private String admin;

    @DocumentField(FIELD_ROOM_INFO_GAME_ID)
    private int gameId;

    @DocumentField(FIELD_ROOM_ID)
    private int roomId;

    @DocumentField(FIELD_FUND_IS_DEALER)
    private boolean isDealer = false;


    public DFundHistory(){

    }

    public DFundHistory(int id){
        this.setId(id);
        this.updateDB();
    }

    @Override
    public DFundHistory updateDB() throws NotFoundDocumentInDatabase {
        MFundHistory.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public DFundHistory saveDB() {
        MFundHistory.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.changed.put(FIELD_DEFAULT_ID, id);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.changed.put(FIELD_FUND_TYPE, type);
        this.type = type;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.changed.put(FIELD_FUND_VALUE, value);
        this.value = value;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.changed.put(FIELD_TIMESTAMP, timestamp);
        this.timestamp = timestamp;
    }


    public long getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(long beforeValue) {
        this.setAfterValue(beforeValue + this.value);
        this.changed.put(FIELD_FUND_BEFORE_VALUE, beforeValue);
        this.beforeValue = beforeValue;
    }

    private long getAfterValue() {
        return afterValue;
    }

    private void setAfterValue(long value){
        this.changed.put(FIELD_FUND_AFTER_VALUE, value);
        this.afterValue = value;
    }



    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.changed.put(FIELD_FUND_ADMIN, admin);
        this.admin = admin;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.changed.put(FIELD_ROOM_INFO_GAME_ID, gameId);
        this.gameId = gameId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.changed.put(FIELD_ROOM_ID, roomId);
        this.roomId = roomId;
    }

    public boolean isDealer() {
        return isDealer;
    }

    public void setDealer(boolean dealer) {
        isDealer = dealer;
        this.changed.put(FIELD_FUND_IS_DEALER, dealer);
    }
}
