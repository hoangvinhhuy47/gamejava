package games.features.constant;

/**
 * Created by tuanhoang on 8/21/17.
 */
public interface ResponseFeatureErrorCode {

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
     * Response User not exist - wrong user id
     */
    int REC_INBOX_USER_NOT_EXIST = 2;

    /**
     * Response you can't open inbox of others
     */
    int REC_INBOX_CANT_OPEN_INBOX_OF_OTHER = 3;

    /**
     * Response inbox not exits -> wrong user id
     */
    int REC_INBOX_NOT_EXIST = 4;

    /**
     * Response you can't remove inbox of others
     */
    int REC_INBOX_CANT_REMOVE_INBOX_OF_OTHER = 5;


    /**
     * Response friend not exist
     */
    int REC_FRIEND_NOT_EXIST = 40;

    /**
     * Response you can't add friend because you are friends
     */
    int REC_FRIEND_CANT_ADD_BECAUSE_ALREADY_FRIEND = 41;

    /**
     * Response him reject you
     */
    int REC_FRIEND_CANT_ADD_BECAUSE_ALREADY_IN_REQUEST_OR_REJECT = 42;

    /**
     * Response you can't accept friend because you are friends
     */
    int REC_FRIEND_CANT_ACCEPT_BECAUSE_ALREADY_FRIEND = 43;

    /**
     * Response you can't accept friend because not exist on list request
     */
    int REC_FRIEND_CANT_ACCEPT_BECAUSE_NOT_EXIST_IN_REQUEST = 44;

    /**
     * Response you can't remove friend because you are not friend
     */
    int REC_FRIEND_CANT_REMOVE_BECAUSE_YOU_ARE_NOT_FRIEND = 45;

    /**
     * Response you can't accept friend because you are friends
     */
    int REC_FRIEND_CANT_DECLINE_BECAUSE_ALREADY_FRIEND = 46;

    /**
     * Response you can't accept friend because not exist on list request
     */
    int REC_FRIEND_CANT_DECLINE_BECAUSE_NOT_EXIST_IN_REQUEST = 47;

    /**
     * Response you can't accept friend because not exist on list request
     */
    int REC_FRIEND_CANT_ADD_FRIEND_WITH_MINE = 48;
}
