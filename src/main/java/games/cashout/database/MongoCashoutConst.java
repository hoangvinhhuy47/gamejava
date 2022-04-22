package games.cashout.database;

/**
 * Created by tuanhoang on 8/31/17.
 */
public interface MongoCashoutConst {
    String COLL_CASHOUT = "cashout";
    String COLL_CARD    = "card";
    String COLL_IP_CHECK = "ipCheck";

    //coll cashout
    String FIELD_DEFAULT_ID = "_id";

    //user nào request đơn hàng này
    String FIELD_CO_USER_ID = "user_id";

    //thời gian request
    String FILED_CO_TIME_REQUEST = "time_request";

    //thời gian xác nhận
    String FIELD_CO_TIME_ACCEPT = "time_accept";

    //User xác thực
    String FIELD_CO_USER_VERIFY = "is_verify";

    //người xác nhận
    String FIELD_CO_APPROVER    = "approver";//

    //Type cash out. VQMM - đổi thưởng
    String FIELD_CO_TYPE        = "type";

    //sản phẩm đổi là gì
    String FIELD_CO_PRODUCT_ID = "product_id";//card_vt_20000 or iphone_7

    //số lượng
    String FIELD_CO_QUANTITY = "quantity";
    //status
    String FIELD_RECEIPT_STATUS = "status";



    //coll card
    //serial
    String FIELD_CARD_SERIAL = "card_serial";

    //code
    String FIELD_CARD_CODE = "card_code";

    //card type: vt - vn - mobi
    String FIELD_CARD_TYPE = "card_type";

    //card value: 10000 - 20000
    String FIELD_CARD_VALUE = "card_value";

    //Thời gian add vào db
    String FIELD_CARD_TIME_ACTIVE = "time_active";

    //USED = false: có thể lấy để trả. USED = true: ko thể, card đã được sử dụng
    String FIELD_CARD_IS_USED = "is_used";

    //đã trả thưởng nó cho ai
    String FIELD_CARD_USER_ID = "user_id";

    //Thoi gian trả thưởng
    String FIELD_CARD_TIME_USED = "time_used";

    //id của đơn hàng
    String FIELD_CARD_RECEIPT_ID = "receipt_id";


    //coll ip check
    String FIELD_IP_LIST_USER_ID = "list_user_id";

    String FIELD_IP_LIST_CARD_RECIEVED = "list_card";

    String FIELD_IP_COUNT_CARD = "count_card";

}
