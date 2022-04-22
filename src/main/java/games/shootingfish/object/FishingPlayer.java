package games.shootingfish.object;

import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyBag;
import games.core.common.money.MoneyType;
import games.core.common.money.TransactionStatus;
import games.core.database.MongoConst;
import games.core.user.IUser;
import games.core.user.User;
import games.core.user.UserService;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.datatype.TimingSpawnFish;
import games.shootingfish.manager.GunManager;
import libs.util.CRandom;
import message.FishShootingProtos.PlayerInfoBean;
import message.IResponseBean;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class FishingPlayer extends FishingPlayerLogGame implements IUser, IResponseBean<PlayerInfoBean.Builder>, MongoConst {

    private static UserService userService = UserService.getInstance();

    protected User user;

    /**
     * Slot in table = 0 if not in table
     */
    private byte slot;

    /**
     * Current gun of player (Default = 15)
     */
    private Gun gun;

    /**
     * id of table player playing
     */
    private int playingInTable;

    /**
     * =  true if playing game
     */
    private boolean isPlaying;

    /**
     * =  true he is bot
     */
    protected boolean isBot;

    /*
    *  = true: he is host
    * */
    private boolean isHost;

    /**
     * const default gun
     */
    private static final int DEFAULT_GUN = 1;

    //dat du 100 thi co super gun
    private float percentSuperGun = 0;

    private int countBulletSuper = 0;

    //so lan nhan duoc cai so' trong game phuc loc tho
    private int countSo = 0;


    private int countBulletFree = 0;

    //so tien ma no kiem duoc ban bullet free
    private long moneyBulletFree = 0;

    //thoi diem ma no duoc bullet Free
    private long timeStampBulletFree = 0; //milisecond

    /**
     *Static reference
     */
    private static GunManager gunManager = GunManager.getInstance();

    public FishingPlayer() {
        user = new User(0, 0);
    }

    public FishingPlayer(int id) {
        user = userService.getUser(id);
        defaultInit();
    }

    public User getUser() {
        return user;
    }

    //tra ve 1: neu ban super
    //return 0: chuyen sung thanh super
    //retun -1: ko co gi xay ra
    // return : -2: khong con super nua
    //con khong thi den % len de duoc super
    public int fireBullet() {
        if (countBulletSuper > 0) {
            countBulletSuper --;
            if (countBulletSuper <= 0) {
                return -2;
            }
            return 1;
        } else {
            percentSuperGun += GunManager.PERCENT_SUPER_ONE_BULLET;
            if (percentSuperGun >= 100) {
                percentSuperGun = 0;
                CRandom cRandom = new CRandom();
                countBulletSuper = cRandom.randInt(5, GunManager.COUNT_BULLET_SUPER);
                return 0;
            }
            return -1;
        }
    }

    //khi ban vien dan duoc free
    public void subBulletFree() {
        countBulletFree --;
    }

    //khi ban khong thanh cong
    public void refundBulletFree() {
        countBulletFree++;
    }

    //moi lan an duoc cai so ben phuc loc tho thi goi ham nay
    //count bullet ma lon thi goi ham change Free bullet
    public int addSo() {
        countSo++;
        if (countSo >= TimingSpawnFish.COUNT_SO_FREE_BULLET) {

            CRandom cRandom = new CRandom();
            int bulletPrice = gun.getMultiple();
            int maxBulletFree = GunManager.MAX_MONEY_FOR_FREE_BULLET / bulletPrice;
            int minBulletFree = GunManager.MIN_MONEY_FOR_FREE_BULLET / bulletPrice;
            countBulletFree += cRandom.randInt(minBulletFree, maxBulletFree);

            if (countBulletFree > 50) {
                countBulletFree = 50;
            }
            timeStampBulletFree = System.currentTimeMillis();
            System.out.println("Player " + getId() + " - add so - " + countSo + " - bullet Price = "
                    + bulletPrice + " - minBF = " + minBulletFree + " - maxBF = " + maxBulletFree + " - bulletFree = " + countBulletFree);
            countSo = 0;
            moneyBulletFree = 0;//reset
            return  countBulletFree;
        }

        return 0;//chua nhan duoc freebullet
    }

    //goi khi ban chet ca bang bullet free
    public void addMoneyBulletFree(long moneyWin) {
        moneyBulletFree += moneyWin;
    }

    protected void defaultInit(){
        this.slot = 0;
        this.gun = gunManager.getGun(DEFAULT_GUN);
        this.playingInTable = 0;
        this.isPlaying = false;
        isBot = false;
        isHost = false;
        countSo = 0;
        countBulletFree = 0;
        countBulletSuper = 0;
        percentSuperGun = 0;
    }

    public void leaveTable(){
        this.setPlaying(false);
        this.setPlayingInTable(0);
        this.setSlot((byte) 0);
        countBulletSuper = 0;
        percentSuperGun = 0;
        countBulletFree = 0;
        countSo = 0;
        timeStampBulletFree = 0;
        moneyBulletFree = 0;
    }

    //GET SET METHOD

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setSlot(byte slot) {
        this.slot = slot;
    }

    public Gun getGun() {
        return gun;
    }

    public void setGun(Gun gun) {
        this.gun = gun;
    }

    public byte getSlot() {
        return slot;
    }

    public int getCountBulletFree() {
        //check time tai tay
        if (System.currentTimeMillis() - timeStampBulletFree > GunManager.TIME_BULLET_FREE) {
            countBulletFree = 0;
            moneyBulletFree = 0;
            timeStampBulletFree = 0;//reset
        }
        return countBulletFree;
    }

    public void decreaseNumOfTrial(){
        user.setNumOfTrial(user.getNumOfTrial() - 1);
    }
    public void increaseNumOfTrial(){
        user.setNumOfTrial(user.getNumOfTrial() - 1);
    }

    public void setPlayingInTable(int playingInTable) {
        this.playingInTable = playingInTable;
    }

    public int getPlayingInTable() {
        return playingInTable;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public ChannelContextInfo getCtx() {
        return user.getCtx();
    }

    @Override
    public int getId(){
        return user.getId();
    }

    @Override
    public MoneyBag getMoneyBag() {
        return user.getMoneyBag();
    }

    @Override
    public String getUserName(){
        return user.getUserName();
    }

    @Override
    public String getUserPass() {
        return user.getUserPass();
    }

    @Override
    public String getNickName(){
        return user.getNickName();
    }

    @Override
    public String getAvatar(){
        return user.getAvatar();
    }

    @Override
    public long getMoneyGold(){
        return user.getMoneyGold();
    }

    @Override
    public long getMoneyByType(MoneyType moneyType) {
        return user.getMoneyByType(moneyType);
    }

    @Override
    public long getMoneySilver(){
        return user.getMoneySilver();
    }

    @Override
    public void resetMoneyTrial() {
        this.user.resetMoneyTrial();
    }

    @Override
    public long getMoneyTrial() { return user.getMoneyTrial(); }

    @Override
    public TransactionStatus addMoney(Money moneyAdd) {
        return user.addMoney(moneyAdd);
    }

    @Override
    public void addMoneyWin(Money moneyWin) {
        user.addMoneyWin(moneyWin);
    }

    @Override
    public TransactionStatus subMoney(Money moneySub, boolean subAnyway) {
        return user.subMoney(moneySub, subAnyway);
    }

    @Override
    public void saveUserInfo() throws NoSuchFieldException, IllegalAccessException {
        user.saveUserInfo();
    }

    @Override
    public void saveUserMoney() throws NoSuchFieldException, IllegalAccessException {
        user.saveUserMoney();
    }

    @Override
    public void saveUserMiniGame() throws NoSuchFieldException, IllegalAccessException {
        user.saveUserMiniGame();
    }

    @Override
    public void plusNumOfTrial(int numPlus) {
        user.plusNumOfTrial(numPlus);
    }

    @Override
    public void subNumOfTrial(int numSub) {
        user.subNumOfTrial(numSub);
    }

    @Override
    public void SetMoney(long newMoney)
    {

    }

    @Override
    public long GetMoney() {
        return 0;
    }

    //#GET SET METHOD

    @Override
    public String toString() {
//        return String.format("ID: %s, SlotId: %s, PlayInTable: %s, Money: %s, Gun[Id: %s, Gold: %s]", this.id, this.tx,this.playingInTable, this.getMoneyBag().get(MoneyType.GOLD), this.getGun().getId(), this.getGun().getMultiple());
        return "Player: " + getId();
    }

    public PlayerInfoBean.Builder parseResponseBeanBuilder(int roomType) {
        PlayerInfoBean.Builder builder = PlayerInfoBean.newBuilder();
        long timeCountDownBulletFree = 0;//milisecond
        if (countBulletFree > 0) {
            timeCountDownBulletFree = GunManager.TIME_BULLET_FREE - (System.currentTimeMillis() - timeStampBulletFree);
            if (timeCountDownBulletFree <= 0) {
                timeCountDownBulletFree = 0;
                countBulletFree = 0;
                moneyBulletFree = 0;
            }
        }
        builder.setPlayerId(this.getId())
                .setAvatar(this.getAvatar())
                .setDisplayName(this.getNickName())
//                .setMoney(this.getMoneyGold())
                .setGunId(this.getGun().getId())
                .setIsBot(this.isBot)
                .setIsHost(this.isHost)
                .setSlotId(this.getSlot())
                .setCountSo(this.countSo)
                .setCountBulletFree(this.countBulletFree)
                .setMoneyBulletFree(moneyBulletFree)
                .setTimeBulletFree(timeCountDownBulletFree);
        if (roomType == RoomType.TRIAL) {
            builder.setMoney(this.getMoneyTrial());
        } else {
            builder.setMoney(this.getMoneyGold());
        }
        return builder;
    }

    @Override
    public PlayerInfoBean.Builder parseResponseBeanBuilder() {
        PlayerInfoBean.Builder builder = PlayerInfoBean.newBuilder();
        builder.setPlayerId(this.getId())
                .setAvatar(this.getAvatar())
                .setDisplayName(this.getNickName())
                .setMoney(this.getMoneyGold())
                .setGunId(this.getGun().getId())
                .setIsBot(this.isBot)
                .setIsHost(this.isHost)
                .setSlotId(this.getSlot())
                .setCountSo(this.countSo)
                .setCountBulletFree(this.countBulletFree);
        return builder;
    }
}
