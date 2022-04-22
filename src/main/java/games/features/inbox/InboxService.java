package games.features.inbox;

import games.core.database.document.UserInfo;
import games.core.user.User;
import games.core.user.UserService;
import games.features.constant.ResponseFeatureErrorCode;
import games.features.database.document.Inbox;
import games.features.database.model.MInbox;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by tuanhoang on 8/21/17.
 */
public class InboxService implements ResponseFeatureErrorCode {
    private static InboxService instance;
    private MInbox mInbox;
    private Logger logger;

    private static int LIMIT_COUNT_INBOX = 30;
    public static int SYSTEM_ID = -1;
    private static String SYSTEM_NAME = "Hệ thống";
    public static InboxService getInstance() {
        if (instance == null) instance = new InboxService();
        return instance;
    }

    InboxService() {
        this.mInbox = MInbox.getInstance();
        this.logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    public int sendMessageFromSystem(int toId, String title, String content) {
        return this.sendMessage(SYSTEM_ID, toId, title, content);
    }

    public int sendMessage(int fromId, int toId, String title, String content) {
        String fromNickname = "";
        String toNickname = "";
        if (fromId == SYSTEM_ID) {
            fromNickname = SYSTEM_NAME;
        } else {
            UserInfo fromUser = new UserInfo(fromId);

            try {
                fromUser.updateDB();
                fromNickname = fromUser.getNickName();
            } catch (IllegalAccessException e) {
                logger.error("Get user with from id error - " + fromId);
                return REC_INBOX_USER_NOT_EXIST;
            }
        }

        if (toId == SYSTEM_ID) {
            toNickname = SYSTEM_NAME;
        } else {
            User user = UserService.getInstance().getUser(toId);
            if (user != null) {
                toNickname = user.getNickName();
                user.addEmail();
            } else {
                UserInfo toUser = new UserInfo(toId);
                try {
                    toUser.updateDB();
                    toNickname = toUser.getNickName();
                } catch (IllegalAccessException e) {
                    logger.error("Get user with from id error - " + toId);
                    return REC_INBOX_USER_NOT_EXIST;
                }
            }
        }
        int timestamp = (int)ZonedDateTime.now().toEpochSecond();
        try {
            mInbox.createInbox(fromId, toId, title, content, timestamp, fromNickname, toNickname);
        } catch (Exception e) {
            logger.error("Create inbox error - " + toId);
            return REC_NOT_DEFINE;
        }
        return REC_SUCCESS;
    }

    public List<Inbox> getListInbox(int userId) {
        return mInbox.getListInbox(userId, LIMIT_COUNT_INBOX);
    }

    public int openInbox(int userId, long inboxId) {
        try {
            Inbox inbox = mInbox.getInboxById(inboxId);
            if (inbox.getToId() == userId && !inbox.isRead()) {
                inbox.setRead(true);
                inbox.saveDB();

                User user = UserService.getInstance().getUser(userId);
                user.readEmail();
                return REC_SUCCESS;
            } else {
                return REC_INBOX_CANT_OPEN_INBOX_OF_OTHER;
            }
        } catch (Exception e) {
            logger.error("Open inbox error - ", e);
            return REC_INBOX_NOT_EXIST;
        }
    }

    public int removeInbox(int userId, long inboxId) {
        try {
            Inbox inbox = mInbox.getInboxById(inboxId);
            if (inbox.getToId() == userId) {
                inbox.setRemoved(true);
                inbox.saveDB();
                return REC_SUCCESS;
            } else {
                return REC_INBOX_CANT_REMOVE_INBOX_OF_OTHER;
            }
        } catch (Exception e) {
            logger.error("Open inbox error - ", e);
            return REC_INBOX_NOT_EXIST;
        }
    }
}
