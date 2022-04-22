package games.minigame.wheel;

import games.cashout.CashoutService;
import games.cashout.constant.CardType;
import games.cashout.constant.CashoutType;
import games.cashout.database.document.IPAddressCheck;
import games.cashout.database.model.MIPAddressCheck;
import games.core.application.ServerConfig;
import games.core.application.channel.TaskScheduler;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.data.DataCommonManager;
import games.core.common.money.*;
import games.core.constant.ServerCommandConstant;
import games.core.database.document.UserMiniGame;
import games.core.database.model.MUserMiniGame;
import games.core.user.User;
import games.core.user.UserService;
import games.core.user.object.InfoUserItem;
import games.features.pushmessage.PushMessageService;
import games.minigame.constant.AddTicketWheelFrom;
import games.minigame.constant.ResponseBeanMiniGameConst;
import games.minigame.constant.ResponseMiniGameErrorCode;
import games.minigame.database.document.WheelUserHistory;
import games.minigame.database.model.MWheelUserHistory;
import games.system.database.document.ReportMinigame;
import games.system.database.model.MReportMinigame;
import libs.util.CRandom;
import libs.util.Helper;
import libs.util.LogFactory;
import message.MiniGameProtos;
import message.XProtos;
import org.apache.log4j.Logger;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tuanhoang on 8/17/17.
 */
public class FortuneWheelService implements ResponseMiniGameErrorCode, ResponseBeanMiniGameConst {
    private Logger logger = LogFactory.getLogger(this.getClass().getSimpleName());
    private static FortuneWheelService instance;
    private MoneyService moneyService;
    private CashoutService cashoutService;
    private DataCommonManager dataCommonManager;

    MReportMinigame mReportMinigame = MReportMinigame.getInstance();
    ReportMinigame reportMinigame;

    MIPAddressCheck mipAddressCheck;
    private static int LIMIT_COUNT_WHEEL_HISTORY = 30;
    private  int LIMIT_CARD_REWARD_BY_IP = 0;
    private  int LIMIT_CARD_ONE_DAY_IP = 0;
    public static FortuneWheelService getInstance() {
        if (instance == null){
            instance = new FortuneWheelService();
        }
        return instance;
    }

    FortuneWheelService() {
        moneyService = MoneyService.getInstance();
        cashoutService = CashoutService.getInstance();
        mipAddressCheck = MIPAddressCheck.getInstance();
        mReportMinigame = MReportMinigame.getInstance();
        dataCommonManager = DataCommonManager.getInstance();

        int dayCurrent = Helper.getDayCurrent();
        initReportMinigameByDay(dayCurrent);

        LIMIT_CARD_REWARD_BY_IP = Integer.parseInt(ServerConfig.getInstance().getMaxCardOnceIP());
        LIMIT_CARD_ONE_DAY_IP = Integer.parseInt(ServerConfig.getInstance().getMaxCardPerDayOnceIP());
        init();
    }

    public void initReportMinigameByDay(int dayCurrent) {
        try {
            reportMinigame = mReportMinigame.getReportMinigameByDay(dayCurrent);
        } catch (Exception e) {
            reportMinigame = new ReportMinigame();
            reportMinigame.setDay(dayCurrent);
        }
    }

    void init() {
        wheelCore1 = new FortuneWheelCore();
        wheelCore1.reload(1);

        long timeStampEndDay = ZonedDateTime.now().toLocalDate().plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long now = ZonedDateTime.now().toEpochSecond();
        long delay = timeStampEndDay - now + 60 * 30;
        TaskScheduler.getInstance().scheduleAtFixedRate(new ReloadWheel(), (int)delay, 60 * 60 * 24, TimeUnit.SECONDS);
        //TaskScheduler.getInstance().scheduleAtFixedRate(new FakeUserRewardCard(), 2, 10, TimeUnit.MINUTES);


        //wheelCore2 = new FortuneWheelCore();
        //wheelCore2.reload(2);
    }

//    public class FakeUserRewardCard implements Runnable {
//
//        @Override
//        public void run() {
//            fakeReward();
//        }
//    }

    void fakeReward() {
        InfoUserItem infoUserItem = dataCommonManager.getRandomeInfoUser();
        logger.info("Fake reward - user = " + infoUserItem.getName());
        FortuneWheelItem item = null;//wheelCore1.randomeItem();
        int rand = new CRandom().randInt(0, 100);
        if (rand < 3) {
            item = wheelCore1.getByIndex(4);
        } else if (rand < 30) {
            item = wheelCore1.getByIndex(0);
        } else if (rand < 60) {
            item = wheelCore1.getByIndex(7);
        } else if (rand < 80) {
            item = wheelCore1.getByIndex(2);
        } else {
            item = wheelCore1.getByIndex(6);
        }
        if (item != null) {
            logger.info("Fake reward - item = " + item.getName());
            if (item.getCurrency() != -1) {
                pushMessageRewardByItem(infoUserItem.getId(), infoUserItem.getName(), item);
            }
        }
    }

    public class ReloadWheel implements Runnable {

        @Override
        public void run() {
            int dayCurrent = Helper.getDayCurrent();
            if (reportMinigame.getDay() != dayCurrent) {
                initReportMinigameByDay(dayCurrent);
            }
            wheelCore1.reload(1);
        }
    }

    public static void main(String[] args) {
        UserMiniGame userMiniGame = MUserMiniGame.getInstance().getUserMiniGameByUserId(12);
        System.out.print("User Mini Game - user id = " + userMiniGame.getUserId() + " - ticket = " + userMiniGame.getTicketWheel());

    }

    private FortuneWheelCore wheelCore1;
    //private FortuneWheelCore wheelCore2;

    //Quay
    public void fortuneWheelRequestSpin(ChannelContextInfo ctx) {
        MiniGameProtos.WheelResponseResultSpin.Builder responseSpin = MiniGameProtos.WheelResponseResultSpin.newBuilder();
        if (ctx.checkAuth()) {
            int userId = ctx.getUserId();
            User user = UserService.getInstance().getUser(userId);
            //UserMiniGame userMiniGame = MUserMiniGame.getInstance().getUserMiniGameByUserId(userId);

            if (user.getTicketWheel() >= 1)
            {
                boolean continueSpin = false;
                do {
                    continueSpin = false;
                    FortuneWheelItem item1 = wheelCore1.spin();
                    try {
                        if (item1 != null) {
                            logger.info("Spin success - id = " + item1.getName());
                            boolean rewardSucess = recieveReward(user, item1);
                            if (rewardSucess) {
                                logger.info("Reward spin success");
                                responseSpin.setIdWheel1(item1.getIndex());
                                responseSpin.setErrorCode(REC_SUCCESS);
                                long sessionID = saveWheelHistory(userId, item1.getIndex());
                                responseSpin.setSessionId(sessionID);

                                //log spin
                                if (item1.getCurrency() != 0) {
//                                    logKafkaService.logAction(userId, UserAction.SPIN_WHEEL_RESULT, 0, 0, MoneyType.GOLD, "Wheel:" + item1.getName());
                                }

                                user.subTicketWheel(1);
                                user.saveUserMiniGame();
                            } else {
                                logger.info("Reward spin fail continue spin");
                                continueSpin = true;
                            }

                        } else  {
                            logger.error("User + " + userId + " spin error item null");
                            responseSpin.setErrorCode(REC_NOT_DEFINE);
                        }
                    } catch (Exception e) {
                        logger.error("User + " + userId + " spin error", e);
                        responseSpin.setErrorCode(REC_NOT_DEFINE);
                    }
                } while (continueSpin);

            } else {
                responseSpin.setErrorCode(REC_WHEEL_NOT_ENOUGH_TICKET);
            }
        }
        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.MINI_GAME_COMMAND);
        response.setBeanType(FORTURN_WHEEL_RESPONSE_SPIN);
        response.setData(responseSpin.build().toByteString());

        ctx.writeAndFlush(response.build());
    }

    //Lưu lịch sử vòng quay may mắn lại
    long saveWheelHistory(int userId, int idWheel1) {
        WheelUserHistory wheelUserHistory = new WheelUserHistory();
        wheelUserHistory.setUserId(userId);
        wheelUserHistory.setIdResultWheel1(idWheel1);
        wheelUserHistory.setTimestamp((int)ZonedDateTime.now().toEpochSecond());
        try {
            MWheelUserHistory.getInstance().saveWheelUserHistory(wheelUserHistory);
            return wheelUserHistory.getSessionId();
        } catch (Exception e) {
            logger.error("Save wheel history error - userid = " + userId, e);
        }
        return 0;
    }

    void pushMessageRewardByItem(int userId, String nickName, FortuneWheelItem item) {
        switch (item.getCurrency())
        {
            case 0: //Gold
                PushMessageService.getInstance().pushTextRunFromGame(userId, nickName, item.getItem_reward() + " gold", "Lucky Wheel");

                break;
            case 1: //Silver
                break;
            case 5: // Card 10k
                PushMessageService.getInstance().pushTextRunFromGame(userId, nickName, "card 10k", "Lucky Wheel");
                break;
            case 6://card 20k
                PushMessageService.getInstance().pushTextRunFromGame(userId, nickName, "card 20k", "Lucky Wheel");
                break;
            case 7: //them luot
                PushMessageService.getInstance().pushTextRunFromGame(userId, nickName, item.getItem_reward() + " more ticket", "Lucky Wheel");
                break;
            case -1: // fail
                break;
            default:
        }
    }

    //Nhận thưởng từ vòng quay.
    boolean recieveReward(User user, FortuneWheelItem item) throws NoSuchFieldException, IllegalAccessException {
        logger.info("Recieve Reward spin = " + item.getName() + " - currency = " + item.getCurrency() + " - reward = " + item.getItem_reward());
        String messageToClient = "";

        switch (item.getCurrency()) {
            case 0: //Gold
                Money moneyReward = new Money(item.getItem_reward(), MoneyType.GOLD);
                long moneyBefore = user.getMoneyGold();
                user.SetMoney(moneyReward.getAmount());
                TransactionData transactionData = moneyService.addMoneyToSave(moneyReward, user, true, "Vong_Quay_May_Man");

                PushMessageService.getInstance().pushTextRunFromGame(user.getId(), user.getNickName(), item.getItem_reward() + " gold", "Lucky Wheel");

                //Log nhận tiền từ VQMM
//                logKafkaService.logAction(user.getId(), UserAction.RECIEVE_WHEEL_REWARD, moneyBefore, item.getItem_reward(), MoneyType.GOLD, "Wheel:" + item.getName());
                if (transactionData.getStatus() != TransactionStatus.SUCCESS) {
                    logger.error("Recieve reward error when add money - error code =" + transactionData.getStatus().name());
                }
                break;
            case 1: //Silver
                break;
            case 5: // Card 10k
                //get card và gửi xuống cho nó.
                if (checkRewardCard(user, "card10k")) {
                    PushMessageService.getInstance().pushTextRunFromGame(user.getId(), user.getNickName(), "card 10k", "Lucky Wheel");
                    return cashoutService.addReciept(user.getId(), CashoutType.VQMM, 10000, 1);
                } else {
                    return false;
                }
            case 6://card 20k
                if (checkRewardCard(user, "card20k")) {
                    PushMessageService.getInstance().pushTextRunFromGame(user.getId(), user.getNickName(), "card 20k", "Lucky Wheel");
                    return cashoutService.addReciept(user.getId(), CashoutType.VQMM, 20000, 1);
                } else {
                    return false;
                }
            case 7: //them luot

                PushMessageService.getInstance().pushTextRunFromGame(user.getId(), user.getNickName(), item.getItem_reward() + " more ticket", "Lucky Wheel");
                this.addTicket(user, (int)item.getItem_reward(), AddTicketWheelFrom.WHEEL);
                break;
            case -1: // fail
                break;
            default:
        }
        return true;
    }

    boolean checkRewardCard(User user, String cardType) {
        try {
            IPAddressCheck ipAddressCheck = mipAddressCheck.getIPAddressCheckByIP(user.getIpAddress());
            if (ipAddressCheck.getCountCard() < LIMIT_CARD_REWARD_BY_IP && ipAddressCheck.checkCardOneDay(LIMIT_CARD_ONE_DAY_IP)) {
                ipAddressCheck.addCard(user.getId(), cardType);
                ipAddressCheck.saveDB();
                logger.info("IP - " + ipAddressCheck.getIpAddress() + " - được nhận card - " + cardType + " - count = " + ipAddressCheck.getCountCard());
                return true;
            } else {
                logger.info("IP " + ipAddressCheck.getIpAddress() + " đã đạt limit card - " + ipAddressCheck.getCountCard());
                return false;
            }

        }
        catch (Exception e)
        {
            logger.error("Check reward card error", e);
            return false;
        }

    }

    public void testAccept(int recieptId, String approver) {
        cashoutService.acceptReciept(recieptId, approver);
    }

    public void verifyCard(ChannelContextInfo ctx, int cardId)
    {
        if (ctx.checkAuth())
        {
            try {
                int userId = ctx.getUserId();
                CardType cardType = CardType.VIETTEL;
                if (cardId == CardType.VIETTEL.getId()) {
                    cardType = CardType.VIETTEL;
                } else if (cardId == CardType.MOBI.getId()) {
                    cardType = CardType.MOBI;
                } else if (cardId == CardType.VINA.getId()) {
                    cardType = CardType.VINA;
                }
                int error_code = cashoutService.verifyReciept(userId, CashoutType.VQMM, cardType);
                MiniGameProtos.CardTypeResponseBean.Builder responseBean = MiniGameProtos.CardTypeResponseBean.newBuilder();
                responseBean.setErrorCode(error_code);

                XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
                response.setCommand(ServerCommandConstant.MINI_GAME_COMMAND);
                response.setBeanType(FORTUNR_WHEEN_CARD_VERIFY_RESPONSE_BEAN);
                response.setData(responseBean.build().toByteString());

                ctx.writeAndFlush(response.build());
            } catch (Exception e) {
                logger.error("Verify card error", e);
            }
        }
    }

    public void addTicket(User user, int ticket, AddTicketWheelFrom addTicketWheelFrom) throws NoSuchFieldException, IllegalAccessException {
        user.addTicketWheel(ticket);
        user.saveUserMiniGame();
        updateReportAddTicket(ticket, addTicketWheelFrom);
    }

    public  void addTicketByUID(int userId, int ticket, AddTicketWheelFrom addTicketWheelFrom)
    {
        try {
            User user = UserService.getInstance().getUser(userId);
            if (user != null) {
                user.addTicketWheel(ticket);
                user.saveUserMiniGame();
            } else {
                UserMiniGame userMiniGame = MUserMiniGame.getInstance().getUserMiniGameByUserId(userId);
                userMiniGame.setTicketWheel(userMiniGame.getTicketWheel() + ticket);
                userMiniGame.saveDB();
            }
            updateReportAddTicket(ticket, addTicketWheelFrom);
        } catch (Exception e) {
            logger.error("Fortune wheel service ADD ticket error", e);
        }

    }

    void updateReportAddTicket(int ticket, AddTicketWheelFrom addTicketWheelFrom)
    {
        switch (addTicketWheelFrom) {
            case WHEEL:
                reportMinigame.setTicketWheel(reportMinigame.getTicketWheel() + ticket);
                break;
            case GIFT_CODE:
                reportMinigame.setTicketGiftCode(reportMinigame.getTicketGiftCode() + ticket);
                break;
            case PROMOTE_CODE:
                reportMinigame.setTicketPromoteCode(reportMinigame.getTicketPromoteCode() + ticket);
                break;
            case LOGIN:
                reportMinigame.setTicketLogin(reportMinigame.getTicketLogin() + ticket);
                break;
            case ADMIN:
                reportMinigame.setTicketAdmin(reportMinigame.getTicketAdmin() + ticket);
                break;
        }
        try {
            mReportMinigame.saveToMongo(reportMinigame);
        } catch (Exception e) {
            logger.error("Save report mini game error - day = " + reportMinigame.getDay(), e);
        }
    }

    //Gọi khi user lấy thông tin của vòng quay
    //Trả về các item trong vòng quay.
    //Số lượt quay hiện tại của họ
    public void fortuneWheelRequestInfo(ChannelContextInfo ctx) {
        int userId = ctx.getUserId();
        UserMiniGame userMiniGameMM = MUserMiniGame.getInstance().getUserMiniGameByUserId(userId);
        int ticketWheel = userMiniGameMM.getTicketWheel();
        MiniGameProtos.WheelResponseInfo.Builder responseWheelInfo = MiniGameProtos.WheelResponseInfo.newBuilder();
        responseWheelInfo.setTicketWheel(ticketWheel);

        for (MiniGameProtos.WheelItemInfoBean wheelItemInfoBean : wheelCore1.toListWheelItemBuilder()) {
            responseWheelInfo.addListItem(wheelItemInfoBean);
        }
        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.MINI_GAME_COMMAND);
        response.setBeanType(FORTURN_WHEEL_RESPONSE_INFO);
        response.setData(responseWheelInfo.build().toByteString());

        ctx.writeAndFlush(response.build());
    }

    //Lịch sử vòng quay của user
    public void fortuneWheelRequestHistory(ChannelContextInfo ctx) {
        int userId = ctx.getUserId();
        MiniGameProtos.WheelUserHistoryResponse.Builder responseHistory = MiniGameProtos.WheelUserHistoryResponse.newBuilder();

        List<WheelUserHistory> userHistoryList = MWheelUserHistory.getInstance().getListWheelHistoryByUserId(userId,LIMIT_COUNT_WHEEL_HISTORY);
        for (WheelUserHistory wheelHistory : userHistoryList) {
            MiniGameProtos.WheelUserHistoryData.Builder dataHistory = MiniGameProtos.WheelUserHistoryData.newBuilder();
            dataHistory.setSessionId(wheelHistory.getSessionId());
            dataHistory.setTime((int)ZonedDateTime.now().toEpochSecond());
            dataHistory.setIdWheel1(wheelHistory.getIdResultWheel1());
            responseHistory.addListWheelHistory(dataHistory.build());
        }
        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.MINI_GAME_COMMAND);
        response.setBeanType(FORTURN_WHEEL_RESPONSE_HISTORY);
        response.setData(responseHistory.build().toByteString());

        ctx.writeAndFlush(response.build());
    }
}
