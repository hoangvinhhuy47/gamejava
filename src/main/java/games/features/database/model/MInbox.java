package games.features.database.model;

import games.core.database.MongoManager;
import games.features.database.MongoFeatureConst;
import games.features.database.document.Inbox;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 8/21/17.
 */
@CollectionClass(MongoFeatureConst.COLL_INBOX)
public class MInbox extends MGModelAdapter<Inbox> implements MongoFeatureConst{
    private static MInbox instance;
    public static MInbox getInstance() {
        if (instance == null) instance = new MInbox();
        return instance;
    }
    long inboxIdCur = 0;
    protected MInbox() {
        super(MongoManager.getInstance().getDatabase());
        inboxIdCur = this.getMaxIDCurrent();
    }

    public Inbox getInboxById(long inboxId) throws IllegalAccessException {
        Inbox inbox = new Inbox();
        inbox.setId(inboxId);
        this.updateFromMongo(inbox);
        return inbox;
    }



    public Inbox createInbox(int fromId, int toId, String title, String content, int timestamp, String fromNickName, String toNickName) throws NoSuchFieldException, IllegalAccessException {
        long id = ++inboxIdCur;
        Inbox inbox = new Inbox();
        inbox.setId(id);
        inbox.setFromId(fromId);
        inbox.setToId(toId);
        inbox.setTitle(title);
        inbox.setContent(content);
        inbox.setTimestamp(timestamp);
        inbox.setFromNickName(fromNickName);
        inbox.setToNickName(toNickName);
        inbox.setRead(false);
        inbox.setRemoved(false);
        this.saveToMongo(inbox);
        return inbox;
    }

    public int countEmailNotReadOfUser(int userId) {
        List<Document> listDoc = this.mgCollection.find(new Document(FIELD_INBOX_TO_ID, userId).append(FIELD_INBOX_IS_REMOVED, false).append(FIELD_INBOX_IS_READ, false))
                .into(new ArrayList<>());
        return listDoc.size();
    }

    public List<Inbox> getListInbox(int toId, int limit)
    {
        List<Document> listDoc = this.mgCollection.find(new Document(FIELD_INBOX_TO_ID, toId).append(FIELD_INBOX_IS_REMOVED, false))
                .sort(new Document(FIELD_INBOX_TIMESTAMP, 1)).limit(limit).into(new ArrayList<>());
        List<Inbox> listInbox = new ArrayList<>();
        for (Document document : listDoc) {
            Inbox inbox = new Inbox();
            try {
                this.createByDocument(inbox, document);
                listInbox.add(inbox);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return listInbox;
    }
}
