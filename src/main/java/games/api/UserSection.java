package games.api;

import games.core.application.channel.ChannelService;
import games.core.common.money.*;
import games.core.database.document.UserMiniGame;
import games.core.user.User;
import games.core.user.UserCommand;
import games.core.user.UserService;
import games.minigame.constant.AddTicketWheelFrom;
import games.minigame.wheel.FortuneWheelService;
import games.payment.database.model.MPaymentHistory;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserSection implements IApiSection
{

    private Logger logger;
    MoneyService moneyService;
    ChannelService channelService;

    UserService userService;

    private static UserSection instance;
    public static UserSection getInstance() {
        if (instance == null) instance = new UserSection();
        return instance;
    }

    UserSection()
    {
        moneyService = MoneyService.getInstance();
        userService = UserService.getInstance();
        channelService = ChannelService.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    @Override
    public ApiReturnObject process(String action, Map<String, Object> data)
    {
        ApiReturnObject apiReturnObject = new ApiReturnObject();
        switch (action){
            case GET_CCU:
                apiReturnObject.setData(getCCU());
                break;
            case ADD_GOLD:
                try
                {
                    int userId = Integer.parseInt((String) data.get("user_id"));
                    long qty = Long.parseLong((String) data.get("qty"));
                    addGold(userId, qty);
                }
                catch (Exception e)
                {
                    logger.error("Add gold error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case SUB_GOLD:
                try
                {
                    int userId = Integer.parseInt((String) data.get("user_id"));
                    long qty = Long.parseLong((String) data.get("qty"));
                    subGold(userId, qty);
                }
                catch (Exception e)
                {
                    logger.error("Sub gold error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case ADD_SILVER:
                try{
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    long qty = Long.valueOf((String) data.get("qty"));
                    addSilver(userId, qty);
                } catch (Exception e){
                    logger.error("Add Silver error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case SUB_SILVER:
                try{
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    long qty = Long.valueOf((String) data.get("qty"));
                    subSilver(userId, qty);
                } catch (Exception e){
                    logger.error("Sub silver error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case ADD_WHEEL:
                try{
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    int qty = Integer.valueOf((String) data.get("qty"));
                    addWheel(userId, qty);
                } catch (Exception e){
                    logger.error("Add wheel error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case SUB_WHEEL:
                try{
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    int qty = Integer.valueOf((String) data.get("qty"));
                    subWheel(userId, qty);
                } catch (Exception e){
                    logger.error("Sub wheel error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case ADD_TRIAL:
                try{
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    int qty = Integer.valueOf((String) data.get("qty"));
                    addTrial(userId, qty);
                } catch (Exception e){
                    logger.error("Add trial error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case SUB_TRIAL:
                try{
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    int qty = Integer.valueOf((String) data.get("qty"));
                    subTrial(userId, qty);
                } catch (Exception e){
                    logger.error("Sub trials error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;

            case BANS:
                try {
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    bansUser(userId);
                } catch (Exception e){
                    logger.error("Bans user error: ", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;

            case UN_BANS:
                try {
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    unBansUser(userId);
                } catch (Exception e){
                    logger.error("Un bans user error: ", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
        }
        return apiReturnObject;
    }

    private HashMap<String, Object> getCCU(){
        HashMap<String, Object> reportCCU = new HashMap<>();
        reportCCU.put("ccu", channelService.getCCU());
        return reportCCU;
//        reportCCU.put("ccu_in_game", channelService.getCCU_IN_GAME());
//        try{
//            reportCCU.put("player_in_trial", GameService.getInstance().getPlayerInRoom(RoomType.TRIAL));
//            reportCCU.put("player_in_normal", GameService.getInstance().getPlayerInRoom(RoomType.NORMAL));
//            reportCCU.put("player_in_vip", GameService.getInstance().getPlayerInRoom(RoomType.VIP));
//            return reportCCU;
//        } catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }

    }

    private void subGold(int userId, long qty) throws Exception {
        //TODO tools tru gold
        Money moneySub = new Money(qty, MoneyType.GOLD);
        TransactionData transactionData = moneyService.subMoneyFromTool(moneySub, userId, true, true);
        if (transactionData.getStatus() == TransactionStatus.SUCCESS) {
            long moneyAfter = transactionData.getRemainingAmount().getAmount();
            long moneyBefore = moneyAfter + qty;
            MPaymentHistory.getInstance().logAdminTransfer(moneyBefore, -qty, userId, "Admin sub");
        }
        UserCommand.getInstance().forceUpdateMoneyToUser(userId, null);
    }

    private void addGold(int userId, long qty) throws Exception {
        //TODO tools cong gold
        Money moneyAdd = new Money(qty, MoneyType.GOLD);

        TransactionData transactionData = moneyService.addMoneyS(moneyAdd, userId, "FromAdmin");
        if (transactionData.getStatus() == TransactionStatus.SUCCESS)
        {
            long moneyAfter = transactionData.getRemainingAmount().getAmount();
            long moneyBefore = moneyAfter - qty;
            MPaymentHistory.getInstance().logAdminTransfer(moneyBefore, qty, userId, "Admin add");
        }
        UserCommand.getInstance().forceUpdateMoneyToUser(userId, null);
    }
    private void addSilver(int userId, long qty) throws Exception {
        //TODO tools cong silver
        Money moneyAdd = new Money(qty, MoneyType.SILVER);
        moneyService.addMoneyS(moneyAdd, userId, "Silver_From_Admin");
        UserCommand.getInstance().forceUpdateMoneyToUser(userId, null);
    }
    private void subSilver(int userId, long qty) throws Exception {
        //TODO tools tru silver
        Money moneySub = new Money(qty, MoneyType.SILVER);
        moneyService.subSilverMoney(moneySub, userId, true);
        UserCommand.getInstance().forceUpdateMoneyToUser(userId, null);
    }
    private void addWheel(int userId, int qty) throws IllegalAccessException, NoSuchFieldException
    {
        //TODO tools cong wheel
        FortuneWheelService.getInstance().addTicketByUID(userId, qty, AddTicketWheelFrom.ADMIN);
//        User user = userService.getUser(userId);
//        if (user != null) {
//            user.addTicketWheel(qty);
//            user.saveUserMiniGame();
//        } else {
//            UserMiniGame userMiniGame = new UserMiniGame(userId).updateDB();
//            if (userMiniGame != null) {
//                userMiniGame.setTicketWheel(userMiniGame.getTicketWheel() + qty);
//                userMiniGame.saveDB();
//            }
//        }
    }
    private void subWheel(int userId, int qty) throws NoSuchFieldException, IllegalAccessException
    {
        //TODO tools tru wheel
        FortuneWheelService.getInstance().addTicketByUID(userId, -qty, AddTicketWheelFrom.ADMIN);
//        User user = userService.getUser(userId);
//        if (user != null) {
//            user.subTicketWheel(qty);
//            user.saveUserMiniGame();
//        } else {
//            UserMiniGame userMiniGame = new UserMiniGame(userId).updateDB();
//            if (userMiniGame != null) {
//                userMiniGame.setTicketWheel(userMiniGame.getTicketWheel() - qty);
//                userMiniGame.saveDB();
//            }
//        }
    }
    private void addTrial(int userId, int qty) throws NoSuchFieldException, IllegalAccessException {
        //TODO tools cong trial
        User user = userService.getUser(userId);
        if (user != null)
        {
            user.setNumOfTrial(user.getNumOfTrial() + qty);
            user.saveUserMiniGame();
        }
        else
        {
            UserMiniGame userMiniGame = new UserMiniGame(userId).updateDB();
            if (userMiniGame != null) {
                userMiniGame.setNumOfTrial(userMiniGame.getNumOfTrial() + qty);
                userMiniGame.saveDB();
            }
        }
    }
    private void subTrial(int userId, int qty) throws NoSuchFieldException, IllegalAccessException
    {
        //TODO tools tru trial
        User user = userService.getUser(userId);
        if (user != null)
        {
            user.subNumOfTrial(qty);
            user.saveUserMiniGame();
        }
        else
        {
            UserMiniGame userMiniGame = new UserMiniGame(userId).updateDB();
            if (userMiniGame != null) {
                userMiniGame.setNumOfTrial(userMiniGame.getNumOfTrial() - qty);
                userMiniGame.saveDB();
            }
        }
    }

    private void bansUser(int userId) throws NoSuchFieldException, IllegalAccessException {
        User user = userService.getUser(userId);
        user.bans();
    }

    private void unBansUser(int userId) throws NoSuchFieldException, IllegalAccessException {
        User user = userService.getUser(userId);
        user.unBans();
    }

}
