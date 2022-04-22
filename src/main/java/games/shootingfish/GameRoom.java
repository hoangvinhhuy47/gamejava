package games.shootingfish;

import games.core.common.money.Money;
import games.core.database.MongoConst;
import games.core.database.document.DRoomInfo;
import games.core.user.UserService;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.manager.FundManager;
import games.shootingfish.manager.TableManager;
import games.shootingfish.object.Bot;
import games.shootingfish.object.FishingPlayer;
import games.tx.room.RoomFund;
import libs.util.CRandom;
import libs.util.LogFactory;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by WINDNCC on 7/10/2017.
 */
public class GameRoom {
    /**
     * Type of room
     */
    private RoomType roomType;
    private DRoomInfo roomInfo;
    private RoomFund roomFund;
    /**
     * logger
     */

    /*
    * List bot of Room
    * */
    CopyOnWriteArrayList<Bot> listBots = new CopyOnWriteArrayList<>();

    CopyOnWriteArrayList<Bot> listBotInRooms = new CopyOnWriteArrayList<>();

    private final int MIN_COUNT_BOT_IN_ROOM = 10;
    private final int MAX_COUNT_BOT_IN_ROOM = 15;

    //rand id của user bây giờ là 9 999 999
    private final int MIN_BOT_ID = 10000000;//
    private final int MAX_BOT_ID = 90000000;//

    private final int MIN_MULTIPLY_MONEY_RAND = 850000;//500;
    private final int MAX_MULTIPLY_MONEY_RAND = 5000000;//2000;

    private static Logger logger = LogFactory.getLogger(GameRoom.class.getSimpleName());

    private TableManager tableManager;
    GameRoom(RoomType roomType)
    {
        this.roomType = roomType;
        randomeBot();
        tableManager = new TableManager(roomType);
        defaultBotJoinRoom();
        roomInfo = new DRoomInfo(roomType.getRoomId());
        roomFund = new RoomFund(roomInfo);
    }

    /*
    * randome bot
    * */
    private void randomeBot() {
        try {
            listBots = new CopyOnWriteArrayList<>();
            listBotInRooms = new CopyOnWriteArrayList<>();

            CRandom rand = new CRandom();
            int countBot = rand.randInt(MIN_COUNT_BOT_IN_ROOM, MAX_COUNT_BOT_IN_ROOM);
            logger.info("Random bot in " + roomType.getRoomName() + " - count = " + countBot);
            for (int i = 0; i < countBot; i++) {
                boolean addBotSuccess = randomAddNewBot();
                if (!addBotSuccess) {
                    i--;
                }
            }
        } catch (Exception e) {
            logger.error("randomeBot error", e);
        }

    }

    private boolean randomAddNewBot() {
        CRandom rand = new CRandom();
        int botId = rand.randInt(MIN_BOT_ID, MAX_BOT_ID);
        for (Bot bot : listBots) {
            if (bot.getUser().getId() == botId)
                return false;
        }
        int multiplyMoney = rand.randInt(MIN_MULTIPLY_MONEY_RAND, MAX_MULTIPLY_MONEY_RAND);

        int baseGun = roomType.getBaseGunMoney();
        if (roomType.getRoomId() == RoomType.TRIAL) {
            baseGun = 0;
        }
        baseGun = 1;//cung 1 muc tien het roi
        int money = multiplyMoney * baseGun;

        Bot bot = new Bot(botId, money);

        listBots.add(bot);
        logger.info("Rand bot - " + botId + " - money = " + money);
        return true;
    }

    /*
    * Default 2 bot join first room
    * */
    private void defaultBotJoinRoom() {
        try {
            int countJoinRoom = new CRandom().randInt(1, GameTable.MAX_PLAYER_WITH_BOT);

            for (int i = 0; i < countJoinRoom; i++) {
                Bot bot = listBots.get(0);
                joinTable(bot);
            }
        } catch (Exception e) {
            logger.error("defaultBotJoinRoom error", e);
        }

    }

    public void randomBotJoinTable() {
        try {
            CRandom cRandom = new CRandom();

            //can 100 gold de choi
            Money moneyNeed = new Money(100, roomType.getMoneyType());
            for (Bot bot : listBots) {
                if (!bot.getMoneyBag().isEnoughMoney(moneyNeed)) {
                    listBots.remove(bot);
                    UserService.getInstance().removeUser(bot.getId());
                } else {
                    if (cRandom.randByPercent(1)) {
                        listBots.remove(bot);
                        UserService.getInstance().removeUser(bot.getId());
                    }
                }
            }
            Bot botJoin = null;
            if (listBots.size() > 0) {
                int botIndex = cRandom.randInt(0, listBots.size() - 1);
                botJoin = listBots.get(botIndex);

            } else {
                int countBot = cRandom.randInt(1, MIN_COUNT_BOT_IN_ROOM);
                for (int i = 0; i < countBot; i++) {
                    randomAddNewBot();
                }
                if (listBots.size() > 0) {
                    botJoin = listBots.get(0);
                }
            }
            if (botJoin != null) {

                joinTable(botJoin);
            }
        } catch (Exception e)
        {
            logger.error("randomBotJoinTable error", e);
        }
    }

    /**
     *
     * @param fishingPlayer FishingPlayer : instance of fishingPlayer wanna join to this room
     */
    void joinTable(FishingPlayer fishingPlayer){
        try
        {
            GameTable gameTable = tableManager.getTableCanJoin();
             if (gameTable != null && fishingPlayer != null){ // && !fishingPlayer.isPlaying()

                //Hàm join thằng player vào đây, mọi thay đổi trạng thái của player và table thực hiện trong này
//                System.out.println("Join in game room : " + gameTable.getId() + " - playerId: " + fishingPlayer.getId());
                boolean joinSucess = gameTable.playerJoin(fishingPlayer);

                if (joinSucess)
                {
                    //Nếu bàn này chưa có trong listActive thì thêm vào listActive
                    if (!tableManager.isInListActive(gameTable) && !gameTable.isFree())
                        tableManager.addActive(gameTable);

                    //Nếu bàn không full hoặc không free thì thêm vào available
                    if (!gameTable.isFull() && !gameTable.isFree())
                        //Không thêm được thì log lỗi
                        if (!tableManager.addAvailable(gameTable))
                            logger.error("[joinTable] - Can't add available GameTable["+gameTable.getId()+"]");

                    //Nếu bàn full thì xóa khỏi available
                    if (gameTable.isFull() && tableManager.isInAvailable(gameTable))
                        //Không xóa được thì log lỗi
                        if (!tableManager.removeAvailable(gameTable))
                            logger.error("[joinTable] - Can't remove available gameTable[" + gameTable.getId() + "]");

                    //Nếu bàn free thì xóa khỏi available và thêm vào FreeTable
                    // (Thực ra không cần vì khi join vào thì sẽ không bao h free nhưng để cho chắc)
                    if (gameTable.isFree() && !tableManager.isInFree(gameTable))
                        //Nếu không chuyển đến free được thì log lỗi ra
                        if (!tableManager.moveToFree(gameTable))
                            logger.error("[joinTable] - Can't move GameTable["+gameTable.getId()+"] to listFree");

                    //Kiểm tra nếu thằng player là Bot
                    if (fishingPlayer.isBot()) {
                        listBots.remove(fishingPlayer);
                        listBotInRooms.add((Bot) fishingPlayer);
                    }
                }
//                System.out.println(fishingPlayer.getId() + " join in game room: " +  roomType.getRoomId() + " game table: " + gameTable.getId());
            }
            else
            {
                logger.error("Can't join to table!");
            }
        } catch (Exception ex)
        {
            logger.error("Join table error : " + ex);
        }
    }

    void leaveTable(FishingPlayer fishingPlayer){
        boolean flagError;
        //if (fishingPlayer.isPlaying()){
            GameTable gameTable = tableManager.getActiveTable(fishingPlayer.getPlayingInTable());
            if (gameTable != null){
                try{
                    gameTable.playerLeave(fishingPlayer);
                    //Nếu leave thành công
                    if (!fishingPlayer.isPlaying()) {
                        //Bàn đang free thì move cái bàn đó về listFree
                        if (gameTable.isFree()) tableManager.moveToFree(gameTable);
                        else tableManager.addAvailable(gameTable);


                        if (fishingPlayer.isBot()) {
                            listBotInRooms.remove(fishingPlayer);
                            listBots.add((Bot) fishingPlayer);
                        }
                    }

                    flagError = false;//fishingPlayer.isPlaying();
                } catch (Exception ex){
                    flagError = true;
                    logger.trace("Leave error - " + ex.getStackTrace().toString());
                }
            } else {
                flagError = true;
            }
        //}
//        else {
//            flagError = true;
//        }
        if (flagError){
            //TODO send error to client
            logger.error("Player[" + fishingPlayer.getId() + "] can't leave Table[" + fishingPlayer.getPlayingInTable() + "]");
        }
    }


    GameTable getActiveTable(int tableId){
        return tableManager.getActiveTable(tableId);
    }

    public RoomType getRoomType() {
        return roomType;
    }

    int getPlayerInRoom(){
        Integer numOfPlayer = 0;
        for (GameTable gameTable : tableManager.getListActiveTable().values()){
            numOfPlayer += (GameTable.NUMBER_OF_PLAYER - gameTable.getFreeSlot());
        }
        return numOfPlayer;
    }

    public DRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public Document getRoomInfoDocument(){
        Document roomInfoDocument = new Document();
        roomInfoDocument.put(MongoConst.FIELD_ROOM_ID, this.getRoomType().getRoomId());
        roomInfoDocument.put(MongoConst.FIELD_ROOM_INFO_NAME, this.getRoomType().getRoomName());
        roomInfoDocument.put(MongoConst.FIELD_ROOM_INFO_FUND, FundManager.getInstance().getFund(getRoomType()));
        roomInfoDocument.put(MongoConst.FIELD_ROOM_INFO_FUND_DEALER, FundManager.getInstance().getFundDealer(getRoomType()));
        return roomInfoDocument;
    }

    public RoomFund getRoomFund() {
        return roomFund;
    }
}
