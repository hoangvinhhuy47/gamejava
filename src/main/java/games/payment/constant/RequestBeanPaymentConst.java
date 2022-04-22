package games.payment.constant;

/**
 * Created by tuanhoang on 8/24/17.
 */
public interface RequestBeanPaymentConst {
    //Iap
    int plus = 179;
    int IAP_APPLE_PAYMENT_REQUEST = 1 + plus;
    int IAP_GOOGLE_PAYMENT_REQUEST = 2 + plus;

    int VIDEO_ADS_REQUEST = 10 + plus;

    int CARD_PAYMENT_REQUEST = 11 + plus;

    int CASH_OUT_REQUEST = 21 + plus;

    int TRANSFER_REQUEST = 31 + plus;

    int HISTORY_PAYMENT_REQUEST = 41 + plus;
}
