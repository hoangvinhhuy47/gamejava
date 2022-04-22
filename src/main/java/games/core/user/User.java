package games.core.user;

import games.cashout.CashoutService;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyBag;
import games.core.common.money.MoneyType;
import games.core.common.money.TransactionStatus;
import games.core.database.document.UserInfo;
import games.core.database.document.UserMiniGame;
import games.core.database.document.UserMoney;
import games.features.friends.FriendsService;
import games.minigame.constant.AddTicketWheelFrom;
import games.minigame.wheel.FortuneWheelService;
import games.tx.TaiXiuGame;
import libs.util.CRandom;
import libs.util.Helper;
import message.FeatureProtos;

import java.util.*;

/**
 * User use for manager channel, money.. every game User other need to extends this class
 */
public class User implements IUser{
    private MoneyBag moneyBag;
    private UserInfo userInfo;
    private UserMoney userMoney;
    private UserMiniGame userMiniGame;
    private List<Integer> listFriendIds;
    private int countEmailNotRead = 0;
    private boolean isBot = false;

    private long money;

    public boolean openAds = true;
    private TimerTask timerTask;
    private Timer timer = new Timer();;

    public long countNumberTimeWatchAds = 0;
    private TimerTask task;
    private Timer timerWatchAds = new Timer();;


    public TaiXiuGame taiXiuGame = new TaiXiuGame()
    {
        @Override
        protected void initRooms() {

        }

        @Override
        public void run() {

        }
    };


    public void After15Seconds()
    {
        // Huỷ timer cũ trước khi thực hiện timer mới
        timer.cancel();
        timer = new Timer();

        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                openAds = true;
                timer.cancel();
            }
        };
        timer.schedule(timerTask, 15000, 15000);

    }

    public void After24Hour()
    {
        // Huỷ timer cũ trước khi thực hiện timer mới
        timerWatchAds.cancel();
        timerWatchAds = new Timer();

        task = new TimerTask()
        {
            @Override
            public void run()
            {
                countNumberTimeWatchAds = 0;
                timerWatchAds.cancel();
            }
        };
        timerWatchAds.schedule(task, 86400000, 86400000);//86400000
    }


    /**
     * ChannelContextInfo ctx of player get by ChannelService
     */
    private ChannelContextInfo ctx;

    public User(int userId, long money) {
        if (userId!=0){
            try {
                userInfo = new UserInfo(userId).updateDB();
                userMoney = new UserMoney(userId).updateDB();
                userMiniGame = new UserMiniGame(userId).updateDB();
                listFriendIds = FriendsService.getInstance().listFriend(userId);
                this.moneyBag = new MoneyBag(userMoney.getMoneyGold(), userMoney.getMoneySilver());
                this.isBot = false;
                this.money = money;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            userInfo = new UserInfo();
            this.setId(userId);
            this.isBot = false;
        }

    }



    public User(UserInfo userInfo, UserMoney userMoney, UserMiniGame userMiniGame, int countEmailNotRead, boolean isBot){
        this.userInfo = userInfo;
        this.userMoney = userMoney;
        this.userMiniGame = userMiniGame;
        this.moneyBag = new MoneyBag(userMoney.getMoneyGold(), userMoney.getMoneySilver());
        this.countEmailNotRead = countEmailNotRead;

        this.isBot = isBot;
        if (!this.isBot) {
            listFriendIds = FriendsService.getInstance().listFriend(userInfo.getId());
        } else {
            listFriendIds = new ArrayList<>();
        }
    }

    protected User()
    {
        this.userInfo = new UserInfo();
        this.setId(0);
        this.setAvatar("");
        this.setUserName("");
        this.setNickName("");
        this.setMoneyBag(new MoneyBag());
        this.isBot = false;
        listFriendIds = new ArrayList<>();
    }

    public static User createFakeUser(){
        return createFakeUser(null);
    }
    public static User createFakeUser(Integer userId){
        if (userId == null){
            CRandom random = new CRandom();
            userId = random.randInt(1, Integer.MAX_VALUE-1);
        }
        User user = new User();
        user.setId(userId);
        return user;
    }

    public void resetMiniGame() {
        this.setTicketWheel(0);
        try {
            FortuneWheelService.getInstance().addTicket(this, 1, AddTicketWheelFrom.LOGIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.userMiniGame.setCountRecieveVideoAds(0);
    }

    public void updateInfo(String nickname, int gender, String mail, String phone) throws NoSuchFieldException, IllegalAccessException {
        this.userInfo.setNickName(nickname);
        this.userInfo.setGender(gender);
        this.userInfo.setMail(mail);
        this.userInfo.setPhone(phone);
        this.userInfo.saveDB();
    }

    //friend
    public void addFriend(Integer friendId) {
        if (!listFriendIds.contains(friendId)) {
            listFriendIds.add(friendId);
        }
    }

    public void removeFriend(Integer friendId) {
        if (listFriendIds.contains(friendId)) {
            listFriendIds.remove(friendId);
        }
    }
    public boolean isFriend(Integer friendId) {
        return listFriendIds.contains(friendId);
    }
    //end friend

    public boolean checkUserCashout(long moneyCashout) {
        int dayCurrent = Helper.getDayCurrent();

        int dayCashout = userMiniGame.getDayCashout();
        long moneyCashoutCur = userMiniGame.getMoneyCashoutInDay();
        if (dayCurrent > dayCashout) {
            dayCashout = dayCurrent;
            moneyCashoutCur = 0;
        }

        moneyCashoutCur += moneyCashout;
        if (moneyCashoutCur > CashoutService.getInstance().MONEY_CASHOUT_LIMIT_PERDAY) {
            return false;
        }
        return true;
    }

    //khi cashout success
    public void updateUserCashout(long moneyCashout) {
        int dayCurrent = Helper.getDayCurrent();
        userMiniGame.setDayCashout(dayCurrent);

        long moneyCashoutCur = userMiniGame.getMoneyCashoutInDay() + moneyCashout;

        userMiniGame.setMoneyCashoutInDay(moneyCashoutCur);
        try {
            userMiniGame.saveDB();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /*
    * isBot
    * */
    public boolean isBot() {
        return isBot;
    }

    public int getCountEmailNotRead() {
        return countEmailNotRead;
    }

    public void setCountEmailNotRead(int countEmailNotRead) {
        this.countEmailNotRead = countEmailNotRead;
    }

    public void addEmail() {
        this.countEmailNotRead ++;
    }

    public void readEmail() {
        this.countEmailNotRead --;
    }

    private void setTicketWheel(int ticketWheel) {
        this.userMiniGame.setTicketWheel(ticketWheel);
    }

    public void setNumOfTrial(int numOfTrial) {
        this.userMiniGame.setNumOfTrial(numOfTrial);
    }

    public int getNumOfTrial(){
        return userMiniGame.getNumOfTrial();
    }

    @Override
    public MoneyBag getMoneyBag() {
        return moneyBag;
    }

    private void setMoneyBag(MoneyBag moneyBag)
    {
        this.moneyBag = moneyBag;
    }

    @Override
    public ChannelContextInfo getCtx() {
        return ctx;
    }

    public void setCtx(ChannelContextInfo ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getId() {
        return userInfo.getId();
    }

    public void setId(int id) {
        this.userInfo.setId(id);
    }

    @Override
    public String getUserName() {
        return userInfo.getUserName();
    }

    @Override
    public String getUserPass() {
        return userInfo.getUserPassword();
    }

    private void setUserName(String userName) {
        this.userInfo.setUserName(userName);
    }

    @Override
    public String getAvatar() {
        return userInfo.getAvatar();
    }

    @Override
    public String getNickName() {
        return userInfo.getNickName();
    }

    @Override
    public long getMoneyGold(){
        return moneyBag.get(MoneyType.GOLD).getAmount();
    }

    @Override
    public long getMoneyByType(MoneyType moneyType) {
        return moneyBag.get(moneyType).getAmount();
    }

    @Override
    public long getMoneySilver(){
        return moneyBag.get(MoneyType.SILVER).getAmount();
    }

    @Override
    public long getMoneyTrial() {
        return moneyBag.get(MoneyType.TRIAL).getAmount();
    }

    @Override
    public void resetMoneyTrial() {
        this.moneyBag.resetMoneyTrial();
    }

    public void setNickName(String nickName) {
        this.userInfo.setNickName(nickName);
    }

    public void setAvatar(String avatar)
    {
        this.userInfo.setAvatar(avatar);
    }

    public void setMail(String mail) {
        userInfo.setMail(mail);
    }

    public void setPhone(String phone) {
        userInfo.setPhone(phone);
    }

    public void setOTP(String otp) {
        userInfo.setOtp(otp);
    }

    public void setTimeOTP(int timeOTP) {
        userInfo.setTimeOtp(timeOTP);
    }

    public void setVerifyPhone(boolean isVerify) {
        userInfo.setIsVerifyPhone(isVerify);
    }

    public void setGender(int gender) {
        userInfo.setGender(gender);
    }

    public String getMail() {
        return userInfo.getMail();
    }

    public int getGender() {
        return userInfo.getGender();
    }

    public String getPhone() {
        return userInfo.getPhone();
    }

    public String getOTP() {
        return userInfo.getOtp();
    }

    public boolean IsVerifyPhone() {
        return userInfo.getIsVerifyPhone();
    }

    public boolean isBanned(){
        return userInfo.isBanned();
    }

    public int getTimeOTP() {
        return userInfo.getTimeOtp();
    }

    public String getMacAddress() {
        return userInfo.getMacAddress();
    }


    public String getImie() {
        return userInfo.getImie();
    }

    public String getIpAddress() {
        return userInfo.getIpAddress();
    }

    public String getDeviceId() {
        return userInfo.getDeviceId();
    }

    public String getSource() {return  userInfo.getSource(); }


    @Override
    public TransactionStatus addMoney(Money moneyAdd){
        moneyBag.add(moneyAdd);
        return TransactionStatus.SUCCESS;
    }

    @Override
    public void addMoneyWin(Money moneyWin)
    {
        if (moneyWin.getMoneyType() == MoneyType.GOLD)
        {
            userMoney.setWinGold(userMoney.getWinGold() + moneyWin.getAmount());
        }
    }

    @Override
    public TransactionStatus subMoney(Money moneySub, boolean subAnyway){
        //Check moneyType
        if (moneySub.getMoneyType() == MoneyType.NONE)
            return TransactionStatus.WRONG_MONEY_TYPE;
        //Check if sub anyway (sub money even amount in MoneyBag < moneySub)
        if (subAnyway){
            moneyBag.sub(moneySub);
            return TransactionStatus.SUCCESS;
        }
        //If not sub anyway (check enough money->sub)
        if (moneyBag.isEnoughMoney(moneySub)){
            moneyBag.sub(moneySub);
            return TransactionStatus.SUCCESS;
        }
        //When not enough money
        return TransactionStatus.NOT_ENOUGH_MONEY;
    }

    @Override
    public void saveUserInfo() throws NoSuchFieldException, IllegalAccessException {
        userInfo.saveDB();
    }

    void updateMoneyBagToUserMoney()
    {
        userMoney.setMoneyGold(moneyBag.get(MoneyType.GOLD).getAmount());
        userMoney.setMoneySilver(moneyBag.get(MoneyType.SILVER).getAmount());
    }

    public void addMoneyBagToUserMoney(long money) {
        moneyBag.add(new Money(money,MoneyType.GOLD));
    }

    @Override
    public void saveUserMoney() throws NoSuchFieldException, IllegalAccessException
    {
        this.updateMoneyBagToUserMoney();
        userMoney.saveDB();
    }

    @Override
    public void saveUserMiniGame() throws NoSuchFieldException, IllegalAccessException {
        userMiniGame.saveDB();
    }

    public UserMoney getUserMoney(){return userMoney;}

    public int getTicketWheel() {
        return userMiniGame.getTicketWheel();
    }

    public void addTicketWheel(int ticket) {
        userMiniGame.setTicketWheel(getTicketWheel() + ticket);
    }

    public void subTicketWheel(int ticket) {
        userMiniGame.setTicketWheel(getTicketWheel() - ticket);
    }

    public int getDayReward() {
        return userMiniGame.getDayReward();
    }

    public void setDayReward(int dayReward) {
        userMiniGame.setDayReward(dayReward);
    }

    public int getDayLogin() {
        return userMiniGame.getDayLogin();
    }

    public void setDayLogin(int dayLogin) {
        userMiniGame.setDayLogin(dayLogin);
    }

    public int getRewardCountDay() {
        return userMiniGame.getRewardCountDay();
    }

    public void setRewardCountDay(int rewardCountDay) {
        userMiniGame.setRewardCountDay(rewardCountDay);
    }

    @Override
    public void plusNumOfTrial(int numPlus) {
        userMiniGame.setNumOfTrial(userMiniGame.getNumOfTrial() + numPlus);
    }

    @Override
    public void subNumOfTrial(int numSub) {
        userMiniGame.setNumOfTrial(userMiniGame.getNumOfTrial() - numSub);
    }

    @Override
    public void SetMoney(long newMoney)
    {
        money = newMoney;
    }

    @Override
    public long GetMoney()
    {
        return money;
    }

    public boolean isEnterCodeFriend() {
        return userMiniGame.isEnterCodeFriend();
    }

    public void setEnterCodeFriend(boolean enterCodeFriend) {
        userMiniGame.setEnterCodeFriend(enterCodeFriend);
    }

    public int getCountRecieveVideoAds() {
        return userMiniGame.getCountRecieveVideoAds();
    }

    public void addCountRecieveVideoAds(int addCount) {
        userMiniGame.setCountRecieveVideoAds(addCount + getCountRecieveVideoAds());
    }

    public int getCountFriendPromoteCode() {
        return userMiniGame.getCountFriendPromoteCode();
    }

    public void addFriendPromoteCode(int addCount) {
        userMiniGame.setCountFriendPromoteCode(addCount + getCountFriendPromoteCode());
    }


    //Friend
    public FeatureProtos.FriendInfoBean parseToFriendBean(boolean isFacebook) {
        FeatureProtos.FriendInfoBean.Builder friendInfoBean = FeatureProtos.FriendInfoBean.newBuilder();
        friendInfoBean.setFriendId(this.getId());
        friendInfoBean.setNickName(this.getNickName());
        friendInfoBean.setAvatarUrl(this.getAvatar());
        friendInfoBean.setGender(this.getGender());
        friendInfoBean.setGold(this.getMoneyGold());
        friendInfoBean.setIsFacebookFriend(isFacebook);
        return friendInfoBean.build();
    }

    public void bans() throws NoSuchFieldException, IllegalAccessException {
        userInfo.setBanned(true);
        userInfo.saveDB();
    }

    public void unBans() throws NoSuchFieldException, IllegalAccessException {
        userInfo.setBanned(false);
        userInfo.saveDB();
    }
}
