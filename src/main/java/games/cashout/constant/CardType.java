package games.cashout.constant;

public enum CardType {
    VIETTEL(1, "vt"),
    MOBI(2, "mb"),
    VINA(3, "vn");
    int id;
    String value;
    CardType(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public int getId() {
        return this.id;
    }

    public static CardType getById(int id){
        for (CardType value : values()) {
            if (value.getId() == id){
                return value;
            }
        }
        return null;
    }
}
