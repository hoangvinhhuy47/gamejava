package games.cashout;

import games.cashout.constant.CardType;
import games.cashout.constant.CashoutType;
import games.cashout.constant.ResponseCashoutErrorCode;
import games.cashout.database.document.Card;
import games.cashout.database.document.CashoutReciept;
import games.cashout.database.model.MCard;
import games.cashout.database.model.MCashoutReciept;
import games.core.application.ServerConfig;
import games.features.inbox.InboxService;
import games.payment.constant.ResponseBeanPaymentConst;
import games.payment.database.document.DPaymentHistory;
import games.payment.database.model.MPaymentHistory;
import games.payment.object.PaymentItem;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.data.DataCommonManager;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.common.money.TransactionData;
import games.core.common.money.TransactionStatus;
import games.core.constant.ServerCommandConstant;
import games.core.user.User;
import games.core.user.UserService;
import libs.util.LogFactory;
import message.ShopProtos;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by tuanhoang on 8/31/17.
 */
public class CashoutService implements ResponseCashoutErrorCode {
    private static CashoutService instance;
    private static Logger logger;

    private MCashoutReciept mCashoutReciept;
    private MCard mCard;
    private InboxService inboxService;
    private DataCommonManager dataCommonMananger;
    private MPaymentHistory mPaymentHistory;

    public long MONEY_CASHOUT_LIMIT_PERDAY = 1000000;

    public static CashoutService getInstance() {
        if (instance == null) instance = new CashoutService();
        return instance;
    }

    private CashoutService() {
        mCashoutReciept = MCashoutReciept.getInstance();
        mCard = MCard.getInstance();
        inboxService = InboxService.getInstance();
        dataCommonMananger = DataCommonManager.getInstance();
        mPaymentHistory = MPaymentHistory.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());

        MONEY_CASHOUT_LIMIT_PERDAY = ServerConfig.getInstance().getMoneyCashoutLimitPerDay();
        System.out.println("Money cashout limit per day = " + MONEY_CASHOUT_LIMIT_PERDAY);
    }

    //xu ly tiep ne
    public void userCashout(ChannelContextInfo ctx, ShopProtos.ProcessCashoutCardRequestBean cashoutRequestBean) {
        int packageId = cashoutRequestBean.getPackage();
        int cardTypeId = cashoutRequestBean.getCardType();
        String note = cashoutRequestBean.getNote();
        PaymentItem paymentItem = dataCommonMananger.getPaymentItem(packageId);
        int errorCode = REC_SUCCESS;
        String cardSerial = null;
        String cardCode = null;

        if(paymentItem != null) {
            logger.info("User Cashout - " + paymentItem.getId() + " - " + paymentItem.getRealValue() + " - " + paymentItem.getValue());
            if(ctx.checkAuth()) {
                int userId = ctx.getUserId();
                User user = UserService.getInstance().getUser(userId);


                Money moneySub = new Money(paymentItem.getRealValue(), MoneyType.GOLD);
                CardType cardType = CardType.getById(cardTypeId);
                if (cardType == null){
                    errorCode = REC_CARD_TYPE_NOT_VALID;
                }
                else if(user != null) {
                    //check dieu kien cashout
                    if (!user.checkUserCashout(paymentItem.getValue())) {
                        errorCode = REC_CASH_OUT_LIMIT_PERDAY;
                        logger.info("limit cashout per day" );
                    } else {

                        long beforeMoney = user.getMoneyGold();
                        TransactionData transactionData = MoneyService.getInstance().subMoneyAndSaveMongo(moneySub, user, false, true);
                        logger.info("Transaction money - " + transactionData.getStatus() + " - real = " + transactionData.getRealTransactionAmount());
                        if(transactionData.getStatus() == TransactionStatus.SUCCESS) {
                            Card card = mCard.getValidCardBy(cardType.getValue(), paymentItem.getValue());
                            // Nếu đã có card loại này trong DB
                            if (card != null){
                                logger.info("Card availible - "+ card.getCardCode() + " -" + card.getCardSerial());
                                errorCode = REC_SUCCESS;
                                cardCode = card.getCardCode();
                                cardSerial = card.getCardSerial();
                                DPaymentHistory paymentHistory = mPaymentHistory.logCashout(cardType.getValue(), paymentItem.getValue(), beforeMoney, paymentItem.getRealValue(), userId, note);
                                card.use(paymentHistory);
                                sendInboxCard(paymentHistory, card);

                            } else {
                                // Nếu hết card trong db
                                mPaymentHistory.logCashout(cardType.getValue(), paymentItem.getValue(), beforeMoney, paymentItem.getRealValue(), userId, note, DPaymentHistory.PENDING);
                                errorCode = REC_CARD_NOT_ENOUGH_QUANTITY;
                            }

                            user.updateUserCashout(paymentItem.getValue());
                        } else {
                            if(transactionData.getStatus() == TransactionStatus.NOT_ENOUGH_MONEY) {
                                errorCode = REC_CASH_OUT_ERROR_NOT_ENOUGH_MONEY;
                            } else {
                                errorCode = REC_NOT_DEFINE;
                            }
                        }
                    }

                } else {
                    logger.info("user cash out error  - user null" );
                    errorCode = REC_NOT_DEFINE;
                }
            } else {
                logger.info("user cash out error  - not auth" );
                errorCode = REC_NOT_DEFINE;
            }
        } else {
            logger.info("user cash out error  - payment null" );
            errorCode = REC_NOT_DEFINE;
        }
        
        sendCashoutResponse(ctx, errorCode, cardCode, cardSerial);
    }

    public void sendCashoutResponse(ChannelContextInfo ctx, int errorCode, String cardSerial, String cardCode) {
        ShopProtos.ProcessCashoutResponseBean.Builder cashoutResponse = ShopProtos.ProcessCashoutResponseBean.newBuilder();
        cashoutResponse.setErrorCode(errorCode);
        if (cardCode != null){
            cashoutResponse.setCardCode(cardCode);
        }
        if (cardSerial != null){
            cashoutResponse.setCardSerial(cardSerial);
        }
        ctx.push(ServerCommandConstant.PAYMENT_COMMAND, ResponseBeanPaymentConst.CASH_OUT_RESPONSE_BEAN, cashoutResponse);
    }

    public void sendCashoutResponse(ChannelContextInfo ctx, int errorCode){
        sendCashoutResponse(ctx, errorCode, null, null);
    }

    public boolean addReciept(int userId, CashoutType cashoutType, int cardValue, int quantity) {
        logger.info("Add recipt - user id = " + userId + " - cashout type: " + cashoutType + " - card value = " + cardValue + " - quantity = " + quantity);
        if (userId <= 0) {
            return false;
        }
        String productId = "card_" + cardValue;//card_10000
        try {
            mCashoutReciept.addReciept(userId, cashoutType.getValue(), productId, quantity);
            return true;
        } catch (Exception e) {
            logger.error(e);
        }

        return false;
    }

    //User sau khi trúng card sẽ xác nhận xem mình sẽ đổi thẻ cào nào
    public int verifyReciept(int userId, CashoutType cashoutType, CardType cardType) {
        logger.info("Verify recipt - " + userId + " - card type = " + cardType);
        if (userId <= 0) {
            return REC_VERIFY_CARD_ERROR;
        }
        try {
            List<CashoutReciept> listReciept = mCashoutReciept.listCashoutNeedVeriy(userId, cashoutType.getValue());
            logger.info("Verify reciept list reciept = " + listReciept.size());
            for (CashoutReciept cashoutReciept : listReciept) {
                String productId = cashoutReciept.getProduct_id() + "_" + cardType.getValue();
                cashoutReciept.setVerify(true);
                cashoutReciept.setProduct_id(productId);
                cashoutReciept.saveDB();
            }
            return REC_SUCCESS;
        }
        catch (Exception e) {
            logger.error("Verify error", e);
            return REC_VERIFY_CARD_ERROR;
        }
    }

    public int acceptReciept(int recieptId, String approver) {
        logger.info("Accept reciept - " + recieptId + " - approver = " + approver);
        int error_code = REC_SUCCESS;
        try{
            if (approver == null || approver.equals("")) {
                error_code = REC_APPROVER_NULL;
                return error_code;
            }
            DPaymentHistory dPaymentHistory = new DPaymentHistory(recieptId);

            if (!dPaymentHistory.isCashout()){
                error_code = REC_RECIEPT_INVALID;
            } else if ((dPaymentHistory.getStatus() == DPaymentHistory.SUCCESS)) {
                error_code =  REC_RECIEPT_ACCEPTED;
            } else if (dPaymentHistory.getStatus() == DPaymentHistory.PENDING){
                Card card = dPaymentHistory.getValidCard();
                if (card != null){
                    dPaymentHistory.accept(approver);
                    card.use(dPaymentHistory);
                    sendInboxCard(dPaymentHistory, card);
                } else {
                    error_code = REC_CARD_NOT_ENOUGH_QUANTITY;
                }
            } else {
                error_code = REC_RECIEPT_INVALID;
            }

        }catch (Exception e) {
            logger.error(e);
            error_code = REC_NOT_DEFINE;
        }
        return error_code;
    }


    private void sendInboxCard(DPaymentHistory dPaymentHistory, Card card){
        int userId = dPaymentHistory.getUserId();
        String cardCode = card.getCardCode();
        String cardSerial = card.getCardSerial();

        String content = "Chúc mừng bạn đã đổi thẻ thành công. Thẻ của bạn :\n Mã thẻ:  " + cardCode + " \n Serial: " + cardSerial;

        logger.info("Send inbox card - " + userId + " - " + content);
        // TODO anh H gửi inbox cho user tại đây
        InboxService.getInstance().sendMessageFromSystem(userId, "Đổi thẻ thành công", content);
    }
}

