package games.core.database.model;

import games.core.database.MongoConst;
import games.core.database.MongoManager;
import games.core.database.document.DFundHistory;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;



@CollectionClass(MongoConst.COLL_FUND_HISTORY)
public class MFundHistory extends MGModelAdapter<DFundHistory> implements MongoConst {
    public static MFundHistory instance;
    public static MFundHistory getInstance() {
        if (instance == null) instance = new MFundHistory();
        return instance;
    }
    private MFundHistory() {
        super(MongoManager.getInstance().getDatabase());
    }

    private DFundHistory logFundHistory(
            int gameId, int roomId,
            int type, long beforeValue, long value,
            String admin, boolean isDealer
            ){
        DFundHistory dFundHistory = new DFundHistory();
        dFundHistory.setId((int)getNextId());
        dFundHistory.setType(type);
        dFundHistory.setValue(value);
        dFundHistory.setBeforeValue(beforeValue);
        dFundHistory.setAdmin(admin);
        dFundHistory.setTimestamp((int)(System.currentTimeMillis() / 1000));
        dFundHistory.setGameId(gameId);
        dFundHistory.setRoomId(roomId);
        dFundHistory.setDealer(isDealer);
        dFundHistory.saveDB();
        return dFundHistory;
    }

    public DFundHistory logAddFund(int gameId, int roomId, long beforeValue, long value, String admin, boolean isDealer){
        return this.logFundHistory(gameId, roomId, DFundHistory.TYPE_IN, beforeValue, value, admin, isDealer);
    }

    public DFundHistory logAddFund(int gameId, int roomId, long beforeValue, long value, String admin){
        return this.logAddFund(gameId, roomId, beforeValue, value, admin, false);
    }


    public DFundHistory logSubFund(int gameId, int roomId, long beforeValue, long value, String admin, boolean isDealer){
        if (value > 0){
            value = -value;
        }
        return this.logFundHistory(gameId, roomId, DFundHistory.TYPE_OUT, beforeValue, value, admin, isDealer);
    }

    public DFundHistory logSubFund(int gameId, int roomId, long beforeValue, long value, String admin){
        return logSubFund(gameId, roomId, beforeValue, value, admin, false);
    }

}
