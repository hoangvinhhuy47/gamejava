package games.core.user;

import com.google.protobuf.InvalidProtocolBufferException;
import games.APISendMoney;
import games.core.application.ServerConfig;
import games.core.application.channel.ChannelService;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.application.handler.websocket.ProtobufHandler;
import games.core.common.data.DataCommonManager;
import games.core.common.money.*;
import games.core.constant.*;
import games.core.database.document.UserMoney;
import games.core.database.model.MUserInfo;
import games.core.user.object.DailyRewardItem;
import games.features.pushmessage.PushMessageService;
import games.shootingfish.constant.ResponseFishingErrorCodeConst;
import games.system.SystemService;
import games.system.VersionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import libs.util.CRandom;
import libs.util.Helper;
import libs.util.JsonUtil;
import libs.util.LogFactory;
import message.UserProtos;
import message.UserProtos.LoginRequestBean;
import message.UserProtos.LoginResponseBean;
import message.XProtos;
import message.XProtos.InitClientResponseBean;
import message.XProtos.RequestInitClientBean;
import message.XProtos.XMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class UserCommand extends ICommand implements RequestBeanConstant, ResponseBeanConstant, ResponseErrorCodeConst {
    private static UserCommand instance = new UserCommand();

    public static UserCommand getInstance() {
        if (instance == null) {
            instance = new UserCommand();
        }
        return instance;
    }

    private UserCommand() {
        userService = UserService.getInstance();
        channelService = ChannelService.getInstance();
        log = LogFactory.getLogger(this.getClass().getSimpleName());
        linkFanpage = ServerConfig.getInstance().getLinkFanpage();
        systemService = SystemService.getInstance();
        versionManager = VersionManager.getInstance();
    }

    private String linkFanpage = "";
    private Logger log;
    private UserService	userService;
    private ChannelService channelService;
    private SystemService systemService;
    private VersionManager versionManager;

    private static final int TIME_EXPIRE_OTP = 600;//10p

    @Override
    public void process(ChannelContextInfo ctx, XMessage message) throws IOException, NoSuchFieldException, IllegalAccessException {
        int type = message.getBeanType();
        switch (type)
        {
            case INIT_CLIENT_REQUEST_BEAN:
                processInitClient(ctx, message);
                break;
            case LOGIN_REQUEST_BEAN:
                processLogin(ctx, message);
                break;
            case REQUEST_PING_CHECK_SERVER:
                //Client request check server
                log.info("Client ping check network - " + ctx.getUserId());
                ctx.pingToClient(RESPONSE_PING_RESPONSE_CLIENT);
                break;
            case PING_RESPONSE_SERVER_REQUEST_BEAN:
                //client response khi nhận được ping từ server. Ko cần làm gì cả,
                break;
            case UPDATE_MONEY_REQUEST_BEAN:
                this.updateMoneyResponse(ctx);
                break;
            case GET_INFO_OTHER_PLAYER_REQUEST_BEAN:
                UserProtos.GetInfoOtherUserRequestBean requestBean = UserProtos.GetInfoOtherUserRequestBean.parseFrom(message.getData());
                getInfoOtherUser(ctx, requestBean.getUserId());
                break;
            case INIT_DAILY_REWARD_REQUEST_BEAN:
                initDailyReward(ctx);
                break;
            case DAILY_REWARD_REQUEST_BEAN:
                UserProtos.DailyRewardRequestBean dailyRewardRequestBean = UserProtos.DailyRewardRequestBean.parseFrom(message.getData());
                dailyReward(ctx, dailyRewardRequestBean);
                break;
            case UPDATE_USER_INFO_REQUEST_BEAN:
                UserProtos.UpdateUserInfoRequestBean userInfoRequestBean = UserProtos.UpdateUserInfoRequestBean.parseFrom(message.getData());
                updateInfo(ctx, userInfoRequestBean);
                break;
            case GET_OTP_REQUEST_BEAN:
                UserProtos.GetOTPRequestBean otpRequestBean = UserProtos.GetOTPRequestBean.parseFrom(message.getData());
                getOTP(ctx, otpRequestBean);
                break;
            case VERIFY_PHONE_REQUEST_BEAN:
                UserProtos.VerifyPhoneRequestBean verifyRequestBean = UserProtos.VerifyPhoneRequestBean.parseFrom(message.getData());
                updatePhone(ctx, verifyRequestBean);
                break;


        }
    }



    public void initDailyReward(ChannelContextInfo ctx) {
        try {

            UserProtos.ListDailyRewardResponseBean.Builder listDailyResponseBean = UserProtos.ListDailyRewardResponseBean.newBuilder();

            User user = userService.getUser(ctx.getUserId());
            if (user != null) {
                int dayRewarded = user.getDayReward();
                int dayCurrent = Helper.getDayCurrent();
                int rewardCountDay = user.getRewardCountDay();//hôm nay đã nhận rồi
                boolean recieveRewardToday = false;
                if (dayRewarded < dayCurrent) {
                    //hôm nay chưa nhận quà
                    if (dayCurrent - 1 == dayRewarded)
                    {
                        //ngày hôm qua có nhận
                        rewardCountDay = user.getRewardCountDay() + 1;
                        if (rewardCountDay > 7)
                        {
                            rewardCountDay = 1;
                        }
                    } else {
                        rewardCountDay = 1;
                    }
                } else {
                    recieveRewardToday = true;
                }
                listDailyResponseBean.setDailyRewardDay(rewardCountDay);
                listDailyResponseBean.setRevicedRewardToday(recieveRewardToday);

                Collection<DailyRewardItem> listDailyItem = DataCommonManager.getInstance().getDailyRewardItems();
                for (DailyRewardItem rewardItem : listDailyItem) {
                    listDailyResponseBean.addListDailyReward(rewardItem.parseResponseBeanBuilder());
                }
                XMessage.Builder xMessage = XMessage.newBuilder();
                xMessage.setCommand(ServerCommandConstant.USER_COMMAND).setBeanType(INIT_DAILY_REWARD_RESPONSE_BEAN)
                        .setData(listDailyResponseBean.build().toByteString());
                ctx.writeAndFlush(xMessage.build());
            }

        } catch (Exception e) {
            log.error("Init daily reward error", e);
        }


    }

    public void dailyReward(ChannelContextInfo ctx, UserProtos.DailyRewardRequestBean requestBean) {
        if (ctx.checkAuth()) {
            int userId = ctx.getUserId();
            User user = userService.getUser(userId);
            UserProtos.DailyRewardResponseBean.Builder rewardResponseBean = UserProtos.DailyRewardResponseBean.newBuilder();
            try {
                if (user != null) {
                    int rewardCountDay = requestBean.getDay();

                    int dayCurrent = Helper.getDayCurrent();
                    if (dayCurrent > user.getDayReward()) {
                        int dayCanReward = -1;
                        if (dayCurrent - 1 == user.getDayReward()) {
                            dayCanReward = user.getRewardCountDay() + 1;
                            if (dayCanReward > 7) {
                                dayCanReward = 1;
                            }
                        } else {
                            dayCanReward = 1;//reset lai
                        }
                        rewardCountDay = dayCanReward;
                        //if (rewardCountDay == dayCanReward) {
                            rewardResponseBean.setErrorCode(REC_SUCCESS);
                            rewardResponseBean.setDay(rewardCountDay);

                            DailyRewardItem dailyRewardItem = DataCommonManager.getInstance().getDailyRewardItemByDay(rewardCountDay);

                            long moneyReward = dailyRewardItem.getMoneyReward();
                            Money money = new Money(moneyReward, MoneyType.GOLD);

                            //nhận tiền nhé
                            long moneyBefore = user.getMoneyGold();
                            user.SetMoney(moneyReward);
                            TransactionData transactionData = MoneyService.getInstance().addMoneyToSave(money, user, true, "dailyReward");
                            if (transactionData.getStatus() == TransactionStatus.SUCCESS) {
                                rewardResponseBean.setGoldReward(moneyReward);

                                //save thong tin lai
                                user.setDayReward(dayCurrent);
                                user.setRewardCountDay(rewardCountDay);
                                user.saveUserMiniGame();

                            } else {
                                rewardResponseBean.setErrorCode(REC_NOT_DEFINE);
                            }

                       // }
//                        else {
//                            rewardResponseBean.setErrorCode(REC_DR_DAY_REWARD_WRONG);
//                        }
                    } else {
                        rewardResponseBean.setErrorCode(REC_DR_YOU_RECIEVE_REWARD_TODAY);
                    }
                } else {
                    rewardResponseBean.setErrorCode(REC_USER_NOT_FOUND);
                }
            } catch (Exception e) {
                rewardResponseBean.setErrorCode(REC_NOT_DEFINE);
                log.error("Daily reward error", e);
            }


            log.info("Daily reward user id = " + userId + " - error code = " + rewardResponseBean.getErrorCode());
            XMessage.Builder response = XMessage.newBuilder();
            response.setCommand(ServerCommandConstant.USER_COMMAND).setBeanType(DAILY_REWARD_RESPONSE_BEAN)
                    .setData(rewardResponseBean.build().toByteString());

            ctx.writeAndFlush(response.build());
        }
    }

    public void getInfoOtherUser(ChannelContextInfo ctx, int otherUserId) {
        if (ctx.checkAuth()) {
            User user = userService.getUser(otherUserId);
            if (user == null) {
                try {
                    user = userService.getUserFromDb(otherUserId);
                } catch (Exception e) {
                    log.error("Get user from db error: user id = " + otherUserId, e);
                }
            }
            UserProtos.GetInfoOtherUserResponseBean.Builder infoOtherPlayerResponse = UserProtos.GetInfoOtherUserResponseBean.newBuilder();
            if (user != null) {
                infoOtherPlayerResponse.setErrorCode(REC_SUCCESS);
                infoOtherPlayerResponse.setAvatarUrl("");
                infoOtherPlayerResponse.setUserId(user.getId());
                infoOtherPlayerResponse.setNickName(user.getNickName());
                infoOtherPlayerResponse.setUserName(user.getNickName());
                infoOtherPlayerResponse.setGold(user.getMoneyGold());
                infoOtherPlayerResponse.setSilver(user.getMoneySilver());
                infoOtherPlayerResponse.setGender(user.getGender());
                infoOtherPlayerResponse.setMail(user.getMail());
                infoOtherPlayerResponse.setPhone(user.getPhone());
                boolean isFriend = user.isFriend(ctx.getUserId());
                infoOtherPlayerResponse.setIsFriend(isFriend);
            }

            else
            {
                infoOtherPlayerResponse.setErrorCode(REC_USER_NOT_FOUND);
            }

            XMessage.Builder response = XMessage.newBuilder();
            response.setCommand(ServerCommandConstant.USER_COMMAND).setBeanType(GET_INFO_OTHER_PLAYER_RESPONSE_BEAN)
                    .setData(infoOtherPlayerResponse.build().toByteString());
            ctx.writeAndFlush(response.build());
        }

    }

    public void updateMoneyResponse(ChannelContextInfo ctx) {
        UserProtos.UpdateGoldAndSilverResponseBean.Builder responseBean = UserProtos.UpdateGoldAndSilverResponseBean.newBuilder();
        if (ctx.checkAuth()) {
            int userId = ctx.getUserId();

            try {
                User user = userService.getUserFromDb(userId);
                if (user != null) {
                    responseBean.setGold(user.getMoneyGold());
                    responseBean.setSilver(user.getMoneySilver());
                    responseBean.setTicketWheel(user.getTicketWheel());
                    responseBean.setCountMailNotRead(user.getCountEmailNotRead());
                    XMessage.Builder responseMessage = XMessage.newBuilder().setCommand(ServerCommandConstant.USER_COMMAND).
                            setBeanType(UPDATE_MONEY_RESPONSE_BEAN).
                            setData(responseBean.build().toByteString());
                    ctx.writeAndFlush(responseMessage.build());
                } else {
                    log.info("Get user error in update Money - user ID = " + userId);
                }

            } catch (Exception e) {
                log.error("Get money by user error " + userId, e);
            }

        }
    }

    private void processInitClient(ChannelContextInfo ctx, XMessage message) throws InvalidProtocolBufferException
    {

        InitClientResponseBean.Builder initClientResponseBean = InitClientResponseBean.newBuilder();
        if (!systemService.isServerMaintance())
        {
            RequestInitClientBean requestBean = RequestInitClientBean.parseFrom(message.getData());
            int clientVersion = requestBean.getClientVersion();
            Platform platform = Platform.valueOf(requestBean.getPlatform());
            String deviceId = requestBean.getDeviceId();
            //deviceId = deviceId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

            initClientResponseBean.addAllPaymentItem(DataCommonManager.getInstance().getPaymentItemResponseBeans());

            String macAddress = requestBean.getMacAddress();
            String imie = requestBean.getImie();

            //tạm thời thôi
            String source = requestBean.getOsVersion();
            Map<String, Object> data = new HashMap<>();
            data.put("clientVersion", clientVersion);
            data.put("platform", platform);
            data.put("mac address", macAddress);
            data.put("deviceId", deviceId);
            log.info(">>>>>>>>>>>>>>>>> INIT_CLIENT_REQUEST_BEAN {}" + JsonUtil.generateJson(data));

            boolean isNewVersion = false;
            //Tạm thời gán = 140 sẽ quay lại sửa sau
            if (clientVersion <= 250)//!versionManager.checkActiveVersion(clientVersion, platform)
            {
                //Version version = versionManager.maxVersionByPlatform(platform);
                //if (version != null)
                //{
                    isNewVersion = true;
                    initClientResponseBean.setErrorCode(REC_FORCE_UPDATE_NEW_VERSION);
                    initClientResponseBean.setLinkStore("https://banca.games"); //version.getUrl()
                //}
            }
            if (!isNewVersion)
            {
                InetSocketAddress ipAddress = (InetSocketAddress)ctx.getChannel().remoteAddress();
                initClientResponseBean.setErrorCode(0).setServerTime(System.currentTimeMillis());
                initClientResponseBean.setLinkFanpage(linkFanpage);
                initClientResponseBean.setIsEnableIap(systemService.isEnableIAP());
                ctx.init(deviceId, macAddress, imie, ipAddress.getHostName(), platform, clientVersion, source);
            }
        } else {
            initClientResponseBean.setErrorCode(ResponseErrorCodeConst.REC_SERVER_MAINTENANCE);
        }

        XMessage.Builder responseMessage = XMessage.newBuilder().setCommand(ServerCommandConstant.USER_COMMAND).
                setBeanType(INIT_CLIENT_RESPONSE_BEAN).setData(initClientResponseBean.build().toByteString());
        ctx.writeAndFlush(responseMessage.build());
    }

    private void processLogin(ChannelContextInfo ctx, XMessage message) throws IOException {
        LoginRequestBean requestBean = LoginRequestBean.parseFrom(message.getData());
        LoginResponseBean.Builder loginResponseBean = userService.getLoginResponse(requestBean.getToken(), ctx);
        try{

            //Nếu login thành công
            if (loginResponseBean.getErrorCode() == ResponseFishingErrorCodeConst.REC_SUCCESS)
            {
                //Nếu xác thực thành công
                if (channelService.authChannel(loginResponseBean.getUserId(), ctx))
                {
                    //Set channel cho user
                    //Send text run for user when login success

                    User user = userService.getUser(loginResponseBean.getUserId());
                    user.setCtx(ctx);
                    PushMessageService.getInstance().sendTextRunCurrentForUserLogin(user);
                    UserMoney userMoney = user.getUserMoney();

                    long gold = userMoney.getMoneyGold();
                    long wallet = userMoney.getDepositFrWallet();

                    long totalGold = gold + wallet;

                    APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                            ctx.getUser().getUserName(),
                            Long.toString(totalGold),0,""));
                }
                else
                {
                    loginResponseBean.setErrorCode(ResponseErrorCodeConst.REC_LOGIN_CAN_NOT_AUTH);
                }
            }
        } catch (Exception ex){
            loginResponseBean.setErrorCode(ResponseFishingErrorCodeConst.REC_NOT_DEFINE);
            log.trace("[processLogin] lỗi không xác định khi đăng nhập và xác thực! ", ex);
        } finally
        {
            XMessage.Builder responseMessage = XMessage.newBuilder()
                    .setCommand(ServerCommandConstant.USER_COMMAND)
                    .setBeanType(LOGIN_RESPONSE_BEAN)
                    .setData(loginResponseBean.build().toByteString());
            ctx.writeAndFlush(responseMessage.build());
        }

    }

    private void updateInfo(ChannelContextInfo ctx, UserProtos.UpdateUserInfoRequestBean requestBean)
    {
        if (ctx.checkAuth())
        {
            UserProtos.UpdateUserInfoResponseBean.Builder responseUpdateInfo = UserProtos.UpdateUserInfoResponseBean.newBuilder();
            try
            {
                int userId = ctx.getUserId();
                User user = userService.getUser(userId);
                if (requestBean.hasAvatar())
                {
                    user.setAvatar(requestBean.getAvatar());
                }

                if (requestBean.hasGender())
                {
                    user.setGender(requestBean.getGender());
                }

                if (requestBean.hasMail()) {
                    user.setMail(requestBean.getMail());
                }

                if (requestBean.hasNickName()) {
                    user.setNickName(requestBean.getNickName());
                }

                if (requestBean.hasPhone()) {
                    user.setPhone(requestBean.getPhone());
                }
                user.saveUserInfo();
                //user.updateInfo(requestBean.getNickName(), requestBean.getGender(), requestBean.getMail(), requestBean.getPhone());
                responseUpdateInfo.setErrorCode(REC_SUCCESS);
            } catch (Exception e) {
                log.error("Update info error", e);
                responseUpdateInfo.setErrorCode(REC_NOT_DEFINE);
            }

            XMessage.Builder response = XMessage.newBuilder();
            response.setCommand(ServerCommandConstant.USER_COMMAND);
            response.setBeanType(UPDATE_USER_INFO_RESPONSE_BEAN);
            response.setData(responseUpdateInfo.build().toByteString());

            ctx.writeAndFlush(response.build());
        }
    }

    public void forceUpdateMoneyToUser(int userId, io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        User user = userService.getUser(userId);
        if (user != null && user.getCtx() != null)
        {
            UserProtos.UpdateGoldAndSilverResponseBean.Builder responseBean = UserProtos.UpdateGoldAndSilverResponseBean.newBuilder();
            responseBean.setGold(user.getMoneyGold());
            responseBean.setSilver(0);
            XMessage.Builder responseMessage = XMessage.newBuilder().setCommand(ServerCommandConstant.USER_COMMAND).
                    setBeanType(UPDATE_MONEY_RESPONSE_BEAN).
                    setData(responseBean.build().toByteString());
            user.getCtx().writeAndFlush(responseMessage.build());
        }
    }

    //Xóa khỏi list user author
    public void userOffline(int userId) {
        log.info("User offline - " + userId);
        userService.removeUser(userId);
    }

    public void getOTP(ChannelContextInfo ctx, UserProtos.GetOTPRequestBean otpRequestBean)
    {
        UserProtos.GetOTPResponseBean.Builder responseBean = UserProtos.GetOTPResponseBean.newBuilder();
        String phone = "";
        if(otpRequestBean.hasPhone()) {
            phone = otpRequestBean.getPhone();
        }
        int action = otpRequestBean.getAction();
        CRandom cRandom = new CRandom();
        int rand = cRandom.randInt(1001, 9999);
        int timeCur = (int)(System.currentTimeMillis() / 1000);
        String otp = "" + rand;
        int errorCode = REC_SUCCESS;
        if (ctx.checkAuth())
        {
            int userId = ctx.getUserId();
            
            try
            {
                User user = userService.getUser(userId);
                if (user != null) {
                    if(action == 1) {
                        //verify sdt
                        if(phone != null && !phone.equals(""))
                        {
                            //kiem tra phone nay da co hay chua
                            if (MUserInfo.getInstance().checkAvailablePhoneNumber(phone))
                            {
                                //kiem tra sdt nay co dung la sdt cua user hay khong
                                //neu dung thi tra otp ve ma khong can xu ly nhieu
                                String userPhone = user.getPhone();
                                if (userPhone != null && userPhone.equals(phone))
                                {
                                    //dung sdt cua no roi
                                    int timeOtp = user.getTimeOTP();
                                    if (timeCur - timeOtp <TIME_EXPIRE_OTP) {
                                        //khong can tao otp moi. Lay otp cu
                                        String userOtp = user.getOTP();
                                        if (userOtp != null && !userOtp.equals("")) {
                                            otp = userOtp;
                                        }
                                    }
                                    user.setTimeOTP(timeCur);
                                    user.setOTP(otp);
                                    user.setPhone(phone);
                                    user.saveUserInfo();
                                    errorCode = REC_SUCCESS;
                                } else
                                    {
                                    errorCode = REC_USER_PHONE_ALREADY_EXIST;
                                }

                            }
                            else
                            {
                                user.setPhone(phone);
                                user.setOTP(otp);
                                user.setTimeOTP(timeCur);
                                user.saveUserInfo();
                                errorCode = REC_SUCCESS;
                            }

                        }
                        else
                        {
                            errorCode = REC_USER_PHONE_REQUIRE;
                        }
                    } else {
                        phone = user.getPhone();
                        if(phone != null && !phone.equals("")) {
                            int timeOtp = user.getTimeOTP();
                            if (timeCur - timeOtp <TIME_EXPIRE_OTP) {
                                //khong can tao otp moi. Lay otp cu
                                String userOtp = user.getOTP();
                                if (userOtp != null && !userOtp.equals("")) {
                                    otp = userOtp;
                                }
                                System.out.println("Get: Otp da duoc tao roi, ko can tao lai - " + otp);
                            }
                            user.setOTP(otp);
                            user.setTimeOTP(timeCur);
                            user.saveUserInfo();
                            errorCode = REC_SUCCESS;
                        } else {
                            errorCode = REC_USER_OTP_ERROR_NOT_VERIFY_PHONE;
                        }
                    }
                }
            } catch(Exception e) {
                log.error("Get user error " + userId, e);
            }
        }
        if(errorCode == REC_SUCCESS) {
            SMSService.getInstance().sendOTP(phone, otp);
        }
        
        responseBean.setErrorCode(errorCode);//
        XMessage.Builder responseMessage = XMessage.newBuilder().setCommand(ServerCommandConstant.USER_COMMAND).
                setBeanType(GET_OTP_RESPONSE_BEAN).
                setData(responseBean.build().toByteString());
        ctx.writeAndFlush(responseMessage.build());
    }

    public void updatePhone(ChannelContextInfo ctx, UserProtos.VerifyPhoneRequestBean verifyRequestBean) {
        UserProtos.VerifyPhoneResponseBean.Builder responseBean = UserProtos.VerifyPhoneResponseBean.newBuilder();
        if (ctx.checkAuth()) {
            int userId = ctx.getUserId();
            int errorCode = REC_SUCCESS;
            try {
                User user = userService.getUser(userId);
                if (user != null) {
                    String userPhone = user.getPhone();
                    String userOtp = user.getOTP();

                    String phone = verifyRequestBean.getPhone();
                    String otp = verifyRequestBean.getCode();
                    if(otp != null && !otp.equals("") && !userPhone.equals("")) {
                        if(phone.equals(userPhone) && otp.equals(userOtp)) {
                            errorCode = REC_SUCCESS;
                            user.setVerifyPhone(true);
                            user.setOTP("");
                            user.saveUserInfo();
                        } else {
                            errorCode = REC_USER_OTP_NOT_MATCH;
                        }
                    } else {
                        errorCode = REC_USER_OTP_NOT_DEFINE;
                    }
                    responseBean.setErrorCode(errorCode);//
                    XMessage.Builder responseMessage = XMessage.newBuilder().setCommand(ServerCommandConstant.USER_COMMAND).
                            setBeanType(VERIFY_PHONE_RESPONSE_BEAN).
                            setData(responseBean.build().toByteString());
                    ctx.writeAndFlush(responseMessage.build());
                } else {
                    log.info("Get user error in update Money - user ID = " + userId);
                }

            } catch (Exception e) {
                log.error("Get user error " + userId, e);
            }

        }
    }
}
