package games.core.database.document;

import games.minigame.database.MongoMiniGameConst;
import games.core.database.model.MUserMiniGame;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.annotation.DocumentFieldUnique;

/**
 * Created by tuanhoang on 8/17/17.
 * Thông tin của user ở Mini Game
 * - ticket wheel
 * - Mã số của user
 * - bla bla
 */
@CollectionClass(MongoMiniGameConst.COLL_USER_MINI_GAME)
public class UserMiniGame extends MGDocument implements MongoMiniGameConst{

    public UserMiniGame() {

    }

    public UserMiniGame(int userId) {
        this.userId = userId;
    }

    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int userId = 0;

    @DocumentField(FIELD_UM_TICKET_WHEEL)
    private int ticketWheel = 0;

    @DocumentField(FIELD_NUM_OF_TRIAL)
    private int numOfTrial = 0;

    //ngày gần nhất nhận thưởng
    @DocumentField(FIELD_DAY_REWARD)
    private int dayReward = 0;

    //ngày gần nhất Login
    @DocumentField(FIELD_DAY_LOGIN)
    private int dayLogin = 0;

    @DocumentField(FIELD_REWARD_COUNT_DAY)
    private int rewardCountDay = 0;

    @DocumentField(FIELD_PROMOTE_CODE)
    @DocumentFieldUnique
    private String promoteCode = "";

    @DocumentField(FIELD_ENTER_FRIEND_CODE)
    private boolean isEnterCodeFriend = false;

    @DocumentField(FIELD_COUNT_RECIEVE_VIDEO_ADS)
    private int countRecieveVideoAds = 0;

    @DocumentField(FIELD_COUNT_FRIEND_PROMOTE_CODE)
    private int countFriendPromoteCode = 0;

    @DocumentField(FIELD_DAY_CASHOUT)
    private int dayCashout = 0;

    @DocumentField(FIELD_MONEY_CASHOUT_INDAY)
    private long moneyCashoutInDay = 0;

    @Override
    public UserMiniGame updateDB() throws IllegalAccessException {
        MUserMiniGame.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public UserMiniGame saveDB() throws IllegalAccessException, NoSuchFieldException {
        MUserMiniGame.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    //region getter - setter

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.changed.put(FIELD_DEFAULT_ID, userId);
    }

    public int getNumOfTrial() {
        return numOfTrial;
    }

    public int getTicketWheel() {
        return ticketWheel;
    }

    public void setNumOfTrial(int numOfTrial){
        this.numOfTrial = numOfTrial;
        this.changed.put(FIELD_NUM_OF_TRIAL, this.numOfTrial);
    }

    public void setTicketWheel(int ticketWheel) {
        this.ticketWheel = ticketWheel;
        this.changed.put(FIELD_UM_TICKET_WHEEL, this.ticketWheel);
    }

    public int getDayReward() {
        return dayReward;
    }

    public void setDayReward(int dayReward) {
        this.dayReward = dayReward;
        this.changed.put(FIELD_DAY_REWARD, this.dayReward);
    }

    public int getRewardCountDay() {
        return rewardCountDay;
    }

    public void setRewardCountDay(int rewardCountDay) {
        this.rewardCountDay = rewardCountDay;
        this.changed.put(FIELD_REWARD_COUNT_DAY, this.rewardCountDay);
    }

    public int getDayLogin() {
        return dayLogin;
    }

    public void setDayLogin(int dayLogin) {
        this.dayLogin = dayLogin;
        this.changed.put(FIELD_DAY_LOGIN, this.dayLogin);
    }

    public String getPromoteCode() {
        return promoteCode;
    }

    public void setPromoteCode(String promoteCode) {
        this.promoteCode = promoteCode;
        this.changed.put(FIELD_PROMOTE_CODE, this.promoteCode);
    }

    public boolean isEnterCodeFriend() {
        return isEnterCodeFriend;
    }

    public void setEnterCodeFriend(boolean enterCodeFriend) {
        isEnterCodeFriend = enterCodeFriend;
        this.changed.put(FIELD_ENTER_FRIEND_CODE, this.isEnterCodeFriend);
    }

    public int getCountRecieveVideoAds() {
        return countRecieveVideoAds;
    }

    public void setCountRecieveVideoAds(int countRecieveVideoAds) {
        this.countRecieveVideoAds = countRecieveVideoAds;
        this.changed.put(FIELD_COUNT_RECIEVE_VIDEO_ADS, this.countRecieveVideoAds);
    }

    public int getCountFriendPromoteCode() {
        return countFriendPromoteCode;
    }

    public void setCountFriendPromoteCode(int countFriendPromoteCode) {
        this.countFriendPromoteCode = countFriendPromoteCode;
        this.changed.put(FIELD_COUNT_FRIEND_PROMOTE_CODE, this.countFriendPromoteCode);
    }

    public int getDayCashout() {
        return dayCashout;
    }

    public void setDayCashout(int dayCashout) {
        this.dayCashout = dayCashout;
        this.changed.put(FIELD_DAY_CASHOUT, this.dayCashout);
    }

    public long getMoneyCashoutInDay() {
        return moneyCashoutInDay;
    }

    public void setMoneyCashoutInDay(long moneyCashoutInDay) {
        this.moneyCashoutInDay = moneyCashoutInDay;
        this.changed.put(FIELD_MONEY_CASHOUT_INDAY, this.moneyCashoutInDay);
    }
    //endregion
}
