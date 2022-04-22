package games.shootingfish;

import com.google.protobuf.ByteString;
import games.core.application.channel.TaskScheduler;
import games.features.pushmessage.PushMessageService;
import games.shootingfish.constant.ResponseBeanFishingConst;
import games.shootingfish.data.DataManager;
import games.shootingfish.datatype.*;
import games.shootingfish.manager.GunManager;
import games.shootingfish.object.Bullet;
import games.shootingfish.object.Fish;
import games.shootingfish.object.FishingPlayer;
import games.shootingfish.object.Gun;
import games.shootingfish.pool.PoolBullet;
import libs.util.CRandom;
import libs.util.LogFactory;
import message.FishShootingProtos;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by WINDNCC on 7/5/2017.
 */
public class FishShooting implements ResponseBeanFishingConst
{
    private static Logger logger = LogFactory.getLogger(FishShooting.class.getSimpleName());
    /**
     * Reference to GameTable hold this game
     */
    private GameTable gameTable;

    /**
     * count time game running
     */
    private double totalTimePlaying = 0.0f;

    /**
     * count time state running
     */
    private float stateTimePlaying = 0.0f;

    /**
     * System mili second of last updateDB
     */
    private long lastTimeUpdate = 0;

    /**
     * count every 1 sec - for one sec loop
     */
    private int oneSecCount = 0;

    /**
     * count 1 every 5 sec - for five sec loop
     */
    private int fiveSecCount = 0;

    /**
     * count 1 every 2 sec - for two sec loop
     */
    private int twoSecCount = 0;

    /**
     * Hold timestamp when state start
     */
    private long timeStampStateStart;

    private static CRandom cRandom = new CRandom();

    /**
     * New instance poolBullet for this game
     */
    private PoolBullet poolBullet;

    /**
     * List fish will be render in current state
     */
    private TimingSpawnFish timingSpawnFish;

    /**
     * List fish send to client every five second
     */
    private TimingSpawnFish timingSpawnFishByTimer; //ListFish send to client every five second

    /**
     * List fish send to client at join game
     */
    private TimingSpawnFish timingSpawnFishAtJoin; //ListFish send to client at join

    /**
     * List bullet has fire by player
     */
    private ConcurrentHashMap<Integer, Bullet> hashActiveBullet; //List bullet has fire

    /**
     * KeySet of timingSpawnFish (Hold all element during state)
     */
    private Set<Integer> timingSpawnFishKeySet;

    /**
     * KeySet of timingSpawnFish (will remove element when this timer has been sent to client)
     */
    private Set<Integer> timingSpawnFishKeySetTimer;

    /**
     * hold current state of game
     */
    private GameState gameState;
    /**
     *
     */
    private ScheduledFuture<?> oneSecUpdate;

    private final int TIME_UPDATE_MILISECOND = 500;


    private final int BOSS_FISH_TYPE_ID = 19;
    public static void main(String[] args) {
//        FishShooting game = new FishShooting("a", 1);
//        System.out.println(game.getTimingSpawnFishAtJoin(50));
    }

    FishShooting(GameTable gameTable){

        this.gameTable = gameTable;
        gameState = new GameState(this.gameTable.getRoomType().getRoomId());
        initGame();
//        logger.info("Constructor fish shooting - " + gameTable.getId());
    }
    public void start()
    {
//        logger.info("Start fish shooting - " + gameTable.getId() + " state time: " + gameState.getTimeDuring());
        initState();
        lastTimeUpdate = System.currentTimeMillis();
        oneSecUpdate = TaskScheduler.getInstance().scheduleAtFixedRate(new MainUpdateRunnable(), 0, TIME_UPDATE_MILISECOND, TimeUnit.MILLISECONDS);

    }
    public void stop(){
        oneSecUpdate.cancel(true);
        oneSecUpdate = null;
        clear();

    }
    private void clear(){
        timingSpawnFish.clear();
        timingSpawnFishByTimer.clear();
        timingSpawnFishAtJoin.clear();
        hashActiveBullet.clear();
        timingSpawnFishKeySetTimer.clear();
        timingSpawnFishKeySet.clear();
    }
    private void initGame(){
        poolBullet = new PoolBullet();
        timingSpawnFish = new TimingSpawnFish(1000, this);
        timingSpawnFishByTimer = new TimingSpawnFish();
        timingSpawnFishAtJoin = new TimingSpawnFish();
        hashActiveBullet = new ConcurrentHashMap<>(50);
        timingSpawnFishKeySetTimer = new HashSet<>(50);
    }

    /**
     * init some field when nextState
     */
    private void initState(){
        try {
            timingSpawnFish.getByState(gameState, gameTable.getRoomType());
            //Hold element during state
            timingSpawnFishKeySet = timingSpawnFish.keySet();
            //Remove element when this timer has been sent
            timingSpawnFishKeySetTimer.clear();
            timingSpawnFishKeySetTimer.addAll(timingSpawnFish.keySet());
            stateTimePlaying = 0.0f;
            oneSecCount = 0;
            fiveSecCount = 0;
            twoSecCount = 0;
            timeStampStateStart = System.currentTimeMillis() - 5;

            //start new sesssion - log
            gameTable.startNewSession(gameState.getStateType().name());
        } catch (InterruptedException e) {
            logger.error("[initState in FishShooting] - Can't get spawn fish->game crash", e);
            gameTable.saveMoneyAllPlayer();
            gameTable.disconnectAll("Error when init state - " + e.toString());
        }
    }

    public GameTable getGameTable() {
        return gameTable;
    }

    //game hien tai dang choi thuoc game nao
    public RoomType getRoomType() {
        return gameTable.getRoomType();
    }
    /**
     * call when end time of state, to next state of game
     */
    private void nextState(){
        try {
            gameTable.saveMoneyAllPlayer();
            gameTable.logUserAction();
            gameState.nextState();
            initState();
            gameTable.sendToAll(getTimingSpawnFishAtJoin().parseResponseBeanBuilder()
                            .setCurrentState(gameState.getId())
                            .setCurrentStateTime((float)getStateTimePlaying())
                            .setTimestampStartState(getTimeStampStateStart())
                            .setStateDuringTime(gameState.getTimeDuring())
                            .setBgId(gameState.getBgId())
                            .build().toByteString()
                    , ResponseBeanFishingConst.TIMING_SPAWN_FISH_NEXT_STATE_RESPONSE_BEAN);
            logger.info("next state " + gameTable.getId() + " " + gameState.getStateType() + " " + gameTable.getId());
        } catch (Exception e) {
            logger.error("nextState", e);
        }

    }

    /**
     *
     * @param timer timer hold fish want get
     * @param fishId fish id want get
     * @return Fish
     */
    private Fish getFishByTimerAndId(int timer, int fishId){
        HashFish hashFish = timingSpawnFish.get(timer);
        if (hashFish != null){
            return hashFish.get(fishId);
        }
        return null;
    }

    /**
     *
     * @param timer timer hold fish in timing spawn fish
     * @param fishId id of fish
     * @param bulletId id of bullet
     * @return plusMoney if fish has been kill else return 0
     */
    FishShootingProtos.FishHasBeenKillResponseBean.Builder hitFish(FishingPlayer fishingPlayer, int timer, int fishId, int bulletId, RoomType roomType, List<FishShootingProtos.FishHasBeenHoleResponseBean.Builder> listFishHole)
    {
        FishShootingProtos.FishHasBeenKillResponseBean.Builder responseBean = FishShootingProtos.FishHasBeenKillResponseBean.newBuilder();

        Fish fish = getFishByTimerAndId(timer, fishId);

        //logger.info("Hit fish - bullet id = " + bulletId);

        Bullet bullet = hashActiveBullet.getOrDefault(bulletId, null);
        if (bullet == null && bulletId != 10000)
        {
            logger.error("HIt fish - bullet null - bullet id = " + bulletId + " - active bullet = " + hashActiveBullet.size());
        }
        long moneyReward = 0;
        //pushBulletBackToPool(bulletId);
        if (fish != null && bullet != null)
        {
            responseBean.setTypeEffectPLT(0);//khong co effect
            responseBean.setTypeShadow(fish.getTypeShadow());
            float doubleMoney = 1.0f;

            if (bullet.isSuperBUllet()) {
                doubleMoney = GunManager.MUL_GOLD_SUPER_GUN;
            }
            responseBean.setIsDouble(bullet.isSuperBUllet());

            //ty le kill fish tinh doi voi bot
            float otherRateKillFish = 1.0f;
            if (fishingPlayer.isBot())
            {
                otherRateKillFish = TimingSpawnFish.RATE_BOT_KILL_FISH;
            }

            float rateFish = fish.getRate();
            float rateKillFish = roomType.getRateKillFish();
            float rateKillFishByPlayer = gameTable.getRateKillFishByPlayerInRoom();

            int rate = (int) (rateFish * rateKillFish * 1000 * otherRateKillFish * rateKillFishByPlayer);

            int randInt = cRandom.randInt(1,3);

            if (randInt <= rate)
            {
                killFish(timer, fish);

                Gun gun = GunManager.getInstance().getGun(bullet.getGunId());
                if (gun != null)
                {
                    if (fish.getTypeId() == 23 || fish.getTypeId() == 120)
                    {
                        moneyReward = gameTable.recieveJackpot(roomType, gun.getMultiple());
                        responseBean.setIsJackpot(true);
                        logger.info("Player - " + fishingPlayer.getUserName() + " - jackpot  with fish type = " + fish.getTypeId() + " - with gun = " + gun.getMultiple() + " - recieve jackpot = " + moneyReward);
                        String gameName = "Mermaid Ocean";
                        switch (roomType.getRoomId()) {
                            case RoomType.NORMAL:
                                gameName = "Fu Lu Shou";
                                break;
                            case RoomType.VIP:
                                gameName = "Ocean Princess";
                                break;
                            case RoomType.SUPER_VIP:
                                gameName = "Mermaid Ocean";
                                break;
                        }
                        String content = "Jackpot with value " + moneyReward;
                        String nickName = fishingPlayer.getUserName();
                        if (fishingPlayer.isBot()) {
                            nickName = fishingPlayer.getNickName();
                        }
                        PushMessageService.getInstance().pushTextRunFromGame(fishingPlayer.getId(), nickName, content, gameName);
                    }
                    else
                    {
                        moneyReward = (long)(gun.getMultiple() * fish.getPrice() * roomType.getBaseGunMoney() * doubleMoney);
                        responseBean.setIsJackpot(false);
                    }

                }

                if (fish.getTypeShadow() > 0)
                {
                    List<Fish> listFishBeenHole = timingSpawnFish.getListGroupAtTime(timer);

                    for (Fish fishHole : listFishBeenHole)
                    {
                        if (fishHole.getId() != fish.getId())
                        {
                            FishShootingProtos.FishHasBeenHoleResponseBean.Builder builder = fishHole.parseFishHasBeenHoleBulder();
                            builder.setTimer(timer);

                            int moneyHole = gun.getMultiple() * fishHole.getPrice() * roomType.getBaseGunMoney();

                            builder.setReceiveMoney(moneyHole);
                            listFishHole.add(builder);
                        }

                    }
                }

                int typeEffectPlt = 0;//khong co effect
                if (roomType.getRoomId() == RoomType.VIP) {
                    //game thu 2, phuc loc tho
                    if (fish.getTypeId() == 117) {
                        //se rand dome effect tu 1 - 4
                        typeEffectPlt = cRandom.randInt(1, 4);

                        //add so
                        int bulletFree = fishingPlayer.addSo();
                        if (bulletFree > 0) {
                            gameTable.sendToAllOtherPlayerToFreeBullet(fishingPlayer.getId(), bulletFree);
                        }
                    }
                }
                responseBean.setTypeEffectPLT(typeEffectPlt);

                //log kill fish
                fishingPlayer.killFish(fish.getTypeId(), bulletId, moneyReward);
            }
            //}
            //long endhit = System.currentTimeMillis();
            //logger.info("Hit fish time = " + (endhit - startHit));
            pushBulletBackToPool(bulletId);

            int typeEffectLighting = 0;
            if (fish.getPrice() >= 7)
            {
                if (fish.getTypeId() != 12 && fish.getTypeId() < 20 ||
                        fish.getTypeId() >= 209 && fish.getTypeId() <= 213 ||
                        fish.getTypeId() >= 217 && fish.getTypeId() <= 221)
                {
                    //randome de ra effect lighting
                    boolean hasEffect = cRandom.randByPercent(70);
                    if (hasEffect) {
                        typeEffectLighting = cRandom.randInt(1,2);
                    }
                }
                //hao quang cua ca THINH
            }
            responseBean.setTypeEffectLigting(typeEffectLighting);


            //pushBulletBackToPool(bullet);

        }
        else
        {

            if(bulletId == 10000)
            {
                Gun vip = fishingPlayer.getGun();

                moneyReward = (long)(vip.getMultiple() * fish.getPrice() * roomType.getBaseGunMoney()); //tinh tien cho ca
                responseBean.setIsJackpot(false);
                fishingPlayer.killFish(fish.getTypeId(), bulletId, moneyReward);
            }
            else
            {
                pushBulletBackToPool(bulletId);
                moneyReward = -1;
            }

        }
        //logger.info("hit fish - money reward = " + moneyReward);
        if (moneyReward > 0 && fishingPlayer != null)
        {
            if(fishingPlayer.getCountBulletFree() > 0)
            {
                fishingPlayer.addMoneyBulletFree(moneyReward);
            }
        }

        responseBean.setReceiveMoney((int)moneyReward);
        return responseBean;
    }

    /**
     *  push fish back to pool and remove from timingSpawnFish
     * @param timer timer hold fish
     * @param fish instance of fish wanna kill
     */
    private void killFish(int timer, Fish fish){
        try {
            if (fish != null)
                if (!timingSpawnFish.removeFish(timer, fish))
                    logger.error("Can't kill Fish["+fish.getId()+"] in Timer["+timer+"] in Table["+gameTable.getId()+"]");
        } catch (Exception ex){
            logger.error("Kill fish error  Fish["+fish.getId()+"] in Timer["+timer+"] in Table["+gameTable.getId() + "] ", ex);
        }
    }

    //BULLET
    /**
     * get bullet from pool when receive request fire bullet from client
     * @param playerId player fire bullet
     * @param posX posX of bullet
     * @param posY posY of bullet
     * @param angle angle of bullet
     * @param gunId gun fire this bullet
     * @return Bullet
     */
    Bullet generateBullet(int playerId, float posX, float posY, float angle, int gunId){
        Bullet bullet = null;
        try {
            bullet = poolBullet.shiftBullet();
            bullet.setPlayerId(playerId);
            bullet.setPosX(posX);
            bullet.setPosY(posY);
            bullet.setAngle(angle);
            bullet.setGunId(gunId);
            hashActiveBullet.put(bullet.getId(), bullet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bullet;
    }

    boolean removeBulletPlayerLeave(int playerId){
        Collection<Bullet> bulletCollection = hashActiveBullet.values();
        boolean flagError = false; //Mặc định xóa thành công

        //code cũ của Cường
//        for (Bullet bullet : bulletCollection)
//            if (bullet.getPlayerId() == playerId)
//                if (!hashActiveBullet.remove(bullet.getId(), bullet))
//                    flagError = true; //Nếu 1 thằng xóa không được thì trả về fail

        //code Hoàng fix
        for (Bullet bullet : bulletCollection) {
            if (bullet.getPlayerId() == playerId) {
                pushBulletBackToPool(bullet);
            }
//                if (!hashActiveBullet.remove(bullet.getId(), bullet))
//                    flagError = true; //Nếu 1 thằng xóa không được thì trả về fail
        }

        return !flagError;
    }

    /**
     * push bullet back to pool when receive request hit fish from client
     * @param bulletId id of bullet
     */
    private void pushBulletBackToPool(int bulletId){
        //System.out.println("FishShooting.pushBulletBackToPool - hash active bullet - size = " + this.hashActiveBullet.size());
        Bullet inactiveBullet = this.hashActiveBullet.remove(bulletId);
        if (inactiveBullet != null){
            poolBullet.putBullet(inactiveBullet);
        } else {
            logger.error("FishShooting.pushBulletBackToPool - error. Hash active size = " + this.hashActiveBullet.size());
        }
    }

    /**
     *
     * @param bullet instance of bullet
     */
    private void pushBulletBackToPool(Bullet bullet){
        pushBulletBackToPool(bullet.getId());
    }

    /**
     * get timingSpawnFish to send to client by time range,  ex: (1,5) will return timer (1,3,4)
     * @param timerStart from time
     * @param timerEnd to time
     * @return TimingSpawnFish
     */
    private TimingSpawnFish getTimingSpawnFishByTimer(int timerStart, int timerEnd)
    {
        if (!timingSpawnFishByTimer.isEmpty()) timingSpawnFishByTimer.clear();

        for (int i = timerStart; i < timerEnd; i++)
        {
            if (timingSpawnFish.containsKey(i))
            {
                timingSpawnFishByTimer.put(i, timingSpawnFish.get(i));
                if (timingSpawnFishKeySetTimer.contains(i))
                {
                    timingSpawnFishKeySetTimer.remove(i);
                }
            }
        }
        return timingSpawnFishByTimer;
    }

    /**
     * get timingSpawnFish to send to client by timer, return timer before 50s
     * @param timer player join at ?
     * @return TimingSpawnFish
     */
    private TimingSpawnFish getTimingSpawnFishAtJoin(int timer){
        if (!timingSpawnFishAtJoin.isEmpty()) timingSpawnFishAtJoin.clear();
        int countLoop = 0;
        if (timer >= 50) countLoop = timer-50;
        for (int i = countLoop; i< (fiveSecCount+3)*5; i++){
            if (timingSpawnFishKeySet.contains(i)){
                timingSpawnFishAtJoin.put(i, timingSpawnFish.get(i));
            }
        }
        return timingSpawnFishAtJoin;
    }
    /**
     * get timingSpawnFish to send to client by timer, return timer before 50s
     * @return TimingSpawnFish
     */
    TimingSpawnFish getTimingSpawnFishAtJoin(){
        return getTimingSpawnFishAtJoin(oneSecCount);
    }
    class MainUpdateRunnable implements Runnable
    {
        @Override
        public void run() {
            Long now = System.currentTimeMillis();
            double deltaTime = (now - lastTimeUpdate) / 1000.0f;
            lastTimeUpdate = now;
            mainUpdate(deltaTime);
        }
    }

    /**
     *  DO other updateDB loop
     * @param deltaTime time between 2 updateDB
     */
    private void mainUpdate(double deltaTime){
        totalTimePlaying += deltaTime;
        stateTimePlaying += deltaTime;

//        long start;
//        long end;
//        long startMain = System.currentTimeMillis();
        if (stateTimePlaying >= oneSecCount){
            //start = System.currentTimeMillis();
            OneSecUpdate();
            oneSecCount++;
            //end = System.currentTimeMillis();
//            if (end - start > 20) {
//                logger.info("One sec update - " + (end - start) + " - current time = " + stateTimePlaying + " - state = " + gameState.getStateType().toString());
//            }
        }

        if (stateTimePlaying >= twoSecCount * 2){
            //start = System.currentTimeMillis();
            twoSecUpdate();
            twoSecCount++;
            //end = System.currentTimeMillis();
//            if (end - start > 20) {
//                logger.info("Two sec update - " + (end - start) + " - current time = " + stateTimePlaying + " - state = " + gameState.getStateType().toString());
//            }
        }

        if (stateTimePlaying >= fiveSecCount * 5){
            //start = System.currentTimeMillis();
            fiveSecUpdate();
            fiveSecCount++;
            //end = System.currentTimeMillis();
//            if (end - start > 20) {
//                logger.info("Five sec update - " + (end - start) + " - current time = " + stateTimePlaying + " - state = " + gameState.getStateType().toString());
//            }
        }
//        long endMain = System.currentTimeMillis();
//        if (deltaTime > 0.6f) {
//            logger.info("main update - " + (endMain - startMain) + " - delta time = " + deltaTime + " - state time = " + stateTimePlaying + " - table id - " + this.gameTable.getId());
//        }
//
//        if (endMain - startMain > 25) {
//            logger.info("main update - " + (endMain - startMain) + " - delta time = " + deltaTime);
//        }
    }


    //Update sau 1 giây
    private void OneSecUpdate(){
        //System.out.println(gameName + " - OneSecUpdate");
        //Kiem tra xem thoi gian cua state hien tai lon hơn hoặc bằng thời gian tồn tại của state thì next state
        if (oneSecCount >= gameState.getTimeDuring())
        {
            nextState();
        }
    }

    private void twoSecUpdate(){
        gameTable.saveTempFund();

        gameTable.sendJackPotForAll();
    }

    private void fiveSecUpdate(){
        int timeStartSend = 0;
        if (fiveSecCount != 0) timeStartSend = (fiveSecCount+1)*5;
        int timeEndSend = (fiveSecCount+2)*5;
        if (gameTable.getFreeSlot() < GameTable.NUMBER_OF_PLAYER){
            try{
                ByteString responseByteString = this.getTimingSpawnFishByTimer(timeStartSend, timeEndSend).parseResponseBeanBuilder().build().toByteString();
                gameTable.sendToAll(responseByteString, TIMING_SPAWN_FISH_RESPONSE_BEAN);
                //kiểm tra bot

                logger.trace("[fiveSecUpdate] - Send list fish to client");
            } catch (Exception e){
//                e.printStackTrace();
//                System.out.println(e.getMessage());
                logger.trace("loi k gui ca xuong cho client dc " + gameTable.getId(), e);
            }
        }
        this.gameTable.checkBot();

        //get list boss ra. Kiem tra thoi gian.
        List<Fish> listFishBoss = timingSpawnFish.getListFishBoss();

        if (listFishBoss != null && listFishBoss.size() > 0)
        {
            for (Fish fish : listFishBoss)
            {
                if (fish.getTypeId() == 22 && fish.getPrice() < 150 ||
                        fish.getTypeId() == 214 && fish.getPrice() < 200)
                {
                    int maxPrice = fish.getTypeId() == 22 ? 150 : 200;

                    if (oneSecCount > fish.getTimer() + 5 && oneSecCount < fish.getTimer() + 40)
                    {
                        boolean bossGrow = cRandom.randByPercent(20);

                        if (bossGrow)
                        {
                            int curPrice = fish.getPrice();
                            int newPrice = cRandom.randInt(curPrice + 10, curPrice + 40);

                            if (newPrice > maxPrice)
                            {
                                newPrice = maxPrice;
                            }

                            fish.setPrice(newPrice);
                            sendBossGrowUpToAll(fish);
                        }
                        //chi growup 1 con ca nay thoi
                        break;
                    }
                }
            }
        }
    }

    //gui su kien boss grow up cho tat ca moi nguoi
    private void sendBossGrowUpToAll(Fish fish) {
        FishShootingProtos.BossGrowUpResponseBean.Builder response = FishShootingProtos.BossGrowUpResponseBean.newBuilder();
        response.setFishId(fish.getId());
        response.setNewPrice(fish.getPrice());

        System.out.println("Send boss grown up - " + fish.getId() + " - " + fish.getTypeId() + " - " + fish.getTimer() + " - " + fish.getPrice());
        gameTable.sendToAll(response.build().toByteString(), BOSS_GROW_UP_RESPONSE_BEAN);
    }

    public void sendCurentMoney(Fish fish) {
        FishShootingProtos.MoneyCurrentResponseBean.Builder response = FishShootingProtos.MoneyCurrentResponseBean.newBuilder();
        response.setFishId(fish.getId());

        System.out.println("GET MONEY CURRENT: " + fish.getId() + " - " + fish.getTypeId() + " - " + fish.getTimer() + " - " + fish.getPrice());
        gameTable.sendToAll(response.build().toByteString(), GET_MONEY_BOSS_RESPONSE_BEAN);
    }


    TimingSpawnFish getTimingSpawnFish() {
        return timingSpawnFish;
    }


    GameState getGameState() {
        return gameState;
    }

    double getStateTimePlaying() {
        return stateTimePlaying;
    }

    long getTimeStampStateStart() {
        return timeStampStateStart;
    }

    @Override
    public String toString() {
        String f1 ="[State : %s] \n \t Total Time: %f \n \t State Time: %f";
        String f3 = "\n\ttimingSpawnFish: %s";
        return String.format(f1+f3, gameState.getStateType(), totalTimePlaying, stateTimePlaying, timingSpawnFish.toString());
    }
}
