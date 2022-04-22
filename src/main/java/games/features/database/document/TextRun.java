package games.features.database.document;

import games.features.database.MongoFeatureConst;
import games.features.database.model.MTextRun;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;
import message.FeatureProtos;
import message.IResponseBean;

/**
 * Created by tuanhoang on 8/22/17.
 */
@CollectionClass(MongoFeatureConst.COLL_TEXT_RUN)
public class TextRun extends MGDocument implements MongoFeatureConst, IResponseBean<FeatureProtos.PushTextRunItemBean> {
    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int         id;

    @DocumentField(FIELD_MESSAGE_CONTENT)
    private String      content = "";

    @DocumentField(FIELD_MESSAGE_LINK)
    private String      link	= "";

    @DocumentField(FIELD_MESSAGE_IS_AUTO)
    private boolean		isAuto;

    @DocumentField(FIELD_MESSAGE_PRIORITY)
    private int			priority;

    @DocumentField(FIELD_MESSAGE_TIME_START)
    private int        timeStart;//second

    @DocumentField(FIELD_MESSAGE_TIME_END)
    private int		timeEnd;//second

    @DocumentField(FIELD_MESSAGE_LOOP_INTERVAL)
    private int				loopInterval;

    @DocumentField(FIELD_MESSAGE_IS_ACTIVE)
    private boolean	isActive;

    public TextRun() {}

    public TextRun(String content, String link, boolean isAuto, int priority, int timeStart, int timeEnd, int loopInterval, boolean isActive) {
        this.setContent(content);
        this.setLink(link);
        this.setAuto(isAuto);
        this.setPriority(priority);
        this.setTimeStart(timeStart);
        this.setTimeEnd(timeEnd);
        this.setLoopInterval(loopInterval);
        this.setActive(isActive);
    }

    @Override
    public MGDocument updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase {
        MTextRun.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        MTextRun.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    //region getter - setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        changed.put(FIELD_DEFAULT_ID, this.id);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        changed.put(FIELD_MESSAGE_CONTENT, this.content);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
        changed.put(FIELD_MESSAGE_LINK, this.link);
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean auto) {
        isAuto = auto;
        changed.put(FIELD_MESSAGE_IS_AUTO, this.isAuto);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        changed.put(FIELD_MESSAGE_PRIORITY, this.priority);
    }

    public int getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(int timeStart) {
        this.timeStart = timeStart;
        changed.put(FIELD_MESSAGE_TIME_START, this.timeStart);
    }

    public int getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(int timeEnd) {
        this.timeEnd = timeEnd;
        changed.put(FIELD_MESSAGE_TIME_END, this.timeEnd);
    }

    public int getLoopInterval() {
        return loopInterval;
    }

    public void setLoopInterval(int loopInterval) {
        this.loopInterval = loopInterval;
        changed.put(FIELD_MESSAGE_LOOP_INTERVAL, this.loopInterval);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        changed.put(FIELD_MESSAGE_IS_ACTIVE, this.isActive);
    }

    @Override
    public FeatureProtos.PushTextRunItemBean parseResponseBeanBuilder() {
        FeatureProtos.PushTextRunItemBean.Builder builder = FeatureProtos.PushTextRunItemBean.newBuilder();
        builder.setContent(this.content);
        builder.setLink(this.link);
        builder.setStartTime(this.timeStart);
        builder.setEndTime(this.timeEnd);
        builder.setLoop(this.loopInterval);
        builder.setPriority(this.priority);
        return builder.build();
    }

    //endregion
}
