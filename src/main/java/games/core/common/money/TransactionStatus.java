package games.core.common.money;

/**
 * Created by Cuong Nguyen Cao on 7/8/17.
 */
public enum TransactionStatus {
    SUCCESS(1),
    NOT_ENOUGH_MONEY(2),
    INPUT_ERROR(3),
    WRONG_MONEY_TYPE(4),
    NONE(999);

    private int code;

    TransactionStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TransactionStatus getByCode(int code){
        for (TransactionStatus t : TransactionStatus.values()){
            if (t.code == code){
                return t;
            }
        }
        return NONE;
    }
}
