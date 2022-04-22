package games.features.constant;

/**
 * Created by tuanhoang on 8/21/17.
 */
public interface RequestBeanFeatureConst {
    //Rank
    int	REQUEST_RANKING_TOP_GOLD						    = 1;
    int	REQUEST_RANKING_TOP_WIN_GOLD						= 2;


    //Inbox
    int	REQUEST_LIST_INBOX_MESSAGE_BEAN						= 10;
    int	REQUEST_SEND_INBOX_MESSAGE_BEAN						= 11;
    int	REQUEST_OPEN_INBOX_MESSAGE_BEAN						= 12;
    int	REQUEST_REMOVE_INBOX_MESSAGE_BEAN					= 13;

    //News
    int LIST_NEWS_REQUEST_BEAN                              = 30;
    int READ_NEWS_REQUEST_BEAN                              = 31;

    //Friends
    //get list user online
    int LIST_USER_ONLINE_REQUEST_BEAN                       = 40;

    //request add friends
    int ADD_FRIEND_REQUEST_BEAN                             = 41;

    //request accept friends
    int ACCEPT_FRIEND_REQUEST_BEAN                          = 42;

    //request decline friends
    int DECLINE_FRIEND_REQUEST_BEAN                         = 43;

    //request remove friends
    int REMOVE_FRIEND_REQUEST_BEAN                          = 44;

    //request get list friends
    int LIST_FRIEND_REQUEST_BEAN                            = 45;

    //request get list Ask friend
    int LIST_ASK_FRIEND_REQUEST_BEAN                        = 46;

    //request find friend
    int FIND_FRIEND_REQUEST_BEAN                            = 47;
}
