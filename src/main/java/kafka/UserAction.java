package kafka;

/**
 * Created by tuanhoang on 9/21/17.
 */
public enum  UserAction {

    //ko có tiền
    LOGIN (1, 0),
    OFFLINE(2, 0),

    SPIN_WHEEL_RESULT (9, 0),
    //Nhận tiền
    RECIEVE_DAILY_REWARD(10, 1),
    RECIEVE_WHEEL_REWARD (11, 1),
    RECIEVE_GIFT_CODE_REWARD (12, 1),
    //RECIEVE_PROMOTE_CODE_REWARD (13,1),

    //thanh toán
    PAYMENT_VIDEO_ADS(30, 1),
    PAYMENT_GOOGLE (31, 1),
    PAYMENT_APPLE (32, 1),
    PAYMENT_CARD (33, 1),

    //Game
    JOIN_GAME (40, 0),
    LEAVE_GAME (41, 0),
    FIRE_BULLET (42, 2),
    HIT_FISH (43, 0),
    KILL_FISH (44, 1),
    START_SESSION (45, 0),
    END_SESSION (46, 0),

    //Admin
    ADMIN_ADD(100, 1),
    ADMIN_SUB(101, 2);

    int value;
    int groupId;
    UserAction(int value, int groupId) {
        this.groupId = groupId;
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public int getGroupId() {return this.groupId;}
}
