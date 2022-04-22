package games.api;

import games.core.database.model.MUserInfo;
import games.features.inbox.InboxService;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class MessageSection implements IApiSection {
    private static MessageSection instance;
    InboxService inboxService;
    public static MessageSection getInstance() {
        if (instance == null) instance = new MessageSection();
        return instance;
    }

    private Logger logger;
    MessageSection() {

        inboxService = InboxService.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    @Override
    public ApiReturnObject process(String action, Map<String, Object> data) {
        ApiReturnObject apiReturnObject = new ApiReturnObject();
        switch (action){
            case SEND_ONE:
                try{
                    int userId = Integer.valueOf((String) data.get("user_id"));
                    String title = (String) data.get("title");
                    String content = (String) data.get("content");
                    logger.info("Send one - " + userId + " - title= " + title + " - content = " + content);
                    sendOne(userId, title, content);
                } catch (Exception e){
                    logger.error("Send one error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }

                break;
            case SEND_ALL:
                try{
                    String title = (String) data.get("title");
                    String content = (String) data.get("content");
                    sendAll(title, content);
                } catch (Exception e){
                   logger.error("Send all error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
        }
        return apiReturnObject;
    }

    private void sendOne(int userId, String title, String content) throws Exception {
        //TODO tools send message to userID
        inboxService.sendMessageFromSystem(userId, title, content);
    }

    private void sendAll(String title, String content) throws Exception {
        //TODO tools send message to all
        List<Integer> listUserId  = MUserInfo.getInstance().getListUserId();// new ArrayList<>();
        for (Integer userId : listUserId) {
            inboxService.sendMessageFromSystem(userId, title, content);
        }
    }
}
