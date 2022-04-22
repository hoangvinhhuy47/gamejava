package games.core.user;

import games.cashout.database.document.IPAddressCheck;
import games.cashout.database.model.MIPAddressCheck;
import games.core.application.ServerConfig;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.data.DataCommonManager;
import games.core.constant.ResponseErrorCodeConst;
import games.core.database.document.UserInfo;
import games.core.database.document.UserMoney;
import games.core.database.model.MUserInfo;
import games.core.database.model.MUserMoney;
import games.core.exception.NotFoundUserInDB;
import games.core.user.object.InfoUserItem;
import games.features.database.document.Friends;
import games.features.database.model.MFriends;
import games.features.database.model.MInbox;
import games.features.inbox.InboxService;
import games.features.pushmessage.PushMessageService;
import games.core.database.document.UserMiniGame;
import games.core.database.model.MUserMiniGame;
import games.shootingfish.constant.ResponseFishingErrorCodeConst;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.util.Helper;
import libs.util.LogFactory;
import message.FeatureProtos;
import message.UserProtos.LoginResponseBean;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Cuong Nguyen Cao on 8/8/17.
 */
public class UserService {
    /**
     * singleton
     */
    private static UserService instance;

    /**
     * for log
     */
    private static Logger log;

    /**
     * manager userInfo in database
     */
    private MUserInfo mUserInfo;

    /**
     * send message text run when user online
     */
    PushMessageService pushMessageService;

    /**
     * manager userMoney in database
     */
    private MUserMoney mUserMoney;

    private MUserMiniGame mUserMiniGame;

    private MIPAddressCheck mipAddressCheck;

    private MInbox mInbox;

    private MFriends mFriends;

    private int moneyRewardRegister = 0;
    /**
     * list user authentication
     */
    private ConcurrentHashMap<Integer, User> listAthUser;
    private ConcurrentHashMap<Integer, io.netty.channel.ChannelHandlerContext> listCtx;

    public static UserService getInstance() {
        if (instance == null) instance = new UserService();
        return instance;
    }

    private UserService() {
        mUserInfo = MUserInfo.getInstance();
        mUserMoney = MUserMoney.getInstance();
        mUserMiniGame = MUserMiniGame.getInstance();
        mipAddressCheck = MIPAddressCheck.getInstance();
        mInbox = MInbox.getInstance();
        mFriends = MFriends.getInstance();

        listAthUser = new ConcurrentHashMap<>();
        listCtx = new ConcurrentHashMap<>();
        pushMessageService = PushMessageService.getInstance();
        moneyRewardRegister = ServerConfig.getInstance().getMoneyRewardWhenRegister();
        log = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    public static void main(String[] args)
    {

    }

    public User getUserFromDb(int userId) throws Exception {
        UserInfo userInfo = new UserInfo(userId).updateDB();
        UserMoney userMoney = new UserMoney(userId).updateDB();
        UserMiniGame userMiniGame = new UserMiniGame(userId).updateDB();
        int countMailNotRead = 0;
        return new User(userInfo, userMoney, userMiniGame, 0, false);
    }

    public FeatureProtos.FriendInfoBean getFriendInfoById(int friendId, boolean isFacebook) throws IllegalAccessException {
        User user = this.getUser(friendId);
        if (user != null) {
            return user.parseToFriendBean(isFacebook);
        } else {
            UserInfo userInfo = new UserInfo(friendId).updateDB();
            UserMoney userMoney = new UserMoney(friendId).updateDB();

            FeatureProtos.FriendInfoBean.Builder friendInfoBean = FeatureProtos.FriendInfoBean.newBuilder();
            friendInfoBean.setFriendId(friendId);
            friendInfoBean.setGender(userInfo.getGender());
            friendInfoBean.setAvatarUrl(userInfo.getAvatar());
            friendInfoBean.setNickName(userInfo.getNickName());
            friendInfoBean.setGold(userMoney.getMoneyGold());
            friendInfoBean.setIsFacebookFriend(isFacebook);

            return friendInfoBean.build();
        }

    }

    private User getUserFromDb(String token, ChannelContextInfo ctx) throws IllegalAccessException, NotFoundDocumentInDatabase, NotFoundUserInDB {
        try {
            UserInfo userInfo = mUserInfo.getUserInfoByToken(token);
            if (userInfo.isNewRegister()) {

                processWhenCreateNewUser(userInfo.getId(), userInfo, ctx);
//                userInfo.setNewRegister(false);
//                userInfo.saveDB();

                InboxService.getInstance().sendMessage(InboxService.SYSTEM_ID, userInfo.getId(), "Welcom to BlockChain Games",
                        "Welcom to BlockChain Games, Wish you have fun and hunt lots of fish.");

            }
            UserMoney userMoney = new UserMoney(userInfo.getId()).updateDB();
            UserMiniGame userMiniGame = new UserMiniGame(userInfo.getId()).updateDB();//mUserMiniGame.getUserMiniGameByUserId(userInfo.getId());//
            int countMailNotRead = mInbox.countEmailNotReadOfUser(userInfo.getId());
            return new User(userInfo, userMoney, userMiniGame, countMailNotRead, false);
        }catch (Exception e) {
            log.error("Get user form db error", e);
        }
        return null;
    }

    private void processWhenCreateNewUser(int userId, UserInfo userInfo, ChannelContextInfo ctx) throws NoSuchFieldException, IllegalAccessException {
        //tạo và lưu user money
        UserMoney userMoney = new UserMoney(userId);
        userMoney.setMoneyGold(moneyRewardRegister);//for test
        userMoney.setMoneySilver(0);
        userMoney.setWinGold(0);
        userMoney.setMoneySilver(0);
        mUserMoney.saveToMongo(userMoney);
        //tạo và lưu user mini game

        UserMiniGame userMiniGame = new UserMiniGame(userId);
        userMiniGame.setDayReward(0);
        userMiniGame.setRewardCountDay(1);
        userMiniGame.setNumOfTrial(3);
        userMiniGame.setTicketWheel(1);
        userMiniGame.setPromoteCode("" + userId);
        userMiniGame.setEnterCodeFriend(false);
        if (userInfo.getFbId() == null || userInfo.getFbId().equals("") ) {
            InfoUserItem userItem = DataCommonManager.getInstance().getRandomeInfoUser();
            if (userItem != null) {
                userInfo.setNickName(userItem.getName());
                userInfo.setGender(userItem.getGender());

            } else {
                userInfo.setNickName("Alice Top");
                userInfo.setGender(0);
            }
        } else {
            userInfo.setGender(0);
        }

        userInfo.setMail("");
        userInfo.setPhone("");
        if (userInfo.getDeviceId() != null) {
            if (userInfo.getDeviceId().equals("")) {
                userInfo.setDeviceId(ctx.getDeviceId());
            }
        }  else {
            userInfo.setDeviceId(ctx.getDeviceId());
        }


        userInfo.setMacAddress(ctx.getMacAddress());
        userInfo.setImie(ctx.getImie());
        userInfo.setIpAddress(ctx.getIpAddress());
        userInfo.setPlatform(ctx.getPlatform().getValue());
        userInfo.setVersion(ctx.getVersion());
        userInfo.setSource(ctx.getSource());

        userInfo.setNewRegister(false);
        userInfo.saveDB();
        mUserMiniGame.saveToMongo(userMiniGame);

        //Save ip address check
        IPAddressCheck ipAddressCheck = mipAddressCheck.getIPAddressCheckByIP(userInfo.getIpAddress());
        if (ipAddressCheck != null) {
          ipAddressCheck.addUserId(userId);
            ipAddressCheck.saveDB();
        }

        //khởi tạo friend
        Friends friends = mFriends.getFriendById(userId);
    }

    public void addUser(User user) {
        listAthUser.put(user.getId(), user);
    }

    public User getUser(int userId)
    {
        if (!isUserExist(userId)){
            try {
                return getUserFromDb(userId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return listAthUser.get(userId);
    }

    public boolean isUserExist(int userId){
        return listAthUser.containsKey(userId);
    }

    public void addCtx(int userID, io.netty.channel.ChannelHandlerContext ctx) {
        listCtx.put(userID, ctx);
    }

    public io.netty.channel.ChannelHandlerContext getCtx(int userId)
    {
        if (listCtx.containsKey(userId)) {
            return listCtx.get(userId);
        }
        return null;
    }


    public void removeUser(Integer userId) {
        if (listAthUser.containsKey(userId)) {
            listAthUser.remove(userId);
        }
    }

    public ConcurrentHashMap<Integer, User> getListAthUser() {
        return listAthUser;
    }

    public Collection<User> getListActiveUsers() {
        return listAthUser.values();
    }

    LoginResponseBean.Builder getLoginResponse(String token, ChannelContextInfo ctx)
    {
        LoginResponseBean.Builder responseBean = LoginResponseBean.newBuilder();
        if (token != null && !"".equals(token)) {
            try {
                User user = getUserFromDb(token, ctx);
                if (user != null)
                {
                    listAthUser.put(user.getId(), user);

                    int dayCurrent = Helper.getDayCurrent();
                    boolean reviceRewardToday = true;
                    if (dayCurrent > user.getDayReward()) {
                        reviceRewardToday = false;
                    }

                    if (dayCurrent > user.getDayLogin()) {
                        user.setDayLogin(dayCurrent);
                        user.resetMiniGame();
                        try {
                            user.saveUserMiniGame();
                        } catch (NoSuchFieldException e) {
                            log.error("save mini game error", e);
                        }
                    }

                    UserMoney userMoney = user.getUserMoney();
                    long depositFrWeb = userMoney.getDepositFrWeb();
                    long depositFrWallet = userMoney.getDepositFrWallet();
                    long totalGold = user.getMoneyGold() + depositFrWeb + depositFrWallet;

                    responseBean.setErrorCode(ResponseFishingErrorCodeConst.REC_SUCCESS)
                            .setUserId(user.getId())
                            .setAvatarUrl(user.getAvatar())
                            .setUserName(user.getUserName())
                            .setNickName(user.getNickName())
                            .setGold(totalGold)
                            .setSilver(user.getMoneySilver())
                            .setRecievedDailyReward(reviceRewardToday)
                            .setGender(user.getGender())
                            .setMail(user.getMail())
                            .setPhone(user.getPhone())
                            .setIsVerifyPhone(user.IsVerifyPhone())
                            .setIsBanned(user.isBanned())
                            .setGoldFrWallet(depositFrWallet);


                    userMoney.setDepositFrWebProvisional(0); // Biến tạm chỉ dùng để test, set nó lại = 0
                    userMoney.setDepositFrWalletProvisional(0); // Biến tạm, set nó lại = 0

                    userMoney.setDepositFrWeb(0); // Sau khi lấy tiền nạp xong r thì set nó lại = 0
                    userMoney.setDepositFrWallet(0); // Sau khi lấy tiền nạp xong r thì set nó lại = 0

                    user.addMoneyBagToUserMoney(depositFrWeb + depositFrWallet);
                    user.saveUserMoney();
                }
                else
                {
                    responseBean.setErrorCode(ResponseFishingErrorCodeConst.REC_NOT_DEFINE);
                }
            } catch (NotFoundDocumentInDatabase | NotFoundUserInDB ex) {
                log.info(ex.getMessage());
                responseBean.setErrorCode(ResponseErrorCodeConst.REC_LOGIN_WRONG_TOKEN);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                responseBean.setErrorCode(ResponseFishingErrorCodeConst.REC_NOT_DEFINE);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return responseBean;
    }

}
