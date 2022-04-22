package games.core.common.money;

/**
 * @author Cuong Nguyen Cao
 */
public enum MoneyType {
    SILVER(1),
    GOLD(2),
    TRIAL(3),
    NONE(0);


    private int code;
    MoneyType(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    public static MoneyType getByCode(int code){
        for (MoneyType moneyType : MoneyType.values()){
            if (moneyType.getCode() == code) return moneyType;
        }
        return NONE;
    }
}
