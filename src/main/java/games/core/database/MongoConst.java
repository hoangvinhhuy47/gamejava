package games.core.database;

public interface MongoConst {
    //collection
    String COLL_USER = "users";
    String COLL_USER_MONEY = "userMoney";
    String COLL_ROOM_INFO = "roomInfo";
    String COLL_PAYMENT_HISTORY = "paymentHistory";
    String COLL_FUND_HISTORY = "fundHistory";

    //Field
        // DEFAULT
    String FIELD_TIMESTAMP = "timestamp";
    String FIELD_USER_ID = "user_id";

        //for coll_user
    String FIELD_DEFAULT_ID = "_id";
    String FIELD_USER_NAME = "user_name";
    String FIELD_NICK_NAME = "nick_name";
    String FIELD_GENDER = "gender";
    String FIELD_MAIL = "mail";
    String FIELD_USER_PHONE = "phone";
    String FIELD_PLATFORM = "platform";
    String FIELD_VERSION = "version";
    String FIELD_SOURCE    = "source";
    String FIELD_OTP = "otp";
    String FIELD_TIME_GET_OTP = "time_otp";
    String FIELD_IS_VERIFY_PHONE = "is_verify_phone";//da verify sdt
    String FIELD_IS_BANNED = "is_banned";

    String FIELD_USER_PASS = "user_password";
    String FIELD_ACCESS_TOKEN = "access_token";

    String FIELD_USER_AVATAR = "avatar";
    String FIELD_FB_ID = "fb_id";

    String FIELD_DEVICE_ID = "device_id";
    String FIELD_MAC_ADDRESS = "mac_address";
    String FIELD_IMIE           = "imie";
    String FIELD_IP_ADDRESS = "id_address";
    String FIELD_NEW_REGISTER = "new_register";
    String FIELD_DAY_REGISTER = "day_register";

        //for coll_user_money
    String FIELD_USER_GOLD = "gold";
    String FIELD_DEPOSIT_FROM_WEB = "depositFrWeb";
    String FIELD_DEPOSIT_FROM_WALLET = "depositFrWallet";
    String FIELD_DEPOSIT_FROM_WEB_PROVISIONAL = "depositFrWebProvisional";
    String FIELD_DEPOSIT_FROM_WALLET_PROVISIONAL = "depositFrWalletProvisional";
    String FIELD_USER_SILVER = "silver";
    String FIELD_WIN_GOLD = "win_gold";
    String FIELD_WIN_SILVER = "win_silver";

    // For tx
    String FIELD_ROOM_ID = "room_id";

    //For coll roomInfo
    String FIELD_ROOM_INFO_GAME_ID = "game_id";
    String FIELD_ROOM_INFO_NAME = "name";
    String FIELD_ROOM_INFO_POT = "pot";
    String FIELD_ROOM_INFO_MIN_JACKPOT = "min_jackpot";
    String FIELD_ROOM_INFO_MAX_JACKPOT = "max_jackpot";
    String FIELD_ROOM_INFO_POT_PERCENT = "pot_percent";
    String FIELD_ROOM_INFO_FUND_PERCENT = "fund_percent";
    String FIELD_ROOM_INFO_FUND = "fund";
    String FIELD_ROOM_INFO_FUND_DEALER = "fund_dealer";//tien quy cua nha cai
    String FIELD_ROOM_INFO_SLOT_MIN_STEP = "slot_min_step";
    String FIELD_ROOM_INFO_SLOT_MAX_STEP = "slot_max_step";
    String FIELD_ROOM_INFO_SLOT_STEP_MONEY = "slot_step_money";
    String FIELD_ROOM_INFO_SLOT_WIN_RATE = "slot_win_rate";

        // for payment history
    String FIELD_PAYMENT_TYPE = "type";
    String FIELD_PAYMENT_BEFORE_VALUE = "before_value";
    String FIELD_PAYMENT_VALUE = "value";
    String FIELD_PAYMENT_AFTER_VALUE = "after_value";
    String FIELD_PAYMENT_NOTE = "note";
    String FIELD_PAYMENT_SENDER_ID = "sender_id";
    String FIELD_PAYMENT_RECEIVER_ID = "receiver_id";
    String FIELD_PAYMENT_STATUS = "status";
    String FIELD_PAYMENT_CARD_TYPE = "card_type";
    String FIELD_PAYMENT_CARD_VALUE = "card_value";
    String FIELD_PAYMENT_CARD_CODE = "card_code";
    String FIELD_PAYMENT_CARD_SERIAL = "card_serial";
    String FIELD_PAYMENT_REQUEST_STATUS = "request_status";
    String FIELD_PAYMENT_VERIFY_STATUS = "verify_status";
    String FIELD_PAYMENT_ACCEPTED_TIME = "accepted_timestamp";
    String FIELD_PAYMENT_APPROVER = "approver";

    // for FUND history
    String FIELD_FUND_TYPE = "type";
    String FIELD_FUND_VALUE = "value";
    String FIELD_FUND_BEFORE_VALUE = "before_value";
    String FIELD_FUND_AFTER_VALUE = "after_value";
    String FIELD_FUND_ADMIN = "admin";
    String FIELD_FUND_IS_DEALER = "is_fund_dealer";

    //ERROR
    int ERROR_CODE_MONGO_DB_CANNOT_SAVE = 100;
}
