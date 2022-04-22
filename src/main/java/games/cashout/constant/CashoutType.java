package games.cashout.constant;

public enum CashoutType {
    VQMM (1),
    USER_CASH_OUT(2);

    int value;
    CashoutType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }



}
