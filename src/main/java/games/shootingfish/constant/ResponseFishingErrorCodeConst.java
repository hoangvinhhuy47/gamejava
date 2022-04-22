package games.shootingfish.constant;

public interface ResponseFishingErrorCodeConst {


    //General
    /**
     * Response success (not any error or Exception)
     */
    int REC_SUCCESS = 0;



    /**
     * Lỗi không xác định
     */
    int REC_NOT_DEFINE = 99;


    int NOT_VERIFY = 21;
//    /**
//     * UserId not exists in fishingPlayerManager
//     */
//    int REC_WRONG_USER_ID = 555;

    //Gen Bullet
    /**
     * Player không đủ tiền để bắn đạn
     */
    int REC_GEN_BULLET_NOT_ENOUGH_MONEY = 1;


    //Gun
    /**
     *  player không đủ tiền để chuyển sang loại súng này
     */
    int REC_CHANGE_GUN_NOT_ENOUGH_MONEY = 10;

    /**
     *  GUN id client gửi lên không đúng
     */
    int REC_WRONG_GUN_ID    = 11;

    //Table
    /**
     * Slot muốn đổi đã được sử dụng
     */
    int REC_CHANGE_SLOT_NOT_AVAILABLE = 20;

    /**
     * Không tìm thấy table mà client gửi lên
     */
    int REC_TABLE_NOT_FOUND = 30;

    // join table
    /**
     * không tìm thấy player để thêm vào PlayerManager (Tức player đó không có trong database hoặc không lấy ra được)
     */
    int REC_JOIN_TABLE_USER_NOT_FOUND = 31;

    /**
     * Không tìm thấy room thể join  (Client gửi lên sai roomID)
     */
    int REC_JOIN_TABLE_ROOM_NOT_FOUND = 32;

    /**
     * Không đủ mức base để vào phòng
     */
    int REC_JOIN_TABLE_FAILD_NOT_ENOUGH_MONEY = 33;

    /**
     * Hết lượt chơi trial, xem video để chơi tiếp
     */
    int REC_JOIN_TRIAL_ERROR_NOT_ENOUGH_TICKET = 34;

    //Game
    /**
     * Không tìm thấy Cá hoặc đạn mà client gửi lên trong Game
     */
    int REC_HIT_FISH_NOT_FOUND = 40;

}
