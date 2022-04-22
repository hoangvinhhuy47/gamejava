package games.payment.constant;

/**
 * Created by tuanhoang on 8/24/17.
 */
public interface ResponseBeanPaymentErrorCode {
    //General
    /**
     * Response success (not any error or Exception)
     */
    int REC_SUCCESS = 0;

    /**
     * Response Not define
     */
    int REC_NOT_DEFINE = 99;

    /**
     * User not found
     */
    int REC_USER_NOT_FOUND = 2;

    /**
     * Verify error
     */
    int REC_VERIFY_ERROR = 3;

    /**
     * Package id wrong
     */
    int REC_PRODUCT_ID_ERROR = 4;

    int REC_MAX_VIDEO_ONCEDAY = 10;

    /*
    * Payemnt card
    * */
    int REC_CARD_CODE_WRONG = 20;
    int REC_CARD_USED = 21;
    int REC_CARD_LOCKED = 22;
    int REC_CARD_TIME_OUT = 23;
    int REC_CARD_NOT_ACTIVE = 24;
    int REC_CARD_CODE_OR_SERIAL_INVALID = 25;
    int REC_CARD_CODE_OR_SERIAL_INVALID_2 = 26;
    int REC_CARD_SYSTEM_ERROR = 27;
    int REC_CARD_LOCK_ACCOUNT = 28;
}
