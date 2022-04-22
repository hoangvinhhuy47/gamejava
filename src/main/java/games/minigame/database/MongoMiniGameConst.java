package games.minigame.database;

/**
 * Created by tuanhoang on 8/17/17.
 */
public interface MongoMiniGameConst {

    //name of collection
    String COLL_USER_MINI_GAME = "userMiniGame";
    String COLL_GIFT_CODE = "giftCode";
    String COLL_WHEEL_HISTORY = "wheelHistory";

    //for coll userMiniGame
    String FIELD_DEFAULT_ID = "_id";
    String FIELD_UM_TICKET_WHEEL = "ticket_wheel";
    String FIELD_NUM_OF_TRIAL   = "num_of_trial";
    String FIELD_DAY_LOGIN      = "day_login";//ngày gần nhất login
    String FIELD_DAY_REWARD     = "day_reward";//ngày gần nhất nhận thưởng
    String FIELD_REWARD_COUNT_DAY = "reward_count_day";//đang nhận phần thưởng thứ mấy. 1 -> 7
    String FIELD_PROMOTE_CODE   = "promote_code";
    String FIELD_ENTER_FRIEND_CODE = "enter_friend_code";
    String FIELD_COUNT_RECIEVE_VIDEO_ADS = "count_recieve_video_ads";
    String FIELD_COUNT_FRIEND_PROMOTE_CODE = "count_fp_code";

    String FIELD_DAY_CASHOUT = "dayCashout";
    String FIELD_MONEY_CASHOUT_INDAY = "moneyCashoutInDay";

    //for coll giftcode
    String FIELD_GC_CODE = "_id";
    String FIELD_GC_CODE_NAME = "name";
    String FIELD_GC_GOLD_REWARD = "gold_reward";
    String FIELD_GC_SILVER_REWARD = "silver_reward";
    String FIELD_GC_TICKET_WHEEL = "ticket_wheel";
    String FIELD_GC_TIME_CREATE = "time_create";
    String FIELD_GC_IS_USED = "is_used";
    String FIELD_GC_USER_ID = "user_id";
    String FIELD_GC_TIME_ACTIVE = "time_active";

    //for coll Wheel history
    String FIELD_WH_SESSION_ID = "session_id";
    String FIELD_WH_USER_ID = "user_id";
    String FIELD_WH_TIME_STAMP = "timestamp";//thời gian quay
    String FIELD_WH_ID_RS_WHEEL_1 = "id_wheel_1";


}
