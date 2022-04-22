package games.shootingfish;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import games.APISendMoney;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.constant.ServerCommandConstant;
import games.core.database.MongoManager;
import games.core.database.document.UserMoney;
import games.core.exception.NullUserException;
import games.core.user.User;
import games.shootingfish.constant.RequestBeanFishingConst;
import games.shootingfish.constant.ResponseBeanFishingConst;
import games.shootingfish.constant.ResponseFishingErrorCodeConst;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.exception.NullPlayerException;
import games.shootingfish.exception.TableException;
import games.shootingfish.manager.FishingPlayerManager;
import games.shootingfish.object.FishingPlayer;
import libs.util.LogFactory;
import message.CommonProtos.ChatRequestBean;
import message.FishShootingProtos.*;
import message.UserProtos;
import message.XProtos.XMessage;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;
import static games.core.constant.ServerCommandConstant.GAME_COMMAND;

/**
 * Created by WINDNCC on 7/11/2017.
 */

public class GameCommand extends ICommand implements RequestBeanFishingConst, ResponseBeanFishingConst, ResponseFishingErrorCodeConst
{
    private static MoneyService moneyService = MoneyService.getInstance();

    private static GameCommand instance;
    private static GameLobby gameLobby;

    private static FishingPlayerManager fishingPlayerManager;
    private static Logger logger;

    public static GameCommand getInstance() {
        if (instance == null) instance = new GameCommand();
        return instance;
    }

    private GameCommand()
    {
        gameLobby = GameLobby.getInstance();
        fishingPlayerManager = FishingPlayerManager.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    @Override
    public void process(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException {
        if (ctx.checkAuth()){
            int type = message.getBeanType();
            switch (type){

                case HIT_FISH_REQUEST_BEAN:
                    processHitFish(ctx, message);
                    break;

                case INSTANT_JOIN_TABLE_REQUEST_BEAN:
                    processInstantJoinTable(ctx, message);
                    break;

                case JOIN_TABLE_REQUEST_BEAN:
                    processJoinTable(ctx, message);
                    break;
                case CHANGE_GUN_REQUEST_BEAN:
                    processChangeGun(ctx, message);
                    break;
                case CHANGE_SLOT_REQUEST_BEAN:
                    processChangeSlot(ctx, message);
                    break;

                case NEW_BULLET_REQUEST_BEAN:
                    processGenerateBullet(ctx, message);
                    break;
                case GET_TIMING_SPAWN_FISH_AT_JOIN_REQUEST_BEAN:
                    processGetTimingSpawnFishAtJoin(ctx, message);
                    break;

                case PLAYER_LEAVE_TABLE_REQUEST_BEAN:
                case 9999:
                    processPlayerLeaveTable(ctx, message);
                    break;

                case GET_LIST_ROOM_INFO_REQUEST_BEAN:
                    processListRoomInfo(ctx, message);
                    break;

                case CHAT_IN_TABLE_REQUEST_BEAN:
                    processChatInTable(ctx, message);
                    break;

//                case GET_TREASURE:
//                    HandleGiftsEveryDay(ctx);
//                    break;

                case GET_AGENCY:
                    GetAgencyFromMongo(ctx, message);
                    break;

                case GET_RANK:
                    GetRankFromMongo(ctx, message);
                    break;

                case GOLD_FROM_ADS:
                    AddGoldForUser(ctx, message);
                    break;

                case GOLD_FROM_IAP:
                    AddGoldFromIAP(ctx, message);
                    break;

                case CHANGE_AVATAR:
                    ChangeAvatar(ctx, message);
                    break;

                case CHECK_WALLET:
                    CheckWallet(ctx);
                    break;

            }
        }
    }

    private void CheckWallet(ChannelContextInfo ctx)
    {
        MongoClient client = MongoManager.getInstance().getClient();
        MongoDatabase database = client.getDatabase("fish");
        MongoCollection<Document> collection = database.getCollection("userMoney");
        Document document = collection.find(eq("_id", ctx.getUserId())).first();

        if (document != null)
        {
            try
            {
                long goldWallet = (Long) document.get("depositFrWallet");


                User user = ctx.getUser();
                long totalGold = user.getMoneyGold() + goldWallet;

                user.addMoneyBagToUserMoney(goldWallet);
                UserMoney userMoney = user.getUserMoney();
                userMoney.setDepositFrWallet(0); // Sau khi get ra từ mongo r thì set nó lại = 0
                userMoney.setDepositFrWalletProvisional(0);
                user.saveUserMoney();

                UserProtos.UpdateGoldAndSilverResponseBean.Builder builder =  UserProtos.UpdateGoldAndSilverResponseBean.newBuilder();
                builder.setGold(totalGold);
                builder.setSilver(goldWallet); // Tiền từ wallet bỏ tạm vào silver, vì silver k dc use

                sendMessage(ctx, builder.build().toByteString(), (short) CHECK_WALLET);
            }
            catch (Exception e)
            {

            }
        }
    }

    private void ChangeAvatar(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {
        UserProtos.LoginResponseBean userInfo = UserProtos.LoginResponseBean.parseFrom(message.getData());
        MongoDatabase mongo = MongoManager.getInstance().getDatabase();
        MongoCollection collection = mongo.getCollection("users");

        if (collection == null)
            mongo.createCollection("users");

        Document doc = (Document) collection.find().first();
        if (doc != null)
        {
            Bson filter = new Document("_id", ctx.getUserId());
            int idAvatar = Math.max(Integer.parseInt(userInfo.getAvatarUrl()), 0);
            Bson newValue = new Document("avatar", String.valueOf(idAvatar));
            Bson updateOperationDoc = new Document("$set", newValue);
            collection.updateOne(filter, updateOperationDoc);

            ctx.getUser().setAvatar(String.valueOf(idAvatar));
        }
    }

    private void AddGoldFromIAP(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {
        Request_DailyReward reward = Request_DailyReward.parseFrom(message.getData());
        long gold = reward.getMoney();
        Money newMoney = new Money(gold, MoneyType.GOLD);
        ctx.getUser().SetMoney(gold);
        moneyService.addMoneyToSave(newMoney, ctx.getUser(), true, "Mua_Tu_CHPlay");

        Request_DailyReward.Builder builder =  Request_DailyReward.newBuilder();
        long newGold = ctx.getUser().getMoneyGold();
        builder.setMoney(newGold);
        sendMessage(ctx, builder.build().toByteString(), (short) GOLD_FROM_ADS);
    }

    private void AddGoldForUser(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {
        User user = ctx.getUser();

        if (user.countNumberTimeWatchAds >= 34)
            return;

        if (user.openAds)
        {
            long gold = 3000;
            Money newMoney = new Money(gold, MoneyType.GOLD);
            user.SetMoney(gold);
            moneyService.addMoneyToSave(newMoney, user, true, "Xem_Video");

            Request_DailyReward.Builder builder = Request_DailyReward.newBuilder();
            long newGold = user.getMoneyGold();
            builder.setMoney(newGold);
            sendMessage(ctx, builder.build().toByteString(), (short) GOLD_FROM_ADS);

            user.openAds = false;
            user.After15Seconds();
            user.countNumberTimeWatchAds++;

            if (user.countNumberTimeWatchAds == 34)
                user.After24Hour();
        }
    }


    private void GetRankFromMongo(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {
        MongoClient client = MongoManager.getInstance().getClient();

        MongoDatabase database = client.getDatabase("fish");

        MongoCollection<Document> collection = database.getCollection("RankFish");

        if (collection == null)
        {
            database.createCollection("RankFish");
            collection = database.getCollection("RankFish");
        }

        if (collection.count() == 0)
        {
            collection.insertOne(new Document()
                    .append("id", "WinGold")
                    .append("Rank", "1")
                    .append("Name", "NumberOne")
                    .append("Value", "1.980.000.000"));

            collection.insertOne(new Document()
                    .append("id", "TimePlay")
                    .append("Rank", "1")
                    .append("Name", "NumberOne")
                    .append("Value", "350h"));
        }



        DataRank rank = DataRank.parseFrom(message.getData());
        String type = rank.getType();

        ArrayList<Document> documents = (ArrayList<Document>) collection.find(eq("id", type)).into(new ArrayList<Document>());
        DataRank.Builder dataRank = DataRank.newBuilder();
        if (documents != null)
        {
            for (Document document : documents)
            {
                RankInfoFromMongo.Builder rankInfo = RankInfoFromMongo.newBuilder();

                rankInfo.setRank((String) document.get("Rank"));
                rankInfo.setName((String) document.get("Name"));
                rankInfo.setValue((String) document.get("Value"));

                dataRank.addListRank(rankInfo);
            }
        }

        ByteString newMessage = dataRank.build().toByteString();
        sendMessage(ctx, newMessage, (short) GET_RANK);
    }

    private void GetAgencyFromMongo(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {
        MongoClient client = MongoManager.getInstance().getClient();

        MongoDatabase database = client.getDatabase("fish");

        MongoCollection<Document> collection = database.getCollection("Agency");

        if (collection == null)
        {
            database.createCollection("Agency");
            collection = database.getCollection("Agency");
        }

        if (collection.count() == 0)
        {
            collection.insertOne(new Document()
                    .append("id", "HCM")
                    .append("Name", "HCM")
                    .append("PhoneNumber", "086.705.74.79")
                    .append("Address", "Hồ Chí Minh")
                    .append("Zalo", "086.705.74.79"));

            collection.insertOne(new Document()
                    .append("id", "HaNoi")
                    .append("Name", "Hà Nội")
                    .append("PhoneNumber", "086.707.74.79")
                    .append("Address", "Hà Nội")
                    .append("Zalo", "086.707.74.79"));

            collection.insertOne(new Document()
                    .append("id", "CanTho")
                    .append("Name", "Cần Thơ")
                    .append("PhoneNumber", "086.709.74.79")
                    .append("Address", "Cần Thơ")
                    .append("Zalo", "086.709.74.79"));

            collection.insertOne(new Document()
                    .append("id", "DaNang")
                    .append("Name", "Đà Nẵng")
                    .append("PhoneNumber", "096.529.05.78")
                    .append("Address", "Đà Nẵng")
                    .append("Zalo", "096.529.05.78"));

            collection.insertOne(new Document()
                    .append("id", "TayNguyen")
                    .append("Name", "Tây Nguyên")
                    .append("PhoneNumber", "036.795.666.5")
                    .append("Address", "Tây Nguyên")
                    .append("Zalo", "036.795.666.5"));

            collection.insertOne(new Document()
                    .append("id", "HaiPhong")
                    .append("Name", "Đặng Tâm 111")
                    .append("PhoneNumber", "0917.693.950")
                    .append("Address", "Hải Phòng")
                    .append("Zalo", "0917.693.950"));

            collection.insertOne(new Document()
                    .append("id", "MienBac")
                    .append("Name", "Đặng Tâm 111")
                    .append("PhoneNumber", "0917.693.950")
                    .append("Address", "Miền Bắc")
                    .append("Zalo", "0917.693.950"));

        }


        DataAgency agency = DataAgency.parseFrom(message.getData());
        String regionName = agency.getRegionalName();

        ArrayList<Document> documents = (ArrayList<Document>) collection.find(eq("id", regionName)).into(new ArrayList<Document>());

        DataAgency.Builder dataAgency = DataAgency.newBuilder();

        if (documents != null)
        {
            for (Document document : documents)
            {
                AgencyInfoFromMongo.Builder agencyInfo = AgencyInfoFromMongo.newBuilder();

                agencyInfo.setAgencyName((String) document.get("Name"));
                agencyInfo.setPhoneNumber((String) document.get("PhoneNumber"));
                agencyInfo.setAddress((String) document.get("Address"));
                agencyInfo.setZalo((String) document.get("Zalo"));

                dataAgency.addListAgency(agencyInfo);
            }
        }

        ByteString newMessage = dataAgency.build().toByteString();
        sendMessage(ctx, newMessage, (short) GET_AGENCY);

    }

    private void HandleGiftsEveryDay(ChannelContextInfo ctx)
    {
        if (ctx.getUser().getMoneyGold() < 50000)
        {
            Money newMoney = new Money(1000, MoneyType.GOLD);
            ctx.getUser().SetMoney(newMoney.getAmount());
            moneyService.addMoneyToSave(newMoney, ctx.getUser(), true, "HandleGiftsEveryDay");

            MongoClient client = MongoManager.getInstance().getClient();
            MongoDatabase database = client.getDatabase("fish");
            MongoCollection<Document> collection = database.getCollection("userMoney");
            Document document = collection.find(Filters.eq("_id", ctx.getUserId())).first();

            UserProtos.LoginResponseBean.Builder response = UserProtos.LoginResponseBean.newBuilder();
            long money = (long) document.get("gold");
            response.setGold(money);
            response.setErrorCode(0);

            ByteString message = response.build().toByteString();
            sendMessage(ctx, message, SEND_TREASURE);
        }
    }

//    private void processGetTimingSpawnFish(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
//        TimingSpawnFishRequestBean requestBean = TimingSpawnFishRequestBean.parseFrom(message.getData());
//        TimingSpawnFishResponseBean.Builder responseBean;
//        try{
//            //lấy timingSpawnFish chuyển sang dạng trả về cho client
//            responseBean = gameService.getTimingSpawnFish(requestBean.getTableId()).parseResponseBeanBuilder();
//            responseBean.setCurrentState(gameService.getCurrentStateId(requestBean.getTableId()));
//            float timeCur = gameService.getCurrentStateTimePlaying(requestBean.getTableId());
//            responseBean.setCurrentStateTime(timeCur);
//            //Chuyển thành XMessage để trả về client
//            XMessage.Builder responseMessage =
//                    XMessage.newBuilder()
//                    .setCommand(GAME_COMMAND)
//                    .setBeanType(GET_TIMING_SPAWN_FISH_REQUEST_BEAN)
//                    .setData(responseBean.build().toByteString());
//            ctx.writeAndFlush(responseMessage.build());
//        } catch (GameException ex){
//            logger.trace("[processGetTimingSpawnFish in GameCommand] - not define Error : ", ex);
//        }
//    }

    private void processListRoomInfo(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
        //ListRoomInfoRequestBean.Builder requestBean = ListRoomInfoRequestBean.newBuilder();
        ListRoomInfoResponseBean.Builder responseBean = ListRoomInfoResponseBean.newBuilder();
        try {
            HashMap<Integer, GameRoom> listGameRoom = gameLobby.getListGameRoom();

            listGameRoom.values().forEach((GameRoom gameRoom) ->{
                RoomInfoResponseBean.Builder roomInfoResponseBean = RoomInfoResponseBean.newBuilder();
                RoomType roomType = gameRoom.getRoomType();
                roomInfoResponseBean.setRoomId(roomType.getRoomId());
                roomInfoResponseBean.setBaseGun(roomType.getBaseGunMoney());
                roomInfoResponseBean.setMinGold(roomType.getBaseGunMoney());
                responseBean.addListRoom(roomInfoResponseBean);
            });
            responseBean.setErrorCode(ResponseFishingErrorCodeConst.REC_SUCCESS);
            sendMessage(ctx, responseBean.build().toByteString(), LIST_ROOM_INFO_RESPONSE_BEAN);
        }catch (Exception e) {
            logger.error("List room error", e);
            responseBean.setErrorCode(ResponseFishingErrorCodeConst.REC_NOT_DEFINE);
            sendMessage(ctx, responseBean.build().toByteString(), LIST_ROOM_INFO_RESPONSE_BEAN);
        }

    }

    private void processGetTimingSpawnFishAtJoin(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
        try{
            TimingSpawnFishRequestBean requestBean = TimingSpawnFishRequestBean.parseFrom(message.getData());
            gameLobby.getActiveTable(requestBean.getTableId()).sendTimingSpawnFishAtJoin(ctx);
        } catch (Exception ex){
            logger.error("[processGetTimingSpawnFishAtJoin in GameCommand] - can't get list fish at join!", ex);
            TimingSpawnFishResponseBean.Builder response = TimingSpawnFishResponseBean.newBuilder();
            response.setErrorCode(REC_NOT_DEFINE);
            XMessage.Builder xMessage = XMessage.newBuilder();
            xMessage.setCommand(GAME_COMMAND).setBeanType(TIMING_SPAWN_FISH_AT_JOIN_RESPONSE_BEAN)
                    .setData(response.build().toByteString());
            ctx.writeAndFlush(xMessage.build());
        }

    }

    private void processInstantJoinTable(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
        try
        {
            gameLobby.instantJoinTable(ctx.getUserId());
        }
        catch (NullPlayerException | TableException e)
        {
            logger.error(e.getMessage());
            JoinTableResponseBean.Builder responseBean = JoinTableResponseBean.newBuilder();
            responseBean.setErrorCode(REC_NOT_DEFINE);
            sendMessage(ctx, responseBean.build().toByteString(), ResponseBeanFishingConst.JOIN_TABLE_RESPONSE_BEAN);
        }
    }

    private void processJoinTable(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
            //Init
            JoinTableRequestBean requestBean = JoinTableRequestBean.parseFrom(message.getData());
            JoinTableResponseBean.Builder responseBean = JoinTableResponseBean.newBuilder();
            int roomId = requestBean.getRoomId();
            int userId = ctx.getUserId();
            boolean flagError = false;
            FishingPlayer getPlayer = null;

            try{
                if (roomId != RoomType.NONE)
                {
                    GameRoom gameRoom = gameLobby.getRoom(roomId);
                    if (gameRoom != null)
                    {
                        getPlayer = fishingPlayerManager.addPlayer(userId);
                        long moneyPlayer = ctx.getUser().getMoneyGold();
                        if (roomId == RoomType.TRIAL)
                        {
                            getPlayer.resetMoneyTrial();
                            moneyPlayer = getPlayer.getMoneyTrial();
                        }
                        if (moneyPlayer >= gameRoom.getRoomType().getBaseGunMoney())
                        {
                            if (getPlayer != null)
                            {
                                //Nếu là room trial thì giảm lượt trial của nó xuống, block finally sẽ save xuống DB sau
                                if (roomId == RoomType.TRIAL)
                                {
                                    getPlayer.decreaseNumOfTrial();
                                }

                                if (!getPlayer.isBot())
                                    gameRoom.joinTable(getPlayer);
                            }
                            else
                             {
                                responseBean.setErrorCode(REC_JOIN_TABLE_USER_NOT_FOUND);
                                flagError = true;
                            }
                        }
                        else
                        {
                            responseBean.setErrorCode(REC_JOIN_TABLE_FAILD_NOT_ENOUGH_MONEY);
                            flagError = true;
                        }

                    }
                    else
                    {
                        responseBean.setErrorCode(REC_JOIN_TABLE_ROOM_NOT_FOUND);
                        flagError = true;
                    }
                }
                else {
                    responseBean.setErrorCode(REC_JOIN_TABLE_ROOM_NOT_FOUND);
                    flagError = true;
                }
            } catch (NullUserException nullUserEx){
                responseBean.setErrorCode(REC_NOT_DEFINE);
                logger.error("[Join table in GameCommand] - Player["+userId+"] join to Room["+roomId+"] error " + nullUserEx.getMessage() + " : ", nullUserEx);
                flagError = true;
            } catch (Exception ex){
                logger.error("[Join table in GameCommand] - Player["+userId+"] join to Room["+roomId+"] error NOT DEFINE: ", ex);
                responseBean.setErrorCode(REC_NOT_DEFINE);
                flagError = true;
            }
            finally
            {
                if (flagError)
                {
                    //Nếu xảy ra lỗi ko join bàn được mà là room trial thì refund lại cái lượt chơi trial cho nó
                    if (getPlayer != null && roomId == RoomType.TRIAL) getPlayer.increaseNumOfTrial();

                    XMessage.Builder messageResponse = XMessage.newBuilder();
                    messageResponse.setCommand(GAME_COMMAND).setBeanType(JOIN_TABLE_RESPONSE_BEAN);
                    messageResponse.setData(responseBean.build().toByteString());

                    //remove khoi fishing player
                    fishingPlayerManager.removePlayer(userId);

                    ctx.writeAndFlush(messageResponse.build());
                }
                else
                {
                    //Nếu không xảy ra lỗi mà join vào được thì lưu cái lượt trial đã bị trừ xuống DB

                    try {
                        if (getPlayer != null && roomId == RoomType.TRIAL) {
                            getPlayer.saveUserMiniGame();
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
    }

    private void processChangeGun(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {
            try
            {
                ChangeGunRequestBean requestBean = ChangeGunRequestBean.parseFrom(message.getData());
                GameTable gameTable = gameLobby.getTableByPlayerId(ctx.getUserId());
                if (gameTable != null){
                    gameTable.changeGun(requestBean, ctx);
                }
            }
            catch (Exception e)
            {
                ChangeGunResponseBean.Builder responseBean = ChangeGunResponseBean.newBuilder();
                responseBean.setErrorCode(REC_NOT_DEFINE);
                sendMessage(ctx, responseBean.build().toByteString(), ResponseBeanFishingConst.PLAYER_CHANGE_GUN_RESPONSE_BEAN);
            }
    }

    private void processChangeSlot(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
        ChangeSlotResponseBean.Builder responseBean = ChangeSlotResponseBean.newBuilder();
        responseBean.setErrorCode(REC_SUCCESS);
        //Nếu gửi đổi thất bại thì sẽ gửi response ở hàm này
        //Gửi thành công thì gửi response trong thằng gameTable
        boolean flagFail = false;
        try {
            ChangeSlotRequestBean requestBean = ChangeSlotRequestBean.parseFrom(message.getData());
            int userId = ctx.getUserId();
            GameTable gameTable = gameLobby.getTableByPlayerId(userId);
            if (gameTable != null){
                byte newSlot = Integer.valueOf(requestBean.getSlotId()).byteValue();
                gameTable.changeSlot(userId, newSlot);
            } else {
                responseBean.setErrorCode(REC_TABLE_NOT_FOUND);
                flagFail = true;
            }
        } catch (Exception e){
            flagFail = true;
            responseBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("[processChangeSlot in GameCommand] - not define error!", e);
        } finally {
            if (flagFail)
                sendMessage(ctx, responseBean.build().toByteString(), ResponseBeanFishingConst.PLAYER_CHANGE_SLOT_RESPONSE_BEAN);
        }
    }

    private void processGenerateBullet(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
        try{
            BulletRequestBean requestBean = BulletRequestBean.parseFrom(message.getData());
            GameTable gameTable = gameLobby.getTableByPlayerId(ctx.getUserId());
            if (gameTable != null) {
                gameTable.requestFireBullet(requestBean);
//                FishingPlayer fishingPlayer = gameTable.getPlayerInSlot(ctx.getUserId());
//                if (fishingPlayer != null){
//                    gameTable.generateBullet(requestBean, fishingPlayer);
//                }
            }
        } catch (Exception ex){
            logger.error("[processGenerateBullet] - Can't generate Bullet, Player["+ctx.getUserId()+"]");
        }
    }

    private void processHitFish(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {
        HitFishRequestBean requestBean = HitFishRequestBean.parseFrom(message.getData());
        try {
            GameTable gameTable = gameLobby.getTableByPlayerId(ctx.getUserId());
            if (gameTable != null){
                //gameTable.hitFish(requestBean, ctx.getUserId());
                gameTable.hitFish(requestBean, requestBean.getPlayerId());
            }
        } catch (Exception ex){
            logger.error("[processHitFish in GameCommand] - error when Player["+ctx.getUserId()+"] hit Fish["+requestBean.getFishId()+"] by Bullet["+requestBean.getBulletId()+"]: ", ex);
        }
    }

    private void processPlayerLeaveTable(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
        try {
            LeaveTableRequestBean leaveTableRequestBean = LeaveTableRequestBean.parseFrom(message.getData());
            int playerId = leaveTableRequestBean.getPlayerId();
            if (playerId == ctx.getUserId()) {
                leaveTable(ctx);
            } else {
                //rời bàn cho bot
                GameTable gameTable = gameLobby.getTableByPlayerId(ctx.getUserId());
                if (gameTable != null){
                    FishingPlayer bot = gameTable.getBotById(playerId);
                    if (bot != null) {
                        GameRoom gameRoom = gameLobby.getRoomByTableId(bot.getPlayingInTable());
                        if (gameRoom != null){
                            gameRoom.leaveTable(bot);
                        }
                    } else {
                        logger.info("leave table for bot error, Bot null - bot id = " + playerId);
                    }
                } else {
                    logger.info("leave table for bot error, table null - bot id = " + playerId);
                }
            }
        } catch (Exception e) {
            logger.error("Process leave table error", e);
        }


    }

    private void processChatInTable(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException{
        ChatRequestBean requestBean = ChatRequestBean.parseFrom(message.getData());
        String chatMessage = requestBean.getMessage();
        int userId = ctx.getUserId();
        GameTable gameTable = gameLobby.getTableByPlayerId(userId);
        if (gameTable != null)
            gameTable.chat(userId, chatMessage);
    }

    public void leaveTable(ChannelContextInfo ctx) {
        try {
            APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                    ctx.getUser().getUserName(),
                    Long.toString(ctx.getUser().getMoneyGold()),0,""));
        }
        catch ( Exception e){
            logger.warn("Not sending API successfull");
        }
            try{

                FishingPlayer fishingPlayer = fishingPlayerManager.getPlayer(ctx.getUserId());
                if (fishingPlayer != null) {
                    GameRoom gameRoom = gameLobby.getRoomByTableId(fishingPlayer.getPlayingInTable());
                    if (gameRoom != null){
                        gameRoom.leaveTable(fishingPlayer);
                    } else {
                        logger.error("Leave table error - room null" );
                    }
                } else {
                    logger.info("Leave table error, get player null");
                }
            } catch (Exception ex){
                LeaveTableResponseBean.Builder response = LeaveTableResponseBean.newBuilder();
                response.setErrorCode(REC_NOT_DEFINE);
                sendMessage(ctx, response.build().toByteString(), ResponseBeanFishingConst.PLAYER_LEAVE_TABLE_RESPONSE_BEAN);
                logger.error("Leave table error - ", ex);
            }
    }

    private void sendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, short responseCode){
        XMessage.Builder responseMessage = XMessage.newBuilder();
        responseMessage.setCommand(GAME_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);
        ctx.writeAndFlush(responseMessage.build());
    }
}
