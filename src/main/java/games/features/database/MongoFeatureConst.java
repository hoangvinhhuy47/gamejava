package games.features.database;

/**
 * Created by tuanhoang on 8/21/17.
 */
public interface MongoFeatureConst {
    //name of collection
    String COLL_INBOX = "inbox";
    String COLL_TEXT_RUN = "text_run";
    String COLL_DIALOG = "dialog";
    String COLL_FRIEND = "friends";

    String FIELD_DEFAULT_ID = "_id";

    //for coll inbox
    String FIELD_INBOX_FROM_ID = "from_id";
    String FIELD_INBOX_TO_ID = "to_id";
    String FIELD_INBOX_TITLE = "title";
    String FIELD_INBOX_CONTENT = "content";
    String FIELD_INBOX_TIMESTAMP = "timestamp";
    String FIELD_INBOX_IS_READ = "is_read";
    String FIELD_INBOX_FROM_NICK_NAME = "from_nick_name";
    String FIELD_INBOX_TO_NICK_NAME = "to_nick_name";
    String FIELD_INBOX_IS_REMOVED = "is_removed";

    //for coll Text Run && Dialog
    String FIELD_MESSAGE_CONTENT    = "content";
    String FIELD_MESSAGE_LINK       = "link";
    String FIELD_MESSAGE_IS_AUTO    = "is_auto";
    String FIELD_MESSAGE_PRIORITY   = "priority";
    String FIELD_MESSAGE_TIME_START = "time_start";
    String FIELD_MESSAGE_TIME_END   = "time_end";
    String FIELD_MESSAGE_LOOP_INTERVAL = "loop_interval";
    String FIELD_MESSAGE_IS_ACTIVE  = "is_active";

    //for coll news
    String FIELD_NEWS_CATEGORY = "category";
    String FIELD_NEWS_CONTENt = "content";
    String FIELD_NEWS_URL_IMAGE = "url_image";
    String FIELD_NEWS_LINK = "link";
    String FIELD_NEWS_IS_ACTIVE = "is_active";

    //for coll friends
    String FIELD_FRIEND_LIST_FRIENDS = "list_friends";
    String FIELD_FRIEND_LIST_FRIENDS_FACEBOOK = "list_friends_facebook";
    String FIELD_FRIEND_LIST_FRIENDS_REQUEST = "list_friends_request";
    String FIELD_FRIEND_LIST_FRIENDS_REJECT = "list_friends_reject";
}
