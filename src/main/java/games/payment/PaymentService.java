package games.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.ByteString;
import games.core.application.ServerConfig;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.data.DataCommonManager;
import games.core.common.money.*;
import games.core.constant.ServerCommandConstant;
import games.core.user.User;
import games.core.user.UserService;
import games.features.inbox.InboxService;
import games.payment.constant.*;
import games.payment.database.document.DPaymentHistory;
import games.payment.database.model.MPaymentHistory;
import games.payment.object.*;
import kafka.UserAction;
import libs.util.*;
import message.ShopProtos;
import message.XProtos;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static games.payment.constant.PaymentConst.*;

/**
 * Created by tuanhoang on 8/24/17.
 */
public class PaymentService implements ResponseBeanPaymentErrorCode, ResponseBeanPaymentConst{
    private static PaymentService instance;
    private static Logger logger;

//    private PaymentXBomService paymentXBomService;
    private UserService userService;
    private MoneyService moneyService;
    public static PaymentService getInstance() {
        if (instance == null) instance = new PaymentService();
        return instance;
    }

    private long moneyRewardVideoAds = 0;
    private int maxCountVideoOnceDay = 0;


    PaymentService()
    {
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        userService = UserService.getInstance();
        moneyService = MoneyService.getInstance();
        //paymentXBomService = PaymentXBomService.getInstance();

        moneyRewardVideoAds = Long.parseLong(ServerConfig.getInstance().getMoneyRewardVideoAds());
        maxCountVideoOnceDay = Integer.parseInt(ServerConfig.getInstance().getMaxCountVideoAds());

        logger.info("Payment service init");
    }

    public static void main(String[] args) {

    }

    private int getCardInStatus(int verifyStatus){

        switch (verifyStatus){
            case 1:
                return DPaymentHistory.SUCCESS;
            case 2:
            case 4:
                return DPaymentHistory.REJECT;
            default:
                return DPaymentHistory.PENDING;
        }

    }

    //xu ly goi api nap card
    public void cardPayment(ChannelContextInfo ctx, ShopProtos.ProcessPaymentCardRequestBean requestBean)

    {
        if (ctx.checkAuth())
        {
            int errorCode = REC_SUCCESS;
            try
            {
                int userId = ctx.getUserId();
                User user = userService.getUser(userId);
                if (user != null)
                {
                    UserCardRequest userCardRequest = new UserCardRequest();

                    CardType cardType = CardType.getById(requestBean.getCardType());
                    userCardRequest.setAccountName("" + userId);
                    userCardRequest.setCardCode(requestBean.getCardCode());
                    userCardRequest.setCardSerial(requestBean.getCardSerial());
                    userCardRequest.setCardType(cardType.getValue());
                    userCardRequest.setCardTypeId(requestBean.getCardType());
                    PaymentItem paymentItem1 = DataCommonManager.getInstance().getPaymentItem(requestBean.getPackage());
                    userCardRequest.setCardValue(paymentItem1.getValue());

                    logger.info("Request Package ID: " + requestBean.getPackage());
                    logger.info("Request " + paymentItem1.toString());
                    logger.info(userCardRequest.toString());

                    NewUserCardResponse response = PaymentCardService.getInstance().requestCard(userId, userCardRequest);

                    logger.info("NewUserCardResponse: " + response.toString());


                    int verifyStatusState = getCardInStatus(response.getVerifyStatus());

                    DPaymentHistory paymentHistory = MPaymentHistory.getInstance().logCardIn(
                            user.getMoneyGold(), 0, userId,
                            cardType.name(), paymentItem1.getValue(),
                            requestBean.getCardCode(), requestBean.getCardSerial(),
                            String.valueOf(response.getStatus()), String.valueOf(response.getVerifyStatus()),
                            null, verifyStatusState
                    );

                    if (response.getCode() == 200)
                    {
                        if(verifyStatusState == DPaymentHistory.SUCCESS)
                        {
                            //sucess
                            int amount = (int)response.getPrice();
                            if (amount == -1){
                                amount = userCardRequest.getCardValue();
                            }
                            logger.info("Card payment success - " + amount);
                            PaymentItem paymentItem = DataCommonManager.getInstance().getPaymentItem(PaymentActionType.RECHARGE, amount);
                            if (paymentItem != null) {
                                logger.info("Response " + paymentItem.toString());
                                this.processPaymentSuccess(user, PaymentType.CARD, amount, paymentItem.getRealValue(), MoneyType.GOLD, 0);
                                paymentHistory.setValue(paymentItem1.getRealValue());
                                paymentHistory.setBeforeValue(paymentHistory.getBeforeValue());
                                paymentHistory.setStatus(DPaymentHistory.SUCCESS);
                                paymentHistory.saveDB();
                            } else {
                                logger.info("Card payment error - payment item null ");
                            }

                            return;
                        }
                        else
                        {
                            logger.info("Card payment error card status = " +response.getStatus()+ " - verify status = " + response.getVerifyStatus());
                            errorCode = REC_NOT_DEFINE;
                            switch (response.getVerifyStatus()) {
                                case 2:
                                    errorCode = REC_CARD_CODE_WRONG;
                                    break;
                                case 3:
                                    errorCode = REC_CARD_SYSTEM_ERROR;
                                    break;
                                case 4:
                                    errorCode = REC_CARD_CODE_OR_SERIAL_INVALID;
                                    break;
                                case 5:
                                case 7:
                                    errorCode = REC_CARD_TIME_OUT;
                                    break;
                            }
                        }
                    }
                    else
                    {
                        errorCode = REC_NOT_DEFINE;
                        logger.info("Payment card error code = " + response.getCode());
                        switch (response.getCode()) {
                            case -323:
                                errorCode = REC_CARD_CODE_WRONG;
                                break;
                            case -330:
                                errorCode = REC_CARD_USED;
                                break;
                            case -331:
                                errorCode = REC_CARD_LOCKED;
                                break;
                            case -332:
                                errorCode = REC_CARD_TIME_OUT;
                                break;
                            case -333:
                                errorCode = REC_CARD_NOT_ACTIVE;
                                break;
                            case -334:
                                errorCode = REC_CARD_CODE_OR_SERIAL_INVALID;
                                break;
                            case -335:
                                errorCode = REC_CARD_CODE_OR_SERIAL_INVALID_2;
                                break;
                            case -301:
                                errorCode = REC_CARD_SYSTEM_ERROR;
                                break;
                            case -49:
                                errorCode = REC_CARD_LOCK_ACCOUNT;
                                break;
                        }
                    }
                } else {
                    errorCode = REC_USER_NOT_FOUND;
                }
            }
            catch (Exception e) {
                logger.error("CardPayment error", e);
                errorCode = REC_NOT_DEFINE;
            }
            processPaymentError(ctx, PaymentType.CARD, errorCode);
        }
    }

    public void applePayment(ChannelContextInfo ctx, ShopProtos.ProcessPaymentAppleRequestBean requestBean) {
        if (ctx.checkAuth()) {
            int userId = ctx.getUserId();
            int error_code = 0;
            try {

                User user = userService.getUser(userId);
                if (user != null) {
                    String paymentUrl = PaymentConst.LINK_APPLE_VERIFY;
                    boolean toContinue;
                    boolean isTest = false;
                    boolean sandbox = false;
                    String reciept = encodeByteString(requestBean.getContentData());
                    Map<String, String> map = new HashMap<>();
                    map.put("receipt-data", reciept);

                    do {
                        toContinue = false;
                        SmileeHttpResponse response = HttpUtil.callPOSTWithJSON(paymentUrl, map, false);
                        if (response.getStatus() == ServerConstant.HTTP_STATUS.SUCCESS) {

                            ApplePaymentVerifyData verifyData = (ApplePaymentVerifyData) JsonUtil.parseJsonIgnoreUnknown(response.getData(), new TypeReference<ApplePaymentVerifyData>() {});
                            logger.info("Verify apple with status = " + verifyData.status);
                            if (verifyData.status == STATUS_APPLE_SANDBOX_TEST) {
                                logger.info("Verify with test mode");
                                paymentUrl = LINK_APPLE_VERIFY_TEST;
                                toContinue = true;
                                isTest = true;
                            } else if (verifyData.status == PAYMENT_SUCCESS) {
                                if (verifyData.environment.compareToIgnoreCase("Sandbox") == 0 || isTest)
                                    sandbox = true;

                                if (verifyData.receipt != null && verifyData.receipt.in_app != null && verifyData.receipt.in_app.size() > 0) {
                                    ApplePaymentVerifyInApp applePaymentVerifyInApp = verifyData.receipt.in_app.get(0);

                                    if (applePaymentVerifyInApp != null) {
                                        String productId = applePaymentVerifyInApp.product_id;
                                        String transactionId = applePaymentVerifyInApp.transaction_id;
                                        long amount = getAmountIapAppleByProductId(productId);
                                        if (amount > 0) {
                                            //Nếu payment thành công thì gửi response về ở ProcessPayment Success
                                            processPaymentSuccess(user, PaymentType.IAP_APPLE,amount, amount, MoneyType.GOLD, 0);
                                            return;
                                        } else {
                                            error_code = REC_PRODUCT_ID_ERROR;
                                            logger.info("Payment google error bc Product id not found");
                                        }

                                    } else {
                                        logger.info("Apple payment in app error");
                                        error_code = REC_VERIFY_ERROR;
                                    }
                                } else {
                                    logger.info("Verify data error");
                                    error_code = REC_VERIFY_ERROR;
                                }
                            }
                        } else {
                            error_code = REC_NOT_DEFINE;
                        }
                    } while (toContinue);
                } else {
                    error_code = REC_USER_NOT_FOUND;
                }
            } catch (Exception e) {
                logger.error("Payment apple error", e);
                error_code = REC_NOT_DEFINE;
            }
            //Đã có lỗi xảy ra. Thông báo về cho client
            processPaymentError(ctx, PaymentType.IAP_APPLE, error_code);
        } else {
            logger.error("Payment error, user not author");
        }
    }

    public void googlePayment(ChannelContextInfo ctx, ShopProtos.ProcessPaymentGoogleRequestBean requestBean) {
        if (ctx.checkAuth()) {
            int errorCode = 0;
            try {
                int userId = ctx.getUserId();
                User user = userService.getUser(userId);
                if (user != null) {
                    String itemType = requestBean.getItemtype();
                    String signature = requestBean.getSignature();
                    String jsonData = requestBean.getJsondata();

                    boolean verifyPurchase = GooglePaymentSecurity.verifyPurchase(PaymentConst.GOOGLE_PUBLIC_KEY, jsonData, signature);
                    if (verifyPurchase) {
                        GooglePurchaseData data = new GooglePurchaseData(itemType, jsonData, signature);
                        String productId = data.getSku();

                        long amount = getAmountIapGoogleByProductId(productId);
                        if (amount > 0) {
                            processPaymentSuccess(user, PaymentType.IAP_GOOGLE,amount, amount, MoneyType.GOLD, 0);
                            return;
                        } else {
                            errorCode = REC_PRODUCT_ID_ERROR;
                            logger.info("Payment google error bc Product id not found");
                        }
                    } else {
                        errorCode = REC_VERIFY_ERROR;
                    }
                } else {
                    errorCode = REC_USER_NOT_FOUND;
                }
            } catch (Exception e) {
                logger.error("Payement google error", e);
                errorCode = REC_NOT_DEFINE;
            }
            processPaymentError(ctx, PaymentType.IAP_GOOGLE, errorCode);
        } else {
            logger.error("Payment error, user not author");
        }
    }

    public void videoAdsPayment(ChannelContextInfo ctx) {
        if (ctx.checkAuth()) {
            int errorCode = REC_SUCCESS;
            try {
                int userID = ctx.getUserId();
                User user = userService.getUser(userID);
                int countVideoAdsToday = user.getCountRecieveVideoAds();
                if (countVideoAdsToday < maxCountVideoOnceDay) {
                    user.addCountRecieveVideoAds(1);
                    user.saveUserMiniGame();

                    //từ video thì chỉ có tiền quảng cáo thôi. nên amount = 0
                    processPaymentSuccess(user, PaymentType.VIDEO_ADS,0, moneyRewardVideoAds, MoneyType.GOLD, 0);
                    return;
                } else {
                    errorCode = REC_MAX_VIDEO_ONCEDAY;
                }
            } catch (Exception e) {
                errorCode = REC_NOT_DEFINE;
            }
            processPaymentError(ctx, PaymentType.VIDEO_ADS, errorCode);
        }
    }

    public void processPaymentSuccess(User user, PaymentType paymentType, long amount, long moneyReward, MoneyType moneyType, float promotion) {
        logger.info("Payment sucess - user id = " + user.getId() + " - payment type = " + paymentType + " - Amount = "+ amount + " - moneyReward = " + moneyReward);
        ShopProtos.ProcessPaymentIAPResponseBean.Builder iapResponseBean = ShopProtos.ProcessPaymentIAPResponseBean.newBuilder();
        iapResponseBean.setErrorCode(REC_SUCCESS);

        StringBuilder messageToClient = new StringBuilder();
        messageToClient.append("Chúc mừng bạn đã nhận ");
        messageToClient.append(moneyReward);
        messageToClient.append(" gold, từ ");
        UserAction userAction = UserAction.PAYMENT_APPLE;
        String content = "processPaymentSuccess";
        switch (paymentType)
        {
            case IAP_APPLE:
                userAction = UserAction.PAYMENT_APPLE;
                messageToClient.append(" thanh toán apple store.");
                content = "Thanh_Toan_App_Store";
                break;
            case IAP_GOOGLE:
                userAction = UserAction.PAYMENT_GOOGLE;
                messageToClient.append(" thanh toán play store.");
                content = "Thanh_Toan_Play_Store";
                break;
            case VIDEO_ADS:
                userAction = UserAction.PAYMENT_VIDEO_ADS;
                messageToClient.append(" xem quảng cáo video.\n");
                content = "Thanh_Toan_Do_Xem_QC";
                break;
            case CARD:
                userAction = UserAction.PAYMENT_CARD;
                messageToClient.append(" thanh toán thẻ cào.\n");
                content = "Thanh_Toan_The_Cao";
                break;
        }
        long moneyBefore = user.getMoneyGold();
        Money moneyAdd = new Money(moneyReward, moneyType);

        //add tiền
        user.SetMoney(moneyAdd.getAmount());
        TransactionData transactionData = moneyService.addMoneyToSave(moneyAdd, user, true, content);
        if (transactionData.getStatus() == TransactionStatus.SUCCESS)
        {
            if (promotion > 0)
            {
                long moneyPromotion = (long) (promotion * moneyReward);
                Money moneyAddPromotion = new Money(moneyPromotion, moneyType);
                //add money
                user.SetMoney(moneyAddPromotion.getAmount());
                TransactionData transactionDataPromotion = moneyService.addMoneyToSave(moneyAddPromotion, user, true, content);
                if (transactionDataPromotion.getStatus() == TransactionStatus.SUCCESS) {
                    messageToClient.append(" Và nhận thêm ");
                    messageToClient.append(moneyPromotion);
                    messageToClient.append(" vàng khuyến mãi.");
                } else {
                    logger.info("Save money promotion error");
                }
            }
            InboxService.getInstance().sendMessage(InboxService.SYSTEM_ID, user.getId(), "Thanh toán thành công", messageToClient.toString());
            iapResponseBean.setMessage(messageToClient.toString());

        } else {
            logger.info("Save money error");
            iapResponseBean.setErrorCode(REC_NOT_DEFINE);
        }

        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.PAYMENT_COMMAND);
        response.setBeanType(PAYMENT_RESPONSE_BEAN);
        response.setData(iapResponseBean.build().toByteString());
        if (user.getCtx() != null) {
            user.getCtx().writeAndFlush(response.build());
        } else {
            logger.error("Payment sucess but ctx null, userid = " + user.getId());
        }
    }

    public void processPaymentError(ChannelContextInfo ctx, PaymentType paymentType, int errorCode) {
        ShopProtos.ProcessPaymentErrorBean.Builder paymentError = ShopProtos.ProcessPaymentErrorBean.newBuilder();
        paymentError.setErrorCode(errorCode);
        paymentError.setPaymentType(paymentType.getValue());

        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.PAYMENT_COMMAND);
        response.setBeanType(PAYMENT_ERROR_RESPONSE_BEAN);
        response.setData(paymentError.build().toByteString());

        ctx.writeAndFlush(response.build());
    }

    //endcode byte string Receip Apple payment
    private String encodeByteString(ByteString contentData) {
        Base64 encoder = new Base64();
        String encodedReceipt = new String(encoder.encode(contentData.toByteArray()));
        return encodedReceipt;
    }

    private long getAmountIapAppleByProductId(String productId) {
        switch (productId) {
            case "com.noob.fish.sanca.tier1":
                return 100000;
            case "com.noob.fish.sanca.tier2":
                return 200000;
            case "com.noob.fish.sanca.tier5":
                return 600000;
            case "com.noob.fish.sanca.tier10":
                return 1600000;
        }

        return 0;
    }

    private long getAmountIapGoogleByProductId(String productId) {
        switch (productId) {
            case "com.noob.fish.sanca.tier1":
                return 100000;
            case "com.noob.fish.sanca.tier2":
                return 200000;
            case "com.noob.fish.sanca.tier5":
                return 600000;
            case "com.noob.fish.sanca.tier10":
                return 1600000;
        }

        return 0;
    }
}

