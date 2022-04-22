package games.cashout.constant;

/**
 * Created by tuanhoang on 9/1/17.
 */
public interface ResponseCashoutErrorCode {
    //General
    /**
     * Response success (not any error or Exception)
     */
    int REC_SUCCESS = 0;

    //General
    /**
     * Response Not define
     */
    int REC_NOT_DEFINE = 99;

    /**
     * Approver null
     */
    int REC_APPROVER_NULL = 2;


    /**
     * Hóa đơn đã thanh toán
     */
    int REC_RECIEPT_ACCEPTED = 3;


    /**
     * Card not enought quantity
     */
    int REC_CARD_NOT_ENOUGH_QUANTITY = 4;

    /**
     * Product id wrong
     */
    int REC_PRODUCT_ID_WRONG = 5;

    /**
     * Quantity wrong
     */
    int REC_QUANTITY_WRONG = 6;

    /**
     * Reciept invalid
     */
    int REC_RECIEPT_INVALID = 7;


    /**
     * Verify card error
     */
    int REC_VERIFY_CARD_ERROR = 8;

    int REC_CASH_OUT_ERROR_NOT_ENOUGH_MONEY = 21;

    int REC_CARD_TYPE_NOT_VALID = 22;

    int REC_CASH_OUT_LIMIT_PERDAY = 23;
}
