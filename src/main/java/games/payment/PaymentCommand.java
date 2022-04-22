package games.payment;

import com.google.protobuf.InvalidProtocolBufferException;
import games.core.common.data.DataCommonManager;
import games.core.common.money.*;
import games.core.constant.*;
import games.cashout.CashoutService;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.database.document.UserInfo;
import games.core.user.User;
import games.core.user.UserCommand;
import games.core.user.UserService;
import games.features.inbox.InboxService;
import games.payment.constant.RequestBeanPaymentConst;
import games.payment.constant.ResponseBeanPaymentConst;
import games.payment.database.document.DPaymentHistory;
import games.payment.database.model.MPaymentHistory;
import libs.util.LogFactory;
import message.ShopProtos;
import message.XProtos;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by tuanhoang on 8/24/17.
 */
public class PaymentCommand extends ICommand implements RequestBeanPaymentConst, ResponseErrorCodeConst {
    private static PaymentCommand instance;
    private static Logger logger;

    public static PaymentCommand getInstance() {
        if (instance == null) instance = new PaymentCommand();
        return instance;
    }

    private PaymentService paymentService;
    private CashoutService cashoutService;
    private MoneyService moneyService;
    private DataCommonManager dataCommonManager;

    private PaymentCommand() {
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        paymentService = PaymentService.getInstance();
        cashoutService = CashoutService.getInstance();
        moneyService = MoneyService.getInstance();
        dataCommonManager = DataCommonManager.getInstance();
        logger.info("Payment command init");
    }

    @Override
    public int getCommandCode() {
        return ServerCommandConstant.PAYMENT_COMMAND;
    }

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        switch (message.getBeanType()) {
            case IAP_APPLE_PAYMENT_REQUEST:
                logger.info("Request payment apple");
                ShopProtos.ProcessPaymentAppleRequestBean requestBean = ShopProtos.ProcessPaymentAppleRequestBean.parseFrom(message.getData());
                paymentService.applePayment(ctx, requestBean);
                break;
            case IAP_GOOGLE_PAYMENT_REQUEST:
                logger.info("Request payment google");
                ShopProtos.ProcessPaymentGoogleRequestBean requestBeanGoogle = ShopProtos.ProcessPaymentGoogleRequestBean.parseFrom(message.getData());
                paymentService.googlePayment(ctx, requestBeanGoogle);
                break;
            case VIDEO_ADS_REQUEST:
                logger.info("Request payment video ads");
                paymentService.videoAdsPayment(ctx);
                break;
            case CARD_PAYMENT_REQUEST:
                logger.info("Request payment card");
                ShopProtos.ProcessPaymentCardRequestBean cardRequestBean = ShopProtos.ProcessPaymentCardRequestBean.parseFrom(message.getData());
                paymentService.cardPayment(ctx, cardRequestBean);
                break;
            case CASH_OUT_REQUEST:
                logger.info("Request cashout card");
                ShopProtos.ProcessCashoutCardRequestBean cashoutRequestBean = ShopProtos.ProcessCashoutCardRequestBean.parseFrom(message.getData());
                int errorCode = REC_SUCCESS;//checkOTP(ctx, cashoutRequestBean.getOtp());
                if (errorCode == REC_SUCCESS) {
                    cashoutService.userCashout(ctx, cashoutRequestBean);
                } else {
                    cashoutService.sendCashoutResponse(ctx, errorCode);
                }
                break;
//            case TRANSFER_REQUEST:
//                logger.info("Request transfer");
//                ShopProtos.ProcessTransferGoldRequestBean transferRequestBean = ShopProtos.ProcessTransferGoldRequestBean.parseFrom(message.getData());
//                int errorCodeTransfer = -99;//checkOTP(ctx, transferRequestBean.getOtp());
//                if (errorCodeTransfer == REC_SUCCESS) {
//                    String nickName = ctx.getUser().getUserName();
//                    String userName = transferRequestBean.getUserName();
//                    if (nickName != userName)
//                        transferGold(ctx, transferRequestBean);
//                } else {
//                    sendTransferResponse(ctx, errorCodeTransfer);
//                }
//                break;
            case HISTORY_PAYMENT_REQUEST:
                historyPayment(ctx);
                break;
        }
    }

    //anh se check cai OTP nay
    private int checkOTP(ChannelContextInfo ctx, String otp) {

        int errorCode = REC_NOT_DEFINE;
        if (ctx.checkAuth()) {
            int userId = ctx.getUserId();
            User user = UserService.getInstance().getUser(userId);
            String userOtp = user.getOTP();
            if (userOtp == null || userOtp.equals("")) {
                errorCode = REC_USER_OTP_NOT_DEFINE;
            } else {
                if (!userOtp.equals(otp)) {
                    errorCode = REC_USER_OTP_NOT_MATCH;
                } else {
                    errorCode = REC_SUCCESS;
                    user.setOTP("");
                    try {
                        user.saveUserInfo();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }
        } else {
            errorCode = REC_NOT_DEFINE;
        }


        return errorCode;
    }

    //xu ly chuyen tien tai day
    //tru tien truoc
    // sau do cong tien
    // neu khong duoc thi refund
    private void transferGold(ChannelContextInfo ctx, ShopProtos.ProcessTransferGoldRequestBean transferRequestBean)
    {
        ShopProtos.ProcessTransferResponseBean.Builder builder = ShopProtos.ProcessTransferResponseBean.newBuilder();

        String userName = transferRequestBean.getUserName();
        int moneyAmount = transferRequestBean.getValue();
        UserInfo userInfo = new UserInfo(userName);
        try {
            userInfo = userInfo.updateDBByUserName();
            User receiver = UserService.getInstance().getUser(userInfo.getId());//new User(userInfo.getId());
            if (!ctx.isAuth()) {
                builder.setErrorCode(REC_USER_NOT_AUTH);
            } else if (receiver.getId() == 0) {
                builder.setErrorCode(REC_NOT_FOUND);
            } else {
                User sender = ctx.getUser();
                long beforeMoney = sender.getMoneyGold();
                Money subMoney = new Money(moneyAmount, MoneyType.GOLD);
                // Trừ tiền thằng gửi
                TransactionData transactionDataSub = moneyService.subMoneyAndSaveMongo(subMoney, sender, false, true);
                if (transactionDataSub.getStatus() == TransactionStatus.SUCCESS) {
                    builder.setErrorCode(ResponseErrorCodeConst.REC_SUCCESS);
                    // Cộng tiền thằng nhận
                    int subPercent = dataCommonManager.getPercentPaymentItemTransfer();
                    Money addMoney = subMoney.clone().subPercent((double) subPercent);

                    long beforeMoneyRecieve = receiver.getMoneyGold();
                    receiver.SetMoney(addMoney.getAmount());
                    TransactionData transactionDataAdd = moneyService.addMoneyToSave(addMoney, receiver, true, "Tang_Gold");
                    if (transactionDataAdd.getStatus() == TransactionStatus.SUCCESS) {
                        builder.setErrorCode(REC_SUCCESS);
                        UserCommand.getInstance().forceUpdateMoneyToUser(receiver.getId(), null);
                        // Log history
                        MPaymentHistory.getInstance().logTransfer(beforeMoney, beforeMoneyRecieve, moneyAmount, addMoney.getAmount(), sender.getId(), receiver.getId(), transferRequestBean.getNote());

                        //send email
                        String conentGui = "Bạn vừa chuyển thành công " + moneyAmount + " cho " + receiver.getUserName();
                        InboxService.getInstance().sendMessageFromSystem(sender.getId(), "Chuyển điểm thành công", conentGui);

                        String conentNhan = "Bạn vừa nhận " + addMoney.getAmount() + " từ " + sender.getUserName();
                        InboxService.getInstance().sendMessageFromSystem(receiver.getId(), "Nhận điểm thành công", conentNhan);

                    } else {
                        // Refund nếu cộng tiền fail
                        sender.SetMoney(subMoney.getAmount());
                        moneyService.addMoneyToSave(subMoney, sender, true, "Tang_Gold_fail");
                        builder.setErrorCode(transactionDataAdd.getStatus().getCode());
                    }
                } else {
                    builder.setErrorCode(transactionDataSub.getStatus().getCode());
                }
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            builder.setErrorCode(REC_NOT_DEFINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pushToClient(ctx, TRANSFER_REQUEST, builder);
    }

    //gui respose transfer ve cho client
    private void sendTransferResponse(ChannelContextInfo ctx, int errorCode) {
        ShopProtos.ProcessTransferResponseBean.Builder transferResponse = ShopProtos.ProcessTransferResponseBean.newBuilder();
        transferResponse.setErrorCode(errorCode);

        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.PAYMENT_COMMAND);
        response.setBeanType(ResponseBeanPaymentConst.TRANSFER_RESPONSE_BEAN);
        response.setData(transferResponse.build().toByteString());

        ctx.writeAndFlush(response.build());
    }

    private void historyPayment(ChannelContextInfo ctx)
    {
        ShopProtos.HistoryPaymentResponseBean.Builder historyResponse = ShopProtos.HistoryPaymentResponseBean.newBuilder();

        List<DPaymentHistory> historyPaymentInfoBeans = MPaymentHistory.getInstance().getByUserId(ctx.getUserId(), 1000);

        historyPaymentInfoBeans.forEach(dPaymentHistory -> historyResponse.addListPayment(dPaymentHistory.getProtoMessage()));

        pushToClient(ctx, ResponseBeanPaymentConst.HISTORY_PAYMENT_RESPONSE_BEAN, historyResponse);

    }
}
