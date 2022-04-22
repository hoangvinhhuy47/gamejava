package games.system.database;

/**
 * Created by tuanhoang on 9/8/17.
 */
public interface MongoSystemConst {

    //name of collection
    String COLL_REPORT_CCU = "reportCCU";
    String COLL_REPORT_MINIGAME = "reportMinigame";

    //report ccu
    String FIELD_DEFAULT_ID = "_id";
    String FIELD_REPORT_CCU_PCU = "pcu";
    String FIELD_REPORT_CCU_ACU = "acu";
    String FIELD_REPORT_CCU_COUNT_UPDATE = "count";

    String FIELD_REPORT_CCU_PCU_IN_GAME = "pcu_in_game";
    String FIELD_REPORT_CCU_ACU_IN_GAME = "acu_in_game";
    String FIELD_REPORT_CCU_COUNT_UPDATE_IN_GAME = "count_in_game";

    //report minigame
    String FIELD_REPORT_MINIGAME_TICKET_FROM_GIFT_CODE = "ticket_from_giftcode";
    String FIELD_REPORT_MINIGAME_TICKET_FROM_WHEEL = "ticket_from_wheel";
    String FIELD_REPORT_MINIGAME_TICKET_FROM_PROMOTE_CODE = "ticket_from_promote_code";
    String FIELD_REPORT_MINIGAME_TICKET_FROM_LOGIN = "ticket_from_login";
    String FIELD_REPORT_MINIGAME_TICKET_FROM_ADMIN = "ticket_from_admin";
}
