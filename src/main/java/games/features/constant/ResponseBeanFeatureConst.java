package games.features.constant;

/**
 * Created by tuanhoang on 8/21/17.
 */
public interface ResponseBeanFeatureConst {
    //Rank
    int RESPONSE_RANKING_TOP_TOTAL_MONEY_BEAN 							= 1;
    int RESPONSE_RANKING_TOP_WIN_MONEY_BEAN 							= 2;

    // Inbox
    int	LIST_INBOX_MESSAGE_RESPONSE_BEAN								= 10;
    int	SEND_INBOX_MESSAGE_RESPONSE_BEAN								= 11;
    int	OPEN_INBOX_MESSAGE_RESPONSE_BEAN								= 12;
    int	REMOVE_INBOX_MESSAGE_RESPONSE_BEAN								= 13;

    // PushMessage
    int PUSH_MESSAGE_TEXT_RUN_RESPONSE_BEAN                             = 20;
    int PUSH_MESSAGE_DIALOG_RESPONSE_BEAN                               = 21;

    //News
    int LIST_NEWS_RESPONSE_BEAN                              = 30;
    int READ_NEWS_RESPONSE_BEAN                              = 31;

    //Friend
    //get list user online
    int LIST_USER_ONLINE_RESPONSE_BEAN                       = 40;

    //request add friends
    int ADD_FRIEND_RESPONSE_BEAN                             = 41;

    //request accept friends
    int ACCEPT_FRIEND_RESPONSE_BEAN                          = 42;

    //request decline friends
    int DECLINE_FRIEND_RESPONSE_BEAN                         = 43;

    //request remove friends
    int REMOVE_FRIEND_RESPONSE_BEAN                          = 44;

    //request get list friends
    int LIST_FRIEND_RESPONSE_BEAN                            = 45;

    //request get list Ask friend
    int LIST_ASK_FRIEND_RESPONSE_BEAN                        = 46;

    //request find friend
    int FIND_FRIEND_RESPONSE_BEAN                            = 47;

    //Send for others
    //others request add friends
    int OTHER_ADD_FRIEND_RESPONSE_BEAN                             = 48;

    //othres request accept friends
    int OTHER_ACCEPT_FRIEND_RESPONSE_BEAN                          = 49;

}
