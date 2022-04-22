package games.payment.constant;

/**
 * Created by tuanhoang on 10/4/17.
 */
public enum PaymentActionType {
    NONE(0),
    RECHARGE(1),
    CASH_OUT(2),
    TRANSFER(3);

    int value;
    PaymentActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static PaymentActionType getById(int id){
        for (PaymentActionType pActionType : PaymentActionType.values()){
            if (pActionType.getValue() == id) return pActionType;
        }
        return NONE;
    }
}