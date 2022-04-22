package games.minigame.constant;

/**
 * Created by tuanhoang on 9/8/17.
 */
public enum AddTicketWheelFrom
{
    GIFT_CODE (1),
    WHEEL(2),
    PROMOTE_CODE (3),
    LOGIN (4),
    ADMIN (5);

    int value;
    int getValue() {
        return value;
    }

    AddTicketWheelFrom(int value) {
        this.value = value;
    }
}
