package games.payment.database.model;

import games.core.database.MongoConst;
import games.core.database.MongoManager;
import games.payment.database.document.DPaymentHistory;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


@CollectionClass(MongoConst.COLL_PAYMENT_HISTORY)
public class MPaymentHistory extends MGModelAdapter<DPaymentHistory> implements MongoConst {
    public static MPaymentHistory instance;
    public static MPaymentHistory getInstance() {
        if (instance == null) instance = new MPaymentHistory();
        return instance;
    }
    private MPaymentHistory() {
        super(MongoManager.getInstance().getDatabase());
    }

    private DPaymentHistory logPayment(int type, long beforeValue, long value, int userId, int otherUserId, String note){
        DPaymentHistory paymentHistory = new DPaymentHistory();
        paymentHistory.setId((int)getNextId());
        paymentHistory.setType(type);
        paymentHistory.setNote(note);
        paymentHistory.setSenderId(userId);
        paymentHistory.setReceiverId(otherUserId);
        paymentHistory.setValue(value);
        paymentHistory.setBeforeValue(beforeValue);
        paymentHistory.setTimestamp((int)(System.currentTimeMillis() / 1000));
        if (value < 0){
            paymentHistory.setUserId(userId);
        } else {
            paymentHistory.setUserId(otherUserId);
        }
        paymentHistory.saveDB();
        return paymentHistory;
    }

    public void logTransfer(long beforeValue, long beforeReceive, long value, long realValue, int senderId, int receiverId, String note){
        logPayment(DPaymentHistory.TRANSFER, beforeValue, -value, senderId, receiverId, note);
        logPayment(DPaymentHistory.TRANSFER_RECEIVE, beforeReceive, realValue, senderId, receiverId, note);
    }

    public void logAdminTransfer(long beforeValue, long value, int userId, String note) {
        if (value >= 0)
        {
            logPayment(DPaymentHistory.TRANSFER_RECEIVE, beforeValue, value, -1, userId, note);
        } else {
            logPayment(DPaymentHistory.TRANSFER, beforeValue, value, userId, -1, note);
        }
    }

    public DPaymentHistory logCardIn(
            long beforeValue, long value, int userId,
            String cardType, int cardValue,
            String cardCode, String cardSerial,
            String requestStatus, String verifyStatus,
            String note, int status){
        if (value < 0){
            value = -value;
        }
        DPaymentHistory paymentHistory = logPayment(DPaymentHistory.CARD_IN, beforeValue, value, userId, -1, note);
        paymentHistory.setUserId(userId);
        paymentHistory.setCardType(cardType);
        paymentHistory.setCardValue(cardValue);
        paymentHistory.setCardCode(cardCode);
        paymentHistory.setCardSerial(cardSerial);
        paymentHistory.setRequestStatus(requestStatus);
        paymentHistory.setVerifyStatus(verifyStatus);
        paymentHistory.setStatus(status);
        paymentHistory.saveDB();
        return paymentHistory;
    }

    public DPaymentHistory logCashout(String cardType, int cardValue, long beforeValue, long value, int userId, String note, int status){
        if (value > 0){
            value = -value;
        }
        DPaymentHistory dPaymentHistory = logPayment(DPaymentHistory.CASHOUT, beforeValue, value, userId, -1, note);
        dPaymentHistory.setStatus(status);
        dPaymentHistory.setCardValue(cardValue);
        dPaymentHistory.setCardType(cardType);
        dPaymentHistory.saveDB();
        return dPaymentHistory;
    }

    public DPaymentHistory logCashout(String cardType, int cardValue, long beforeValue, long value, int userId, String note){
       return logCashout(cardType, cardValue, beforeValue, value, userId, note, DPaymentHistory.SUCCESS);
    }

    public List<DPaymentHistory> getByUserId(int userId, int limit){
        Document filter = new Document(FIELD_USER_ID, userId);
        Document sort = new Document(FIELD_TIMESTAMP, -1);
        List<Document> listHistory = mgCollection.find(filter).sort(sort).limit(limit).into(new ArrayList<>());
        List<DPaymentHistory> result = new ArrayList<>();
        for (Document document : listHistory) {
            DPaymentHistory dPaymentHistory = new DPaymentHistory();
            try {
                createByDocument(dPaymentHistory, document);
                result.add(dPaymentHistory);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
