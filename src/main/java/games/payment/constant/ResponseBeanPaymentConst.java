package games.payment.constant;

/**
 * Created by tuanhoang on 8/24/17.
 */
public interface ResponseBeanPaymentConst {

    int plus = 179;
    //payment
    int PAYMENT_RESPONSE_BEAN = 1 + plus;

    int PAYMENT_ERROR_RESPONSE_BEAN = 2 + plus;

    int CASH_OUT_RESPONSE_BEAN = 21 + plus;

    int TRANSFER_RESPONSE_BEAN = 31 + plus;

    int HISTORY_PAYMENT_RESPONSE_BEAN = 41 + plus;
}
