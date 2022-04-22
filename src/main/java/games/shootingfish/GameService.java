package games.shootingfish;

import games.shootingfish.datatype.RoomType;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class GameService {
    private static GameService instance;
    private Logger log;
    private GameService(){
        log = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    public static GameService getInstance() {
        if (instance == null) instance = new GameService();
        return instance;
    }

    public float getRateKillFishByRoomType(Integer roomTypeId) {
        RoomType roomType = getRoomTypeByTypeId(roomTypeId);
        return roomType.getRateKillFish();
    }

    public void setRateKillFishByRoomType(Integer roomTypeId, float rateKillFish) {
        RoomType roomType = getRoomTypeByTypeId(roomTypeId);
        roomType.setRateKillFish(rateKillFish);
    }

    public RoomType getRoomTypeByTypeId(Integer roomTypeId) {
        GameRoom gameRoom = getRoomByTypeId(roomTypeId);
        assert gameRoom != null;
        return gameRoom.getRoomType();
    }

    public GameRoom getRoomByTypeId(Integer roomTypeId) {
        return GameLobby.getInstance().getRoom(roomTypeId);
    }

    public int getPlayerInRoom(Integer roomTypeId){
        GameRoom gameRoom = getRoomByTypeId(roomTypeId);
        assert gameRoom != null;
        return gameRoom.getPlayerInRoom();
    }


}
