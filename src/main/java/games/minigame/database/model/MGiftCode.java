package games.minigame.database.model;

import games.core.database.MongoManager;
import games.minigame.database.MongoMiniGameConst;
import games.minigame.database.document.GiftCode;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

/**
 * Created by tuanhoang on 8/17/17.
 */
@CollectionClass(MongoMiniGameConst.COLL_GIFT_CODE)
public class MGiftCode extends MGModelAdapter<GiftCode> implements MongoMiniGameConst{
    public static MGiftCode instance;
    public static MGiftCode getInstance() {
        if (instance == null) instance = new MGiftCode();
        return instance;
    }

    protected MGiftCode() {
        super(MongoManager.getInstance().getDatabase());
    }

    public GiftCode getGifeCodeByCode(String code) {
        GiftCode giftCode = new GiftCode(code);
        try {
            this.updateFromMongo(giftCode);
            return giftCode;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkUsedCodeWithName(int userId, String name) {
//        GiftCode giftCode = new GiftCode();
//        giftCode.setName(name);
        Document querry = new Document().append(FIELD_GC_USER_ID, userId).append(FIELD_GC_CODE_NAME, name);
        Document result = this.mgCollection.find(querry).first();
        if (result != null) {
            return true;
        }
        return  false;
    }
}
