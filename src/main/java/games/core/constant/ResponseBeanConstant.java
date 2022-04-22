package games.core.constant;

/**
 * Created by tuanhoang on 4/13/17.
 */
public interface ResponseBeanConstant {

    int plus = 179;
    //Init
    int	INIT_CLIENT_RESPONSE_BEAN										= 1 + plus;

    //PING
    //Client chủ động request để check serer
    int RESPONSE_PING_CHECK_CLIENT                                      = 2 + plus;

    //client nhận ping và phản hồi lại cho server
    int RESPONSE_PING_RESPONSE_CLIENT                                   = 3 + plus;

    int DISCONNECT_RESPONSE_BEAN                                        = 8 + plus;
    // NEW USER
    int	LOGIN_RESPONSE_BEAN												= 20 + plus;
    int UPDATE_MONEY_RESPONSE_BEAN                                      = 21 + plus;
    int UPDATE_USER_INFO_RESPONSE_BEAN                                  = 22 + plus;

    int GET_INFO_OTHER_PLAYER_RESPONSE_BEAN                              = 25 + plus;
    int DAILY_REWARD_RESPONSE_BEAN                                      = 26 + plus;
    int INIT_DAILY_REWARD_RESPONSE_BEAN                                 = 27 + plus;

    int GET_OTP_RESPONSE_BEAN                                 = 30 + plus;

    int VERIFY_PHONE_RESPONSE_BEAN                           = 31 + plus;

    //Common
    short CHAT_RESPONSE_BEAN                                              = 4 + plus;
    short CCU_RESPONSE_BEAN                                             = 5 + plus;

}
