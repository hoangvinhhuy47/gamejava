package games.shootingfish.constant;

public interface ResponseErrorCode {


    //General
    /**
     * Response success (not any error or Exception)
     */
    int REC_SUCCESS = 0;

    /**
     * user chưa được xác thực (yêu cầu login lại)
     */
    int REC_USER_NOT_AUTH = 405;

    /**
     * Lỗi không xác định
     */
    int REC_NOT_DEFINE = 99;

    /**
     * UserId not exists in fishingPlayerManager
     */
    int REC_WRONG_USER_ID = 555;

    //Gen Bullet
    /**
     * Player không đủ tiền để bắn đạn
     */
    int REC_GEN_BULLET_NOT_ENOUGH_MONEY = 1;
    int REC_2 = 2;



    //Gun
    /**
     *  player không đủ tiền để chuyển sang loại súng này
     */
    int REC_CHANGE_GUN_NOT_ENOUGH_MONEY = 300;

    /**
     *  GUN id client gửi lên không đúng
     */
    int REC_WRONG_GUN_ID    = 301;

    //Table
    /**
     * Slot muốn đổi đã được sử dụng
     */
    int REC_CHANGE_SLOT_NOT_AVAILABLE = 401;

    /**
     * Không tìm thấy table mà client gửi lên
     */
    int REC_TABLE_NOT_FOUND = 404;
    //Login

    /**
     * WRONG TOKEN
     */
    int REC_LOGIN_WRONG_TOKEN = 100;
    /**
     * Can't auth this user in ChannelService.authChannel
     */
    int REC_LOGIN_CAN_NOT_AUTH = 102;


    /**
     * Account login on other
     */
    int REC_DISCONNECT_BECAUSE_OTHER_PLAYER_LOGIN_YOUR_ACCOUNT = 103;
    // join table
    /**
     * không tìm thấy player để thêm vào PlayerManager (Tức player đó không có trong database hoặc không lấy ra được)
     */
    int REC_JOIN_TABLE_USER_NOT_FOUND = 200;

    /**
     * Không tìm thấy room thể join  (Client gửi lên sai roomID)
     */
    int REC_JOIN_TABLE_ROOM_NOT_FOUND = 201;

    //Game
    /**
     * Không tìm thấy Cá hoặc đạn mà client gửi lên trong Game
     */
    int REC_HIT_FISH_NOT_FOUND = 901;

}
