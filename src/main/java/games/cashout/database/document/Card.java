package games.cashout.database.document;

import games.cashout.database.MongoCashoutConst;
import games.cashout.database.model.MCard;
import games.payment.database.document.DPaymentHistory;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;

/**
 * Created by tuanhoang on 8/31/17.
 */
@CollectionClass(MongoCashoutConst.COLL_CARD)
public class Card extends MGDocument implements  MongoCashoutConst{

    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private String cardCode = "";

    @DocumentField(FIELD_CARD_SERIAL)
    private String cardSerial = "";

    @DocumentField(FIELD_CARD_TYPE)
    private String cardType = "";

    @DocumentField(FIELD_CARD_VALUE)
    private int cardValue = 0;

    @DocumentField(FIELD_CARD_TIME_ACTIVE)
    private int timeActive = 0;

    @DocumentField(FIELD_CARD_TIME_USED)
    private int timeUsed = 0;

    @DocumentField(FIELD_CARD_IS_USED)
    private boolean isUsed = false;

    @DocumentField(FIELD_CARD_USER_ID)
    private int userId = 0;

    @DocumentField(FIELD_CARD_RECEIPT_ID)
    private int recieptId = 0;

    public  Card() {}

    public  Card(String cardCode, String cardSerial, String cardType, int cardValue, int timeActive) {
        this.setCardCode(cardCode);
        this.setCardSerial(cardSerial);
        this.setCardType(cardType);
        this.setTimeActive(timeActive);
        this.setCardValue(cardValue);
        this.setUsed(false);
    }

    @Override
    public Card updateDB() throws NullCollection, NotFoundDocumentInDatabase {
        MCard.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public Card saveDB() {
        MCard.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    //region getter -  setter

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
        this.changed.put(FIELD_DEFAULT_ID, this.cardCode);
    }

    public String getCardSerial() {
        return cardSerial;
    }

    public void setCardSerial(String cardSerial) {
        this.cardSerial = cardSerial;
        this.changed.put(FIELD_CARD_SERIAL, this.cardSerial);
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
        this.changed.put(FIELD_CARD_TYPE, this.cardType);
    }

    public int getCardValue() {
        return cardValue;
    }

    public void setCardValue(int cardValue) {
        this.cardValue = cardValue;
        this.changed.put(FIELD_CARD_VALUE, this.cardValue);
    }

    public int getTimeActive() {
        return timeActive;
    }

    public void setTimeActive(int timeActive) {
        this.timeActive = timeActive;
        this.changed.put(FIELD_CARD_TIME_ACTIVE, this.timeActive);
    }

    public int getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(int timeUsed) {
        this.timeUsed = timeUsed;
        this.changed.put(FIELD_CARD_TIME_USED, this.timeUsed);
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
        this.changed.put(FIELD_CARD_IS_USED, this.isUsed);
    }

    public int getUserId() {
        return userId;

    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.changed.put(FIELD_CARD_USER_ID, this.userId);
    }

    public int getRecieptId() {
        return recieptId;
    }

    public void setRecieptId(int recieptId) {
        this.recieptId = recieptId;
        this.changed.put(FIELD_CARD_RECEIPT_ID, this.recieptId);
    }

    public void use(DPaymentHistory dPaymentHistory){
        this.setUsed(true);
        this.setUserId(dPaymentHistory.getUserId());
        this.setTimeUsed((int)(System.currentTimeMillis()/1000));
        this.setRecieptId(dPaymentHistory.getId());
        this.saveDB();
    }
    //endregion
}
