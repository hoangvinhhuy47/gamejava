package games.shootingfish;

import com.google.protobuf.ByteString;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.user.User;
import games.shootingfish.constant.ResponseBeanFishingConst;
import games.shootingfish.constant.ResponseFishingErrorCodeConst;
import games.shootingfish.data.DataManager;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.datatype.TimingSpawnFish;
import games.shootingfish.manager.FishingPlayerManager;
import games.shootingfish.manager.FundManager;
import games.shootingfish.manager.GunManager;
import games.shootingfish.object.*;
import libs.util.CRandom;
import libs.util.LogFactory;
import message.CommonProtos;
import message.FishShootingProtos;
import message.FishShootingProtos.*;
import message.MessageManager;
import message.XProtos.XMessage;
import org.apache.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static games.core.constant.ServerCommandConstant.GAME_COMMAND;

//Protos

/**
 * Created by WINDNCC on 7/10/2017.
 */
public class GameTable implements ResponseBeanFishingConst, ResponseFishingErrorCodeConst {

    public static final int NUMBER_OF_PLAYER = 8;

    /*
    * So luong bot add vao de dam bao tong luong nguoi trong phong bang MaxPlayerWithBot
    * */
    public static final int MAX_PLAYER_WITH_BOT = 5;
    /**
     * id of table
     */
    private int id;

    /**
     * new fishShooting instance
     */
    private FishShooting fishShooting;

    /**
     * HashMap hold tx with player
     */
    private ConcurrentHashMap<Byte, FishingPlayer> hashSlot;

    /**
     * Nếu tx không có ai thì sẽ chứa thằng player ảo này
     */
    private static final FishingPlayer FREE_PLAYER = new FishingPlayer();

    /**
     * =true when table full tx
     */
    private boolean isFull = false;
    /**
     * =true when nobody in this table
     */
    private boolean isFree = true;

    /**
     * num of tx remaining
     */
    private byte freeSlot = NUMBER_OF_PLAYER;


    // static reference

    /**
     * Reference to MessageManager
     */
    private static MessageManager messageManager = MessageManager.getInstance();

    /**
     * Reference to singleton DataManager
     */
    private static FishingPlayerManager fishingPlayerManager = FishingPlayerManager.getInstance();

    /**
     * invoke money use this please
     */
    private static MoneyService moneyService = MoneyService.getInstance();

    /**
     * manager fund
     */
    private static FundManager fundManager = FundManager.getInstance();

    /**
     * use for log
     */
    private static Logger logger = LogFactory.getLogger(GameTable.class.getSimpleName());

    /**
     * Reentrant lock
     */
    private Lock lock;
    /**
     * room type
     */
    private RoomType roomType;

    /*
    * List bot
    * */
    private CopyOnWriteArrayList<Bot> listBot;

    /*
    * host
    * */
    private FishingPlayer host;

    /**
     * temp fund
     */
    private long tempFund;

    /*
    * Ty le kill ca giam theo viec nhieu nguoi vao ban 1 luc
    * */
    private float rateKillFishByPlayerInRoom = 1.0f;

    /*
    * So luong ca tang len dua theo so nguoi choi
    * */
    private int countFishInscreaseByPlayerInRoom = 0;

    /**
     * constructor
     * @param tableId id of table
     */
    public GameTable(int tableId, RoomType roomType)
    {
        this.tempFund = 0;

        this.roomType = roomType;

        listBot = new CopyOnWriteArrayList<>();

        if (roomType.getRoomId() == RoomType.NORMAL)
        {
            long fund = fundManager.getFund(roomType);
            logger.info("Fund  = " + fund + " - room type = " + roomType.getRoomName() +  " - room id = " + roomType.getRoomId());
        }
        this.id = tableId;

        //mặc định tạo ra 4 tx
        hashSlot = new ConcurrentHashMap<>(5,1);

        for (byte i = 1; i<=NUMBER_OF_PLAYER; i++)
        {
            hashSlot.put(i, FREE_PLAYER);
        }
        lock = new ReentrantLock();
        //End generate tx
        fishShooting = new FishShooting(this);
        host = null;
    }

    public FishShooting getFishShooting() {
        return fishShooting;
    }

    public FishingPlayer getBotById(int botId) {
        for (FishingPlayer bot : listBot) {
            if (bot.getId() == botId) {
                return bot;
            }
        }
        return null;
    }

    void sendToAll(ByteString responseBeanByteString, short responseCode){
        if (freeSlot < NUMBER_OF_PLAYER)
        {
            XMessage.Builder responseMessage = XMessage.newBuilder();
            responseMessage.setCommand(GAME_COMMAND)
                            .setBeanType(responseCode)
                            .setData(responseBeanByteString);
            for (FishingPlayer fishingPlayer : hashSlot.values()) {
                if (fishingPlayer != null && fishingPlayer.getId() != 0 && !fishingPlayer.isBot() && fishingPlayer.getCtx() != null) {
                    messageManager.send(fishingPlayer.getCtx(), responseMessage.build());
                }
            }
        } //else {
//            logger.info(String.format("[sendToAll] - Nobody in Table[%s]", this.getId()));
//        }
    }

    private void sendToPlayer(int playerId, ByteString responseBeanByteString, short responseCode)
    {
        if (!hashSlot.isEmpty())
        {
            FishingPlayer fishingPlayer = getPlayerInSlot(playerId);

            if (fishingPlayer.isBot())
                return;
            if (fishingPlayer != null && fishingPlayer.getId() == playerId && fishingPlayer.getCtx() != null)
            {
                XMessage.Builder responseMessage = XMessage.newBuilder();
                responseMessage.setCommand(GAME_COMMAND)
                        .setBeanType(responseCode);
                if (responseBeanByteString != null) {
                    responseMessage.setData(responseBeanByteString);
                }

                messageManager.send(fishingPlayer.getCtx(), responseMessage.build());
            }
        } //else {
//            logger.info(String.format("[sendToPlayer] - not found FishingPlayer[%s] in Table[%s]", playerId, this.getId()));
//        }
    }

    private void sendToOtherPlayer(int playerId, ByteString responseBeanByteString, short responseCode){
        XMessage.Builder responseMessage = XMessage.newBuilder();
        responseMessage.setCommand(GAME_COMMAND)
                .setBeanType(responseCode);
        if (responseBeanByteString != null) {
            responseMessage.setData(responseBeanByteString);
        }
        //Bàn không rỗng và có ít nhất 2 người chơi trỏ lên mới gửi
        if (!isFree() && freeSlot < NUMBER_OF_PLAYER){
            for (FishingPlayer fishingPlayer : hashSlot.values()){
                if (fishingPlayer != null && fishingPlayer.getId() != 0 && fishingPlayer.getId() != playerId && !fishingPlayer.isBot()){

                    messageManager.send(fishingPlayer.getCtx(), responseMessage.build());
                }
            }
        } //else {
//            logger.info(String.format("[sendToOtherPlayer] - Nobody or just one FishingPlayer in Table[%s]", this.getId()));
//        }
    }

    private void sendToCtx(ChannelContextInfo ctx, ByteString responseBeanByteString, short responseCode){
        if (ctx != null){
            XMessage.Builder responseMessage = XMessage.newBuilder();
            responseMessage.setCommand(GAME_COMMAND)
                    .setBeanType(responseCode);
            if (responseBeanByteString != null) {
                responseMessage.setData(responseBeanByteString);
            }
            messageManager.send(ctx, responseMessage.build());
        }
    }

    void chat(int userId, String message){
        CommonProtos.ChatResponseBean.Builder responseBean = CommonProtos.ChatResponseBean.newBuilder();
        responseBean.setMessage(message);
        responseBean.setUserId(userId);
        sendToOtherPlayer(userId, responseBean.build().toByteString(), CHAT_IN_TABLE_RESPONSE_BEAN);
    }

    void sendTimingSpawnFishAtJoin(ChannelContextInfo ctx){
        TimingSpawnFish timingSpawnFish = fishShooting.getTimingSpawnFishAtJoin();
        TimingSpawnFishResponseBean.Builder timingSpawnFishResponse = timingSpawnFish.parseResponseBeanBuilder();
        timingSpawnFishResponse.setStateDuringTime(fishShooting.getGameState().getTimeDuring())
                .setTimestampStartState(fishShooting.getTimeStampStateStart())
                .setCurrentStateTime((float)fishShooting.getStateTimePlaying())
                .setCurrentState(fishShooting.getGameState().getId())
                .setBgId(fishShooting.getGameState().getBgId());
        if (ctx != null){
            this.sendToCtx(ctx, timingSpawnFishResponse.build().toByteString(), TIMING_SPAWN_FISH_AT_JOIN_RESPONSE_BEAN);
        }
    }

    FishingPlayer getPlayerInSlot(int playerId){
        for (FishingPlayer fishingPlayer : hashSlot.values()){
            if (fishingPlayer.getId() != 0 && fishingPlayer.getId() == playerId) return fishingPlayer;
        }
        return null;
    }

    void changeSlot(int playerId, byte newSlotId){
        lock.lock();
        boolean flagChangeSuccess = false;
        ChangeSlotResponseBean.Builder responseBean = ChangeSlotResponseBean.newBuilder();
        responseBean.setErrorCode(REC_NOT_DEFINE);
        try {
            FishingPlayer playerInSlot = getPlayerInSlot(playerId);
            //Kiểm tra Player xem có hợp lệ không (Có đang chơi, có chơi trong phòng này không)
            //Và tx mới có free không?
            if (playerInSlot != null
                    && playerInSlot.isPlaying()
                    && playerInSlot.getPlayingInTable() == this.id
                    && isFreeSlot(newSlotId)){
                    hashSlot.replace(playerInSlot.getSlot(), playerInSlot, FREE_PLAYER);
                    hashSlot.replace(newSlotId, FREE_PLAYER, playerInSlot);
                    playerInSlot.setSlot(newSlotId);
                    flagChangeSuccess = true;
            }
            if (!isFreeSlot(newSlotId)) responseBean.setErrorCode(REC_CHANGE_SLOT_NOT_AVAILABLE);
            if (flagChangeSuccess){
                responseBean.setErrorCode(REC_SUCCESS);

                //Send to self
                sendToCtx(playerInSlot.getCtx(), responseBean.build().toByteString(), ResponseBeanFishingConst.PLAYER_CHANGE_SLOT_RESPONSE_BEAN);

                //Send to other
                OtherChangeSlotResponseBean.Builder otherResponseBean = OtherChangeSlotResponseBean.newBuilder();
                otherResponseBean.setPlayerId(playerId);
                otherResponseBean.setSlotId(newSlotId);
                sendToOtherPlayer(playerId, otherResponseBean.build().toByteString(), ResponseBeanFishingConst.OTHER_PLAYER_CHANGE_SLOT_RESPONSE_BEAN);
            } else {
                sendToPlayer(playerId, responseBean.build().toByteString(), ResponseBeanFishingConst.PLAYER_CHANGE_SLOT_RESPONSE_BEAN);
            }
        } catch (Exception ex){
            logger.error(ex);
            sendToPlayer(playerId, responseBean.build().toByteString(), ResponseBeanFishingConst.PLAYER_CHANGE_SLOT_RESPONSE_BEAN);
        } finally {
            lock.unlock();
        }
    }

    /*
    * Check ty le khi co nguoi choi moi vao hoac co nguoi ra
    * */
    private void checkRateByFreeSlot() {
        int playerInRoom = NUMBER_OF_PLAYER - freeSlot - listBot.size();//so luong nguoi choi trong ban
        if (playerInRoom < 0) {
            playerInRoom = 0;
        }
        float rateKillFishDesByPlayer = (playerInRoom / 2) * TimingSpawnFish.RATE_KILL_FISH_2PLAYER;
        rateKillFishByPlayerInRoom = 1.0f - rateKillFishDesByPlayer;

        int countFish = (playerInRoom / 3) * TimingSpawnFish.COUNT_FISH_3PLAYER;
        if (countFish > 1) {
            countFish = 1;
        }
        countFishInscreaseByPlayerInRoom = countFish;

        //logger.info("CheckRateByFreeSlot - playerInRoom = " + playerInRoom + " - rateKillFish " + rateKillFishByPlayerInRoom + " - count fish = " + countFishInscreaseByPlayerInRoom);
    }

    public int getCountFishInscreaseByPlayerInRoom() {
        return countFishInscreaseByPlayerInRoom;
    }

    public  float getRateKillFishByPlayerInRoom() {
        return rateKillFishByPlayerInRoom;
    }

    //khi join thành công trả về true.
    //Ko thì false
    boolean playerJoin(FishingPlayer fishingPlayer)
    {
        lock.lock();
        boolean joinSuccess = false;
        try {
            JoinTableResponseBean.Builder responseBean = JoinTableResponseBean.newBuilder();
            OtherJoinResponseBean.Builder otherJoinResponseBean = OtherJoinResponseBean.newBuilder();

            if (!isFull() && !fishingPlayer.isPlaying())
            {
                for (byte slot = 1; slot <= hashSlot.size(); slot++)
                {
                    if (isFreeSlot(slot))
                    {
                        hashSlot.replace(slot, FREE_PLAYER , fishingPlayer);
                        fishingPlayer.setPlayingInTable(this.getId());
                        fishingPlayer.setSlot(slot);
                        fishingPlayer.setPlaying(true);
                        freeSlot--;
                        checkRateByFreeSlot();

                        checkTableState();
//                        System.out.println(fishingPlayer.getId() + " join in table " + getId() + " success! ");
                        //Send info to self
                        responseBean.setErrorCode(ResponseFishingErrorCodeConst.REC_SUCCESS);
                        responseBean.setTableId(this.getId());
                        responseBean.setCurrentTimeStamp((int)System.currentTimeMillis() / 1000);
                        //Parse all fishingPlayer to PlayerInfoBuilder and add to responseBean

                        if (host == null && !fishingPlayer.isBot())
                        {
                            host = fishingPlayer;
                            fishingPlayer.setHost(true);
                        }
                        for (FishingPlayer fishingPlayerInfo : hashSlot.values()){
                            if (fishingPlayerInfo != null && fishingPlayerInfo.getId() != 0)
                            {
                                responseBean.addPlayerInfo(fishingPlayerInfo.parseResponseBeanBuilder(roomType.getRoomId()));
                            }
                        }
                        //End parse
                        this.sendToPlayer(fishingPlayer.getId(), responseBean.build().toByteString(), JOIN_TABLE_RESPONSE_BEAN);
                        //Send fishingPlayer info to other
                        otherJoinResponseBean.setPlayerInfo(fishingPlayer.parseResponseBeanBuilder(roomType.getRoomId()));
                        this.sendToOtherPlayer(fishingPlayer.getId(), otherJoinResponseBean.build().toByteString(), OTHER_JOIN_TABLE_RESPONSE_BEAN);
//                      logger.info(String.format("[playerJoin] - fishingPlayer[%s] has join to[%s]", fishingPlayer.getId(), this.getId()));
                        joinSuccess = true;

                        //Nếu là bot thì add vào list bot của bàn
                        if (fishingPlayer.isBot()) {
                            listBot.add((Bot)fishingPlayer);
                        }

                        //log start session
                        fishingPlayer.startSession(fishingPlayer.getId(), this.getId(), fishShooting.getGameState().getStateType().name(),
                                fishingPlayer.getMoneyByType(roomType.getMoneyType()), ZonedDateTime.now().toEpochSecond(), roomType.getMoneyType());

                        break;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Join table error - ", ex);
        } finally {
            lock.unlock();
            if (joinSuccess && !fishingPlayer.isBot())
            {
            }
            return joinSuccess;
        }
    }

    /**
     * FishingPlayer leave this table
     * @param fishingPlayer instance of fishingPlayer
     */
    void playerLeave(FishingPlayer fishingPlayer){
        try{
            boolean verifyLeave = true;
            if (!fishingPlayer.isBot() && fishingPlayer.getCtx() != null) {
                verifyLeave = fishingPlayer.getCtx().checkAuth();
            }
            if (verifyLeave){
                LeaveTableResponseBean.Builder responseBean = LeaveTableResponseBean.newBuilder();
                for (Map.Entry<Byte, FishingPlayer> playerEntry : hashSlot.entrySet()){
                    if (playerEntry.getValue() != null && playerEntry.getValue().getId() == fishingPlayer.getId()) {

                        //Nếu không xóa được hết đạn của thằng player thì báo lỗi
                        if (!getFishShooting().removeBulletPlayerLeave(fishingPlayer.getId()))
                            logger.error("[playerLeave] - Không thể xóa hết đạn của Player["+fishingPlayer.getId()+"] trong Table["+getId()+"]");
                        //Slot chứa thằng player gán về free_player
                        this.hashSlot.put(playerEntry.getKey(), FREE_PLAYER);
                        //
                        freeSlot++;
                        checkTableState();
                        checkRateByFreeSlot();
                        //
                        fishingPlayer.leaveTable();
                        if (fishingPlayer.isHost()) {
                            fishingPlayer.setHost(false);
                            this.host = null;

                            //collect bullet of bot
                            for (Bot bot : listBot) {
                                if (bot != null && bot.isPlaying()) {
                                    if(!getFishShooting().removeBulletPlayerLeave(bot.getId())) {
                                        logger.error("[playerLeave] - Không thể xóa hết đạn của Bot["+bot.getId()+"] trong Table["+getId()+"]");
                                    } else {
                                        logger.info("Remove all bullet of bot - " + bot.getId());
                                    }
                                }
                            }

                        }

                        //nếu là bot thì chỉ cần xóa trong list bot. ko cần xóa ở fish manager
                        if (fishingPlayer.isBot()) {
                            listBot.remove(fishingPlayer);
                        } else {
                            fishingPlayerManager.removePlayer(fishingPlayer.getId());
                        }

                        break;
                    }
                }
                //if fishingPlayer leave success (send to other player)
                if (!fishingPlayer.isPlaying()){ // && fishingPlayerManager.getPlayer(fishingPlayer.getId()) == null
                    //save money for user nếu ko phải là bot
                    try {
                        if (!isTrial() && !fishingPlayer.isBot()){
                            fishingPlayer.saveUserMoney();
                        }
//                        logger.info("Save money for " + fishingPlayer.getUserName() + " - money = " + fishingPlayer.getMoneyBag().get(MoneyType.GOLD));
                        responseBean.setErrorCode(REC_SUCCESS);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        logger.error("[playerLeave] - Can't save money of Player["+fishingPlayer.getId()+"] to mongo!", e);
                    }

                    //Send to Other
                    OtherLeaveTableResponseBean.Builder otherPlayerResponseBean = OtherLeaveTableResponseBean.newBuilder();
                    otherPlayerResponseBean.setPlayerId(fishingPlayer.getId());
                    this.sendToOtherPlayer(fishingPlayer.getId(), otherPlayerResponseBean.build().toByteString(), OTHER_PLAYER_LEAVE_TABLE_RESPONSE_BEAN);
                } else {
                    responseBean.setErrorCode(REC_NOT_DEFINE);
                }
                //Send to self-fishingPlayer
                if (!fishingPlayer.isBot()) {
                    //end session
                    fishingPlayer.endSession(fishingPlayer.getMoneyByType(roomType.getMoneyType()), ZonedDateTime.now().toEpochSecond());
                    this.sendToCtx(fishingPlayer.getCtx(), responseBean.build().toByteString(), PLAYER_LEAVE_TABLE_RESPONSE_BEAN);
                }

            }
        } catch (Exception ex) {
            logger.error("Player[" + fishingPlayer.getId() + "] Leave table[" + getId() + "] error", ex);
        } finally {
            if (!fishingPlayer.isBot() && host == null) {
                //change host
                this.changeHost();

            }
        }
    }

    void changeGun(ChangeGunRequestBean requestBean, ChannelContextInfo playerCtx)
    {
        int playerId = requestBean.getPlayerId();

        FishingPlayer fishingPlayer = fishingPlayerManager.getPlayer(playerId);
        if (fishingPlayer == null && playerId != playerCtx.getUserId()) {
            fishingPlayer = getBotById(playerId);
        }
        Gun gun = GunManager.getInstance().getGun(requestBean.getGunId());
        ChangeGunResponseBean.Builder responseBean = ChangeGunResponseBean.newBuilder();
        try{
            if (fishingPlayer != null && gun != null){
                if (fishingPlayer.getCountBulletFree() > 0) {
                    responseBean.setErrorCode(REC_NOT_DEFINE);//khong the doi sung khi dang duoc free
                } else {
                    int gunMulCur = fishingPlayer.getGun().getMultiple();
                    long moneyAmount = gun.getMultiple() * roomType.getBaseGunMoney();
                    Money minMoney = createMoneyByRoomType(moneyAmount);
                    if (fishingPlayer.getMoneyBag().isEnoughMoney(minMoney) || gunMulCur >= gun.getMultiple()){
                        fishingPlayer.setGun(gun);
                        //Send to client
                        responseBean.setPlayerId(fishingPlayer.getId());
                        responseBean.setGunId(gun.getId());
                        sendToOtherPlayer(fishingPlayer.getId(), responseBean.build().toByteString(), OTHER_PLAYER_CHANGE_GUN_RESPONSE_BEAN);
                        responseBean.setErrorCode(REC_SUCCESS);
                    } else {
                        responseBean.setErrorCode(REC_CHANGE_GUN_NOT_ENOUGH_MONEY);
                    }
                }

            } else {
                if (fishingPlayer == null) responseBean.setErrorCode(REC_NOT_DEFINE);
                if (gun == null) responseBean.setErrorCode(REC_WRONG_GUN_ID);
            }
        } catch (Exception ex){
            logger.error("[changeGun] - Player[" + playerCtx.getUserId() + "] try change to Gun[" + requestBean.getGunId() + "] ERROR!", ex);
            responseBean.setErrorCode(REC_NOT_DEFINE);
        }
        if (fishingPlayer != null && !fishingPlayer.isBot()) {
            sendToCtx(playerCtx, responseBean.build().toByteString(), PLAYER_CHANGE_GUN_RESPONSE_BEAN);
        }

    }

    private Money createMoneyByRoomType(long amount){
        switch (roomType.getRoomId()){
            case RoomType.TRIAL:
                return new Money(amount, MoneyType.TRIAL);
            default:
                return new Money(amount, MoneyType.GOLD);
        }
    }

    public void requestFireBullet(FishShootingProtos.BulletRequestBean bulletRequestBean) {
        try {
            int playerId = bulletRequestBean.getPlayerId();
            FishingPlayer fishingPlayer = fishingPlayerManager.getPlayer(playerId);
            if (fishingPlayer == null) {
                fishingPlayer = this.getPlayerInSlot(playerId);
            }
            if (fishingPlayer != null) {
                boolean isBulletSuper = false;

                //chi co game 1 moi co super gun
                if (roomType.getRoomId() == RoomType.NORMAL)
                {
                    int result = fishingPlayer.fireBullet();

                    if (result == 1) {
                        isBulletSuper = true;
                    }
                    if (result == 0) {
                        sendToAllOtherPlayerToSuperGun(playerId, true);
                    }
                    if (result == -2) {
                        sendToAllOtherPlayerToSuperGun(playerId, false);
                    }
                }

                generateBullet(bulletRequestBean, fishingPlayer, isBulletSuper);
            }
        }catch (Exception e) {
            logger.error("requestFireBullet error", e);
        }
    }

    private void sendToAllOtherPlayerToSuperGun(int playerId, boolean isSuper) {
        OtherPlayerChangeSuperGun.Builder response = OtherPlayerChangeSuperGun.newBuilder();
        response.setPlayerId(playerId);
        response.setIsSuper(isSuper);
        sendToAll(response.build().toByteString(), OTHER_PLAYER_SUPER_GUN);
    }

    /*
    * Khi 1 thang ban duoc du so' trong game PLT va duoc free bullet
    * */
    public void sendToAllOtherPlayerToFreeBullet(int playerId, int countBullet) {
        OtherPlayerGetFreeBullet.Builder response = OtherPlayerGetFreeBullet.newBuilder();
        response.setPlayerId(playerId);
        response.setCountBulletFree(countBullet);
        sendToAll(response.build().toByteString(), OTHER_PLAYER_FREE_BULLET);
    }

    /**
     * Generate bullet when receive request from client
     * @param bulletRequestBean proto Class
     */
    void generateBullet(FishShootingProtos.BulletRequestBean bulletRequestBean, FishingPlayer fishingPlayer, boolean isSuperBullet)
    {
        lock.lock();
        try
        {
            if (fishingPlayer.isPlaying())
            {
                int gunId = fishingPlayer.getGun().getId();
                Long subAmount = (long) (fishingPlayer.getGun().getMultiple() * roomType.getBaseGunMoney());
                Money subMoney = createMoneyByRoomType(subAmount);
                BulletResponseBean.Builder response = BulletResponseBean.newBuilder();

                //so luong vien dan free ma no co
                int countBulletFree = fishingPlayer.getCountBulletFree();

                boolean isBulletFree = false;
                //Nếu player đủ tiền
                long moneyBefore = fishingPlayer.getMoneyBag().get(subMoney.getMoneyType()).getAmount();
                if (fishingPlayer.getMoneyBag().isEnoughMoney(subMoney) || countBulletFree > 0)
                {
                    if (countBulletFree > 0)
                    {
                        //khong can tru tien no
                        fishingPlayer.subBulletFree();
                        isBulletFree = true;
                    }
                    else
                    {
                        moneyService.subMoney(subMoney, fishingPlayer, true, false);
                        if (!isTrial() && !fishingPlayer.isBot()) {
                            tempFund += subAmount;
                        }
                        isBulletFree = false;
                    }


                    Bullet bullet = null;
                    try
                    {
                        bullet = fishShooting.generateBullet(fishingPlayer.getId(), bulletRequestBean.getPosX(), bulletRequestBean.getPosY(), bulletRequestBean.getAngle(), gunId);
                    }
                    catch (Exception ex)
                    {
                        logger.error("[generateBullet] - error when generate bullet of Player["+fishingPlayer.getId()+"], bulletTempId["+bulletRequestBean.getBulletTempId()+"]");

                        if(isBulletFree)
                        {
                            fishingPlayer.refundBulletFree();
                        }
                        else
                        {
                            fishingPlayer.SetMoney(subMoney.getAmount());
                            moneyService.addMoneyN(subMoney, fishingPlayer, "generateBulletError");
                            if (!isTrial() && !fishingPlayer.isBot()) {
                                tempFund -= subAmount;
                            }
                        }


                        response.setErrorCode(REC_NOT_DEFINE);
                    }
                    //Tạo thành công -> gửi cho cả bàn (kể cả nó)
                    if (bullet != null){
                        bullet.setSuperBUllet(isSuperBullet);
                        if (!isTrial()  && !fishingPlayer.isBot()) {

                            //log fire bullet
                            fishingPlayer.fireBullet(bullet.getId(), subAmount);
                            //logKafkaService.logAction(fishingPlayer.getId(), UserAction.FIRE_BULLET, moneyBefore, subAmount, subMoney.getMoneyType(), "FB;table=" + this.getId() + ";bullet=" + bullet.getId());
                        }
                        sendToAll(bullet.parseResponseBeanBuilder().setErrorCode(REC_SUCCESS).setBulletTempId(bulletRequestBean.getBulletTempId()).build().toByteString(), NEW_BULLET_RESPONSE_BEAN);
                    }
                } else {
                    //Nếu không đủ tiền
                    response.setBulletTempId(bulletRequestBean.getBulletTempId())
                            .setErrorCode(REC_GEN_BULLET_NOT_ENOUGH_MONEY);

                    //Bot không đủ thì thì cho out
                    if (fishingPlayer.isBot()) {
                        GameRoom gameRoom = GameLobby.getInstance().getRoomByTableId(fishingPlayer.getPlayingInTable());
                        if (gameRoom != null){
                            gameRoom.leaveTable(fishingPlayer);
                        }
                    } else {
                        //Chỉ gửi cho nó
                        sendToPlayer(fishingPlayer.getId(), response.build().toByteString(), NEW_BULLET_RESPONSE_BEAN);
                    }
                }

            } else {
                //Lỗi nó (chưa join bàn mà đòi bắn - chống fake)
                logger.error("[generateBullet] - Player["+fishingPlayer.getId()+"] not exists in Table["+getId()+"] ");
                if (!fishingPlayer.isBot()) {
                    fishingPlayer.getCtx().disconnect(REC_NOT_DEFINE);
                }

            }
        } catch (Exception ex) {
            logger.error("Fire bullet error", ex);
        } finally {
            lock.unlock();
        }

    }

    private void changeHost() {
        try {
            if (host == null) {
                for (Map.Entry<Byte, FishingPlayer> playerEntry : hashSlot.entrySet()){
                    FishingPlayer fishingPlayer = playerEntry.getValue();
                    if (fishingPlayer != null && fishingPlayer.getId() != 0 && !fishingPlayer.isBot()) {
                        host = fishingPlayer;
                        fishingPlayer.setHost(true);
                        sendToPlayer(fishingPlayer.getId(), null, CHANGE_HOST_RESPONSE_BEAN);
                        return;

                    }
                }
            }
        } catch (Exception e) {
            logger.error("Change host error", e);
        }

    }

    public void sendJackPotForAll() {
        try {
            if (freeSlot == NUMBER_OF_PLAYER)
                return;
            JackPot.Builder jackPot = JackPot.newBuilder();
            jackPot.setJackPot(fundManager.getJackPot(roomType));
            //logger.info("sendJackPotForAll - jackpot = " + jackPot.getJackPot());
            sendToAll(jackPot.build().toByteString(), JACK_POT_RESPONSE_BEAN);
        } catch (Exception e) {
            logger.error("sendJackPotForAll error - " + roomType.getRoomName(), e);
        }
    }

    long recieveJackpot(RoomType roomType,int gunMultiple)
    {
        return fundManager.recieveJackPot(roomType, gunMultiple);
    }


    /**
     * handler when client send request hit fish, calculate rate fish die and send to client if fish die
     * @param requestBean client request
     */
    void hitFish(HitFishRequestBean requestBean, int playerId)
    {
        lock.lock();
        try
        {
            FishingPlayer fishingPlayer = fishingPlayerManager.getPlayer(playerId);
            if (fishingPlayer == null) {
                fishingPlayer = this.getPlayerInSlot(playerId);
            }
            if (fishingPlayer != null){

                //Hit fish
                //logKafkaService.logAction(fishingPlayer.getId(), UserAction.HIT_FISH, 0, 0, MoneyType.GOLD, "Hit fish " + requestBean.getFishId() + " at table id = " + this.getId() + " - room type = " + this.roomType.getRoomName());
                List<FishHasBeenHoleResponseBean.Builder> listFishHole = new ArrayList<>();
                FishHasBeenKillResponseBean.Builder responseBean = FishHasBeenKillResponseBean.newBuilder();
                responseBean = fishShooting.hitFish(fishingPlayer, requestBean.getTimer(), requestBean.getFishId(), requestBean.getBulletId(), roomType, listFishHole);
                long moneyPlus = responseBean.getReceiveMoney();

                responseBean.setFishId(requestBean.getFishId());
                responseBean.setTimer(requestBean.getTimer());
                responseBean.setErrorCode(REC_HIT_FISH_NOT_FOUND);
                if (moneyPlus == -1){
//                    logger.error(String.format("( Table[%s] - Player[%s] ) can't found Fish[%s] or Bullet[%s] ", getId(), playerId, requestBean.getFishId(), requestBean.getBulletId()));
                    responseBean.setErrorCode(REC_HIT_FISH_NOT_FOUND);
                    sendToPlayer(fishingPlayer.getId(), responseBean.build().toByteString(), FISH_HAS_BEEN_KILL_RESPONSE_BEAN);
                }
                if (moneyPlus > 0)
                {
                    responseBean.setErrorCode(REC_SUCCESS);
                    responseBean.setFishId(requestBean.getFishId());
                    responseBean.setPlayerId(playerId);
                    responseBean.setReceiveMoney((int)moneyPlus);
                    responseBean.setTimer(requestBean.getTimer());

                    if (listFishHole != null && listFishHole.size() > 0)
                    {
                        for (FishHasBeenHoleResponseBean.Builder fishHoleBuiler : listFishHole)
                        {
                            responseBean.addListFishHole(fishHoleBuiler);
                            moneyPlus += fishHoleBuiler.getReceiveMoney();
                        }

                    }
                    Money addMoney = createMoneyByRoomType(moneyPlus);//new Money(moneyPlus, MoneyType.GOLD);

                    User newUser = fishingPlayer.getUser();

                    if (!fishingPlayer.isBot())
                    {
                        long winGold = addMoney.getAmount();
                        newUser.SetMoney(winGold);
                        moneyService.addMoneyN(addMoney, newUser, "Game_Ca");
                    }
                    else
                        moneyService.addMoneyN(addMoney, newUser);


                    if (!isTrial() && !fishingPlayer.isBot())
                    {
                        fishingPlayer.addMoneyWin(addMoney);
                        if (!responseBean.getIsJackpot())
                        {
                            if(!fishingPlayer.isBot())
                                fundManager.subMoney(moneyPlus, roomType);
                        } else {
                            logger.info("Kill fish but is jackpot - khong subFund - " + responseBean.getReceiveMoney());
                        }

                        //logKafkaService.logAction(fishingPlayer.getId(), UserAction.KILL_FISH, moneyBefore, moneyPlus, addMoney.getMoneyType(), "KF;" + requestBean.getFishId() + ";table=" + this.getId() + ";bullet=" + requestBean.getBulletId());
                    }

                    sendToAll(responseBean.build().toByteString(), FISH_HAS_BEEN_KILL_RESPONSE_BEAN);

                }

            }
        } catch (Exception ex) {
            logger.error("Hit fish error", ex);
        } finally {
            lock.unlock();
        }

    }

    /*
    * check bot => neu het tien thi cho out. Neu it qua thi cho them vao. randome out choi thoi
    * */
    public void checkBot() {
        if (freeSlot == NUMBER_OF_PLAYER) { //khong co ai trong phong
            return;
        }
        CRandom random = new CRandom();
        //can 100 gold de choi
        Money moneyNeed = new Money(100, roomType.getMoneyType());
        //Money moneyNeed = createMoneyByRoomType(this.roomType.getBaseGunMoney());
        for (Bot bot : listBot) {
            if (!bot.getMoneyBag().isEnoughMoney(moneyNeed)) {
                //bot khong du tien.
                //cho no leave
                if (random.randByPercent(70)) {
                    GameRoom gameRoom = GameLobby.getInstance().getRoomByTableId(this.id);
                    if (gameRoom != null){
                        gameRoom.leaveTable(bot);
                    }
                }
            }
        }

        //bàn full
        if (listBot.size() > 1) {
            if (random.randByPercent(5 * (NUMBER_OF_PLAYER - freeSlot))) {
                int botIndex = random.randInt(0, listBot.size() - 1);
                Bot bot = listBot.get(botIndex);
                GameRoom gameRoom = GameLobby.getInstance().getRoomByTableId(this.id);
                if (gameRoom != null){
                    gameRoom.leaveTable(bot);
                }
            }
        }

        if (freeSlot > NUMBER_OF_PLAYER - MAX_PLAYER_WITH_BOT) {
            if (random.randByPercent(10 * freeSlot)) {
                GameRoom gameRoom = GameLobby.getInstance().getRoomByTableId(this.id);
                gameRoom.randomBotJoinTable();
            }

        }

    }

    /**
     * set table isFull if free tx == 0
     */
    private void checkTableState(){
        if (this.freeSlot == 0){
            this.setFull(true);
        } else {
            this.setFull(false);
        }
        if (this.freeSlot == NUMBER_OF_PLAYER){
            this.setFree(true);
        } else {
            this.setFree(false);
        }
    }

    void startNewSession(String nameSession) {
        if (!isFree() && !isTrial()){
            try{
                for (FishingPlayer fishingPlayer : hashSlot.values()){
                    if (fishingPlayer != null && fishingPlayer.getId() != 0 && !fishingPlayer.isBot()){
                        fishingPlayer.resetLog();
                        fishingPlayer.startSession(fishingPlayer.getId(), this.getId(), nameSession,
                                fishingPlayer.getMoneyByType(roomType.getMoneyType()), ZonedDateTime.now().toEpochSecond(), roomType.getMoneyType());
                    }
                }
            } catch (Exception ex){
                logger.error("[logUserAction] - error", ex);
            }
        }
    }

    void logUserAction() {
        if (!isFree() && !isTrial()){
            try{
                for (FishingPlayer fishingPlayer : hashSlot.values()){
                    if (fishingPlayer != null && fishingPlayer.getId() != 0 && !fishingPlayer.isBot()){
                        fishingPlayer.endSession(fishingPlayer.getMoneyByType(roomType.getMoneyType()), ZonedDateTime.now().toEpochSecond());
                    }
                }
            } catch (Exception ex){
                logger.error("[logUserAction] - error", ex);
            }
        }
    }

    void saveMoneyAllPlayer(){
        if (!isFree() && !isTrial()){
            try{
                for (FishingPlayer fishingPlayer : hashSlot.values()){
                    if (fishingPlayer != null && fishingPlayer.getId() != 0 && !fishingPlayer.isBot()){
                        try {
                            fishingPlayer.saveUserMoney();
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                            logger.error(e.getMessage());
                        }
                    }
                }
            } catch (Exception ex){
                logger.error("[saveMoneyAllPlayer] - can't save playerMoney to Mongo Table["+getId()+"]: ", ex);
                logger.error("[saveMoneyAllPlayer] Player of Table[" + this.getId() + "] Disconnect All");
                disconnectAll("Error when save money all");
            }
        }
    }

    void saveTempFund(){
        try{
            if (!isTrial()) {
                if (tempFund > 0){
                    fundManager.addMoney(tempFund, roomType);
                    tempFund = 0;
                }
            }

        } catch (Exception ex){
            logger.error("[saveTempFund] - can't save TempFund of Table["+getId()+"]: ", ex);
            logger.info("[saveTempFund] Table[" + this.getId() + "] got error when saveTempFund Disconnect All");
            disconnectAll("Error when Save tempfund");
        }
    }

    void disconnectAll(String errorDesciption){
        logger.info("Disconnect all has error - " + errorDesciption);
        disconnectAll(REC_NOT_DEFINE);
    }

    private void disconnectAll(int errorCode){
        for (FishingPlayer fishingPlayer : hashSlot.values()){
            if (fishingPlayer.isBot()) {
                //nếu là bot thì cho leave
                GameRoom gameRoom = GameLobby.getInstance().getRoomByTableId(fishingPlayer.getPlayingInTable());
                if (gameRoom != null){
                    gameRoom.leaveTable(fishingPlayer);
                }
            } else {
                if (fishingPlayer != null && fishingPlayer.getId() != 0) {
                    fishingPlayer.getCtx().disconnect(errorCode);
                }
            }
        }
    }


    private boolean isFreeSlot(byte slotId){
        return hashSlot.get(slotId).getId() == 0;
    }

    public boolean isFull() {
        return isFull;
    }

    public boolean isFree(){
        return isFree;
    }


    private void setFull(boolean full) {
        isFull = full;
    }


    public void setFree(boolean free) {
        isFree = free;
    }

    public FishingPlayer getPlayerBySlot(byte slot){
        return hashSlot.get(slot);
    }

    public ConcurrentHashMap<Byte, FishingPlayer> getHashSlot() {
        return hashSlot;
    }

    public int getId() {
        return id;
    }

    byte getFreeSlot() {
        return freeSlot;
    }

    public RoomType getRoomType() { return  roomType; }

    private boolean isTrial(){
        return roomType.getRoomId() == RoomType.TRIAL;
    }

    @Override
    public String toString() {
        return "Table[" + this.id + "] of Room[" + roomType.getRoomName() + "]";
    }

    public static void main(String[] args) throws InterruptedException
    {
        GameTable gameTable = new GameTable(100, DataManager.getInstance().getDataRoomType().iterator().next());
        System.out.println(gameTable.hashSlot);
        FishingPlayer fishingPlayer = new FishingPlayer(10001);
        FishingPlayer fishingPlayer2 = new FishingPlayer(10002);
        new Thread(() -> gameTable.playerJoin(fishingPlayer)).start();
        new Thread(() -> gameTable.playerJoin(fishingPlayer2)).start();
        Thread.sleep(100);
        System.out.println(gameTable.hashSlot);
    }
}
