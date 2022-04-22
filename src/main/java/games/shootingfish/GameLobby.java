package games.shootingfish;

import games.core.common.money.MoneyType;
import games.shootingfish.data.DataManager;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.exception.NullPlayerException;
import games.shootingfish.exception.RoomException;
import games.shootingfish.exception.TableException;
import games.shootingfish.manager.FishingPlayerManager;
import games.shootingfish.manager.FundManager;
import games.shootingfish.object.FishingPlayer;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class GameLobby {
    /**
     *Singleton instance
     */
    private static GameLobby instance;
    public static GameLobby getInstance() {
        if (instance == null) instance = new GameLobby();
        return instance;
    }

    public static final int GAME_ID = 1;

    /**
     * get roomType From file
     */
    private static DataManager dataManager = DataManager.getInstance();

    private static Logger logger;

    /**
     * HashMap room of game Map<RoomId, GameRoom>
     */
    private HashMap<Integer, GameRoom> listGameRoom;

    private GameLobby()
    {
        FundManager.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        listGameRoom = new HashMap<>();

        int size = dataManager.getDataRoomType().size();

        for (RoomType roomType : dataManager.getDataRoomType())
        {
            logger.info("Room type = " + roomType);
            if (roomType != null)
            {
                listGameRoom.put(roomType.getRoomId(), new GameRoom(roomType));
            }
        }
    }

    /**
     * Get GameRoom by roomId
     * @param roomId id of room
     * @return GameRoom
     * @throws RoomException room not exists
     */
    public GameRoom getRoom(int roomId) throws RoomException {
        GameRoom gameRoom = listGameRoom.get(roomId);
        if (gameRoom != null){
            return gameRoom;
        }
        return null;
        //throw new RoomException("Room[" + roomId + "] not exists!");
    }

    GameRoom getRoomByTableId(int tableId) throws RoomException {
        int roomId = tableId / 1000000;
        return getRoom(roomId);
    }

    /**
     * get table by table id
     * @param tableId id of table
     * @return GameTable
     */
    GameTable getActiveTable(int tableId) throws TableException, RoomException{
        int roomId = tableId / 1000000;
        GameRoom gameRoom = listGameRoom.get(roomId);
        if(gameRoom != null){
            GameTable gameTable = gameRoom.getActiveTable(tableId);
            if (gameTable != null){
                return gameTable;
            } else {
                throw new TableException("Bàn TableID[" + tableId + "] không tồn tại");
            }
        } else {
            throw new RoomException("Phòng RoomID["+ roomId +"]không tồn tại");
        }
    }

    void instantJoinTable(int playerId) throws TableException, NullPlayerException
    {
        FishingPlayer fishingPlayer = FishingPlayerManager.getInstance().getPlayer(playerId);
        if (fishingPlayer != null)
        {
            Collection<GameRoom> gameRoomSet = listGameRoom.values();
            long currentGold = fishingPlayer.getMoneyBag().get(MoneyType.GOLD).getAmount();
            for (GameRoom gameRoom : gameRoomSet) {
                if (gameRoom.getRoomType().getMaxGoldInstantJoinTable() >= currentGold)
                {
                    gameRoom.joinTable(fishingPlayer);
                    return;
                }
            }
            GameRoom gameRoom = getRoom(RoomType.TRIAL);
            gameRoom.joinTable(fishingPlayer);
            return;

            //throw new TableException(String.format("Player[%s] muốn chơi nhanh nhưng không tìm thấy bàn nào phù hợp!", playerId));
        } else {
            throw new NullPlayerException(String.format("Không tồn tại Player[%s] (Chưa đăng nhập hoặc bị disconnect)", playerId));
        }
    }

    /**
     * get GameTable by playerId (return playing table of player)
     * @param playerId playerId
     * @return GameTable
     * @throws RuntimeException (if can't find player by playerManager)
     */
    GameTable getTableByPlayerId(int playerId) throws TableException{
        FishingPlayer fishingPlayer = FishingPlayerManager.getInstance().getPlayer(playerId);
        if (fishingPlayer != null && fishingPlayer.isPlaying()){
            int tableId = FishingPlayerManager.getInstance().getPlayer(playerId).getPlayingInTable();
            return getActiveTable(tableId);
        } else {
            throw new TableException("FishingPlayer này đang không chơi ở bàn nào cả!");
        }

    }
    FishShooting getGame(int tableId){
        GameTable gameTable = getActiveTable(tableId);
        if (gameTable != null && gameTable.getFishShooting() != null){
            return gameTable.getFishShooting();
        }
        throw new TableException("Game này không tồn tại trong bàn " + tableId);
    }

    public HashMap<Integer, GameRoom> getListGameRoom() {
        return listGameRoom;
    }
}
