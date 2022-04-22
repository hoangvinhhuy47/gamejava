package games.core.database.model;

import games.core.exception.currency.CannotLessThanZero;
import games.core.database.MongoConst;
import games.core.database.MongoManager;
import games.core.database.document.DRoomInfo;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import static games.core.database.MongoConst.COLL_ROOM_INFO;

@CollectionClass(COLL_ROOM_INFO)
public class MRoomInfo extends MGModelAdapter<DRoomInfo> implements MongoConst{
    private static MRoomInfo instance;

    public static MRoomInfo getInstance() {
        if (instance == null){
            instance = new MRoomInfo();
        }
        return instance;
    }




    protected MRoomInfo() {
        super(MongoManager.getInstance().getDatabase());
    }

    public void plusPot(int roomId, long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        this.increaseMoney(roomId, money, "fund");
    }

    public void subPot(int roomId, long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        this.increaseMoney(roomId, -money, "pot");
    }

    public void plusFund(int roomId, long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        this.increaseMoney(roomId, money, "fund");
    }

    public void subFund(int roomId, long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        this.increaseMoney(roomId, -money, "fund");
    }

    private void increaseMoney(int roomId, long money, String field) {
        Document filter = new Document(FIELD_DEFAULT_ID, roomId);
        Document increase = new Document("$inc", new Document(field, money));
        this.mgCollection.updateOne(filter, increase);
    }


}
