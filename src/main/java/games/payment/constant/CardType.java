package games.payment.constant;

/**
 * Created by tuanhoang on 10/4/17.
 */
public enum CardType {
    VIETTEL(1, "viettel"),
    VINAPHONE(2, "vina"),
    MOBIPHONE(3, "mobi"),
    VCOIN(4, "vcoin"),
    VGG(5, "vgg"),
    NONE(0, "none");

    String value;
    int id;
    CardType(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return this.value;
    }

    public static CardType getById(int id){
        for (CardType cardType : CardType.values()){
            if (cardType.getId() == id) return cardType;
        }
        return NONE;
    }
}
