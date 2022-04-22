package games.minigame.giftcode;

import games.core.application.ServerConfig;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.*;
import games.core.constant.ServerCommandConstant;
import games.core.database.document.UserMiniGame;
import games.core.database.model.MUserMiniGame;
import games.core.user.User;
import games.core.user.UserCommand;
import games.core.user.UserService;
import games.features.inbox.InboxService;
import games.minigame.constant.AddTicketWheelFrom;
import games.minigame.constant.ResponseBeanMiniGameConst;
import games.minigame.constant.ResponseMiniGameErrorCode;
import games.minigame.database.document.GiftCode;
import games.minigame.database.model.MGiftCode;
import games.minigame.wheel.FortuneWheelService;
import libs.util.LogFactory;
import message.MiniGameProtos;
import message.XProtos;
import org.apache.log4j.Logger;

import java.time.ZonedDateTime;

/**
 * Created by tuanhoang on 8/17/17.
 */
public class GiftCodeService implements ResponseMiniGameErrorCode, ResponseBeanMiniGameConst {
    private static GiftCodeService instance;
    private static Logger logger;
    private MGiftCode mGiftCode;
    private MoneyService moneyService;
    private FortuneWheelService fortuneWheelService;
    public static GiftCodeService getInstance() {
        if (instance == null) instance = new GiftCodeService();
        return instance;
    }

    private long moneyRewardPromoteCode = 0;
    private int maxCountFriendPromoteCode = 0;
    private int ticketWheelPromoteCode = 0;

    private GiftCodeService(){
        moneyService = MoneyService.getInstance();
        fortuneWheelService = FortuneWheelService.getInstance();

        mGiftCode = MGiftCode.getInstance();
        moneyRewardPromoteCode = Long.parseLong(ServerConfig.getInstance().getMoneyPromoteCode());

        maxCountFriendPromoteCode = Integer.parseInt(ServerConfig.getInstance().getMaxFriendPromoteCode());

        ticketWheelPromoteCode = Integer.parseInt(ServerConfig.getInstance().getTicketWheelPromoteCode());
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    public void usePromoteCode(ChannelContextInfo ctx, String code) {
        if (ctx.checkAuth()) {
            MiniGameProtos.PromoteCodeResponseBean.Builder codeResponseBean = MiniGameProtos.PromoteCodeResponseBean.newBuilder();
            try {
                int userId = ctx.getUserId();
                User user = UserService.getInstance().getUser(userId);
                if (!user.isEnterCodeFriend()) {
                    UserMiniGame userMiniGame = MUserMiniGame.getInstance().getUserMiniGameByPromoteCode(code);
                    if (userMiniGame != null) {
                        //other friend
                        int friendId = userMiniGame.getUserId();
                        int countFriendPromoteCode = userMiniGame.getCountFriendPromoteCode();
                        if (countFriendPromoteCode < maxCountFriendPromoteCode) {
                            StringBuilder message = new StringBuilder();
                            message.append("Người chơi ").append(userId).append(" đã nhập mã giới thiệu của bạn.\n");
                            message.append("Phần thưởng của bạn là: ");


                            if (moneyRewardPromoteCode > 0) {
                                message.append("\n\t ").append(moneyRewardPromoteCode).append(" vàng.");
                                Money moneyReward = new Money(moneyRewardPromoteCode, MoneyType.GOLD);
                                moneyService.addMoneyS(moneyReward, friendId, "Ma_Gioi_Thieu");
                            }

                            if (ticketWheelPromoteCode > 0) {
                                fortuneWheelService.addTicketByUID(friendId, ticketWheelPromoteCode, AddTicketWheelFrom.PROMOTE_CODE);
                                message.append("\n\t ").append(ticketWheelPromoteCode).append(" lượt quay.");
                            }
                            message.append("\n Xin chúc mừng");
                            InboxService.getInstance().sendMessage(InboxService.SYSTEM_ID, friendId, " Phần quà giới thiệu", message.toString());
                            UserCommand.getInstance().forceUpdateMoneyToUser(friendId, null);

                            userMiniGame.setCountFriendPromoteCode(countFriendPromoteCode + 1);
                            userMiniGame.saveDB();
                        }


                        user.setEnterCodeFriend(true);
                        user.saveUserMiniGame();
                        codeResponseBean.setErrorCode(REC_SUCCESS);
                    } else {
                        codeResponseBean.setErrorCode(REC_PROMOTE_CODE_INVALID);
                    }
                } else {
                    codeResponseBean.setErrorCode(REC_PROMOTE_YOU_ENTERD_FIEND_CODE);
                }
            } catch (Exception e) {
                codeResponseBean.setErrorCode(REC_NOT_DEFINE);
            }

            XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
            response.setCommand(ServerCommandConstant.MINI_GAME_COMMAND);
            response.setBeanType(PROMOTE_CODE_USE_RESPONSE);
            response.setData(codeResponseBean.build().toByteString());
            ctx.writeAndFlush(response.build());
        }
    }

    public void useGiftCode(ChannelContextInfo ctx, String code) {
        if (ctx.checkAuth()) {
            int userId = ctx.getUserId();
            User user = UserService.getInstance().getUser(userId);
            code = code.toUpperCase();
            try{
                GiftCode giftCode = mGiftCode.getGifeCodeByCode(code);
                MiniGameProtos.GiftCodeResponseUse.Builder responseGiftCode = MiniGameProtos.GiftCodeResponseUse.newBuilder();

                if (giftCode != null) {
                    if (!giftCode.isUsed()) {
                        if (!mGiftCode.checkUsedCodeWithName(userId, giftCode.getName())) {
                            responseGiftCode.setErrorCode(REC_SUCCESS);
                            StringBuilder messageToClient = new StringBuilder();
                            messageToClient.append("You have successfully received the Giftcode:");

                            long moneyBefore = user.getMoneyGold();

                            if (giftCode.getGoldReward() > 0) {
                                Money goldReward = new Money(giftCode.getGoldReward(), MoneyType.GOLD);
                                user.SetMoney(goldReward.getAmount());
                                TransactionData transactionData = moneyService.addMoneyToSave(goldReward, user, true, "GiftCode");
                                if (transactionData.getStatus() == TransactionStatus.SUCCESS) {
                                    messageToClient.append("\n");
                                    messageToClient.append(giftCode.getGoldReward());
                                    messageToClient.append(" gold.");

                                    responseGiftCode.setGoldReward(giftCode.getGoldReward());
                                } else {
                                    responseGiftCode.setErrorCode(REC_NOT_DEFINE);
                                }
                            }
                            if (giftCode.getTicketWheel() > 0) {
                                FortuneWheelService.getInstance().addTicket(user, giftCode.getTicketWheel(), AddTicketWheelFrom.GIFT_CODE);
                                messageToClient.append("\n");
                                messageToClient.append(giftCode.getTicketWheel());
                                messageToClient.append(" lượt quay VQMM.");

                                responseGiftCode.setTicketWheel(giftCode.getTicketWheel());
                            }
                            if (responseGiftCode.getErrorCode() == REC_SUCCESS) {
                                messageToClient.append("\n Congratulations.");
                                //responseGiftCode.setMessage(messageToClient.toString());
                                giftCode.setUsed(true);
                                giftCode.setUserId(userId);
                                giftCode.setTimeActive(ZonedDateTime.now().toEpochSecond());

                                try {
                                    giftCode.saveDB();
                                    logger.info("Update gift code success");
                                } catch (IllegalAccessException e) {

                                    logger.error("Update giftcode error", e);
                                }
                            }

                        } else {
                            responseGiftCode.setErrorCode(REC_GIFTCODE_YOU_USED_CODE_SAME_NAME);
                        }
                    } else {
                        responseGiftCode.setErrorCode(REC_GIFTCODE_USED);
                    }
                } else {
                    responseGiftCode.setErrorCode(REC_GIFTCODE_DOESNT_EXIST);
                    logger.info("User " + userId + " - use  code = " + code + " error-, code doesn't exits");
                }
                XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder().setCommand(ServerCommandConstant.MINI_GAME_COMMAND)
                        .setBeanType(GIFT_CODE_RESPONSE_USE)
                        .setData(responseGiftCode.build().toByteString());
                ctx.writeAndFlush(response.build());
            }catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
