package games.cashout.database.model;

import games.cashout.database.MongoCashoutConst;
import games.cashout.database.document.CashoutReciept;
import games.core.database.MongoManager;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 8/31/17.
 */
@CollectionClass(MongoCashoutConst.COLL_CASHOUT)
public class MCashoutReciept extends MGModelAdapter<CashoutReciept> implements MongoCashoutConst {
    public static MCashoutReciept instance;

    int receiptIdCur = 0;

    public static MCashoutReciept getInstance() {
        if (instance == null) instance = new MCashoutReciept();
        return instance;
    }

    protected MCashoutReciept() {
        super(MongoManager.getInstance().getDatabase());
        receiptIdCur = (int)this.getCountCollection();
    }

    public void addReciept(int userId, int type, String productId, int quantity) throws NoSuchFieldException, IllegalAccessException {
        CashoutReciept cashoutReciept = new CashoutReciept();
        cashoutReciept.setProduct_id(productId);
        cashoutReciept.setQuantity(quantity);
        cashoutReciept.setUserId(userId);
        cashoutReciept.setType(type);
        cashoutReciept.setVerify(false);
        cashoutReciept.setTimeRequest((int)ZonedDateTime.now().toEpochSecond());
        cashoutReciept.setRecieptId(receiptIdCur + 1);
        cashoutReciept.setStatus(0);
        receiptIdCur ++;
        this.saveToMongo(cashoutReciept);
    }

    public List<CashoutReciept> listCashoutNeedVeriy(int userId, int type) {
        List<Document> listDoc = this.mgCollection.find(new Document(FIELD_CO_TYPE, type)
                .append(FIELD_CO_USER_ID, userId)
                .append(FIELD_CO_USER_VERIFY, false))
                .into(new ArrayList<>());

        List<CashoutReciept> listCashouts = new ArrayList<>();
        for (Document document : listDoc) {
            CashoutReciept cashoutReciept = new CashoutReciept();
            try {
                this.createByDocument(cashoutReciept, document);
                listCashouts.add(cashoutReciept);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return listCashouts;
    }

    public int getReceiptIdCur() {
        return receiptIdCur;
    }
}
