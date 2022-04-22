package games.features.database.model;

import games.core.database.MongoManager;
import games.features.database.MongoFeatureConst;
import games.features.database.document.TextRun;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tuanhoang on 8/23/17.
 */
@CollectionClass(MongoFeatureConst.COLL_TEXT_RUN)
public class MTextRun extends MGModelAdapter<TextRun> implements MongoFeatureConst{
    private static MTextRun instance;
    public static MTextRun getInstance() {
        if (instance == null) instance = new MTextRun();
        return instance;
    }

    int textRunIdCur = 0;
    protected MTextRun() {
        super(MongoManager.getInstance().getDatabase());
        textRunIdCur = (int)getMaxIDCurrent();
    }

    @Override
    public long getMaxIDCurrent() {
        Document document = getDocumentWithIdMax();
        if (document != null && document.containsKey(FIELD_DEFAULT_ID)) {
            int id = document.getInteger(FIELD_DEFAULT_ID);
            return id;
        }
        return 0;
    }

    public void addTextRun(TextRun textRun) throws NoSuchFieldException, IllegalAccessException {
        int id = ++textRunIdCur;
        textRun.setId(id);
        this.saveToMongo(textRun);
    }

    public BlockingQueue<TextRun> getTextRunActives(int limit) {
        int timeCurrent = (int) ZonedDateTime.now().toEpochSecond();
        BlockingQueue<TextRun> textRunList = new ArrayBlockingQueue<TextRun>(limit);
        List<Document> listDocument = this.mgCollection.find(new Document(FIELD_MESSAGE_IS_ACTIVE, true).append(FIELD_MESSAGE_TIME_END, new Document("$gt", timeCurrent)))
            .sort(new Document(FIELD_MESSAGE_TIME_END, -1)).limit(limit)
                .into(new ArrayList<>());
        for (Document document : listDocument) {
            TextRun textRun = new TextRun();
            try {
                this.createByDocument(textRun, document);
                textRunList.add(textRun);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return textRunList;
    }
}
