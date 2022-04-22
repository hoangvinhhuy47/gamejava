package games.core.constant;

/**
 * Created by tuanhoang on 4/13/17.
 */
public interface RequestBeanConstant
{
    int plus = 179;
    // NEW USER
    int INIT_CLIENT_REQUEST_BEAN = 1 + plus;

    // PING
    int PING_RESPONSE_SERVER_REQUEST_BEAN = 2;
    int REQUEST_PING_CHECK_SERVER                           = 3 + plus;

    //LOGIN
    int LOGIN_REQUEST_BEAN                                  = 20 + plus;
    int UPDATE_MONEY_REQUEST_BEAN                           = 21 + plus;
    int UPDATE_USER_INFO_REQUEST_BEAN                       = 22 + plus;

    int GET_INFO_OTHER_PLAYER_REQUEST_BEAN                  = 25 + plus;

    int DAILY_REWARD_REQUEST_BEAN                           = 26 + plus;

    int INIT_DAILY_REWARD_REQUEST_BEAN                      = 27 + plus;

    int GET_OTP_REQUEST_BEAN                                = 30 + plus;

    int VERIFY_PHONE_REQUEST_BEAN                           = 31 + plus;


    //Common
    int CHAT_REQUEST_BEAN                                   = 4 + plus;
}
