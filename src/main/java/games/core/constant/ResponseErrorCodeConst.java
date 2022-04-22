package games.core.constant;

/**
 * Created by tuanhoang on 8/24/17.
 */
public interface ResponseErrorCodeConst {
    //General
    /**
     * Response success (not any error or Exception)
     */
    int REC_SUCCESS = 0;

    //General
    /**
     * Response Not define
     */
    int REC_NOT_DEFINE = 99;

    /**
     * Response User not found
     */
    int REC_USER_NOT_FOUND = 2;

    /**
     * Response Dai reward wrong
     */
    int REC_DR_DAY_REWARD_WRONG = 3;

    /**
     * Response you recieve reward today
     */
    int REC_DR_YOU_RECIEVE_REWARD_TODAY = 4;

    //Login

    /**
     * WRONG TOKEN
     */
    int REC_LOGIN_WRONG_TOKEN = 10;

    /**
     * Can't auth this user in ChannelService.authChannel
     */
    int REC_LOGIN_CAN_NOT_AUTH = 11;


    /**
     * Account login on other
     */
    int REC_DISCONNECT_BECAUSE_OTHER_PLAYER_LOGIN_YOUR_ACCOUNT = 12;

    /**
     * server maintain
     */
    int REC_SERVER_MAINTENANCE = 20;

    /**
     * force update new version
     */
    int REC_FORCE_UPDATE_NEW_VERSION = 21;

    /**
     * user chưa được xác thực (yêu cầu login lại)
     */
    int REC_USER_NOT_AUTH = 50;

    /**
     * Verify sdt thi can phai nhan sdt
     */
    int REC_USER_PHONE_REQUIRE = 60;

    /**
     * Khong get duoc otp vi chua verify
     */
    int REC_USER_OTP_ERROR_NOT_VERIFY_PHONE = 61;

    /**
     * Ma OTP khong hop le
     */
    int REC_USER_OTP_NOT_MATCH = 62;

    //sdt nay da ton tai
    int REC_USER_PHONE_ALREADY_EXIST = 65;

    /**
     * OTP het thoi gian
     */
    int REC_USER_OTP_TIME_OUT = 63;

    /**
     * OTP chua co, m phai get otp truoc
     */
    int REC_USER_OTP_NOT_DEFINE = 64;

    int REC_NOT_FOUND = 404;

    /**
     * Khong du dieu kien chuyen tien
     */
    int NOT_ENOUGH_CONDITION = 405;

}
