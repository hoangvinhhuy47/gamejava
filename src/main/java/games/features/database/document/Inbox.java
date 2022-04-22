package games.features.database.document;

import games.features.database.MongoFeatureConst;
import games.features.database.model.MInbox;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;

/**
 * Created by tuanhoang on 8/21/17.
 */
@CollectionClass(MongoFeatureConst.COLL_INBOX)
public class Inbox extends MGDocument implements MongoFeatureConst{
    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private long        id;

    @DocumentField(FIELD_INBOX_FROM_ID)
    private int		fromId;

    @DocumentField(FIELD_INBOX_TO_ID)
    private int		toId;

    @DocumentField(FIELD_INBOX_TITLE)
    private String		title;

    @DocumentField(FIELD_INBOX_CONTENT)
    private String		content;

    @DocumentField(FIELD_INBOX_TIMESTAMP)
    private int      timestamp;

    @DocumentField(FIELD_INBOX_IS_READ)
    private boolean		isRead = false;

    @DocumentField(FIELD_INBOX_FROM_NICK_NAME)
    private String		fromNickName;

    @DocumentField(FIELD_INBOX_TO_NICK_NAME)
    private String		toNickName;

    @DocumentField(FIELD_INBOX_IS_REMOVED)
    private boolean isRemoved = false;

    @Override
    public Inbox updateDB() throws IllegalAccessException {
        MInbox.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public Inbox saveDB() throws IllegalAccessException, NoSuchFieldException {
        MInbox.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    //region getter - setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        changed.put(FIELD_DEFAULT_ID, this.id);
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
        changed.put(FIELD_INBOX_FROM_ID, this.fromId);
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
        changed.put(FIELD_INBOX_TO_ID, this.toId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        changed.put(FIELD_INBOX_TITLE, this.title);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        changed.put(FIELD_INBOX_CONTENT, this.content);
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
        changed.put(FIELD_INBOX_TIMESTAMP, this.timestamp);
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
        changed.put(FIELD_INBOX_IS_READ, this.isRead);
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
        changed.put(FIELD_INBOX_IS_REMOVED, this.isRemoved);
    }

    public String getFromNickName() {
        return fromNickName;
    }

    public void setFromNickName(String fromNickName) {
        this.fromNickName = fromNickName;
        changed.put(FIELD_INBOX_FROM_NICK_NAME, this.fromNickName);
    }

    public String getToNickName() {
        return toNickName;
    }

    public void setToNickName(String toNickName) {
        this.toNickName = toNickName;
        changed.put(FIELD_INBOX_TO_NICK_NAME, this.toNickName);
    }
    //endregion
}
