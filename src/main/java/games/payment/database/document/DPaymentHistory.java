package games.payment.database.document;

import games.cashout.database.document.Card;
import games.cashout.database.model.MCard;
import games.core.database.MongoConst;
import games.core.proto.ProtoSerializer;
import games.payment.database.model.MPaymentHistory;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import message.ShopProtos;


@CollectionClass(MongoConst.COLL_PAYMENT_HISTORY)
public class DPaymentHistory extends MGDocument implements MongoConst, ProtoSerializer<ShopProtos.HistoryPaymentInfoBean> {
    public static int CARD_IN = 1;
    public static int CASHOUT = 2;
    public static int TRANSFER = 3;
    public static int TRANSFER_RECEIVE = 4;
    public static int ADMIN_ADD = 5;
    public static int ADMIN_SUB = 6;

    public static int SUCCESS = 0;
    public static int PENDING = 1;
    public static int REJECT = -1;
    @DocumentFieldPK
    @DocumentField(FIELD_DEFAULT_ID)
    private int id;

    @DocumentField(FIELD_PAYMENT_TYPE)
    private int type;

    @DocumentField(FIELD_PAYMENT_BEFORE_VALUE)
    private long beforeValue;

    @DocumentField(FIELD_PAYMENT_VALUE)
    private long value;

    @DocumentField(FIELD_PAYMENT_AFTER_VALUE)
    private long afterValue;

    @DocumentField(FIELD_PAYMENT_NOTE)
    private String note;

    @DocumentField(FIELD_PAYMENT_SENDER_ID)
    private int senderId;

    @DocumentField(FIELD_PAYMENT_RECEIVER_ID)
    private int receiverId;

    @DocumentField(FIELD_USER_ID)
    private int userId;

    @DocumentField(FIELD_TIMESTAMP)
    private int timestamp;

    @DocumentField(FIELD_PAYMENT_STATUS)
    private int status;

    @DocumentField(FIELD_PAYMENT_CARD_TYPE)
    private String cardType;

    @DocumentField(FIELD_PAYMENT_CARD_VALUE)
    private int cardValue;

    @DocumentField(FIELD_PAYMENT_CARD_CODE)
    private String cardCode;

    @DocumentField(FIELD_PAYMENT_CARD_SERIAL)
    private String cardSerial;

    @DocumentField(FIELD_PAYMENT_REQUEST_STATUS)
    private String requestStatus;

    @DocumentField(FIELD_PAYMENT_VERIFY_STATUS)
    private String verifyStatus;

    @DocumentField(FIELD_PAYMENT_ACCEPTED_TIME)
    private int acceptedTimestamp;

    @DocumentField(FIELD_PAYMENT_APPROVER)
    private String approver;


    public DPaymentHistory(){

    }

    public DPaymentHistory(int id){
        this.setId(id);
        this.updateDB();
    }

    @Override
    public DPaymentHistory updateDB() throws NotFoundDocumentInDatabase {
        MPaymentHistory.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public DPaymentHistory saveDB() {
        MPaymentHistory.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    public boolean isCashout(){
        return this.getType() == CASHOUT;
    }

    public void accept(String approver){
        if (this.getType() == CASHOUT){
            this.setAcceptedTimestamp((int)(System.currentTimeMillis()/1000));
            this.setStatus(SUCCESS);
            this.setApprover(approver);
            this.saveDB();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.changed.put(FIELD_DEFAULT_ID, id);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.changed.put(FIELD_PAYMENT_TYPE, type);
        this.type = type;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.changed.put(FIELD_PAYMENT_VALUE, value);
        this.value = value;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.changed.put(FIELD_PAYMENT_NOTE, note);
        this.note = note;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.changed.put(FIELD_PAYMENT_SENDER_ID, senderId);
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.changed.put(FIELD_PAYMENT_RECEIVER_ID, receiverId);
        this.receiverId = receiverId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.changed.put(FIELD_USER_ID, userId);
        this.userId = userId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.changed.put(FIELD_TIMESTAMP, timestamp);
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.changed.put(FIELD_PAYMENT_STATUS, status);
        this.status = status;
    }

    public long getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(long beforeValue) {
        this.setAfterValue(beforeValue + this.value);
        this.changed.put(FIELD_PAYMENT_BEFORE_VALUE, beforeValue);
        this.beforeValue = beforeValue;
    }

    private long getAfterValue() {
        return afterValue;
    }

    private void setAfterValue(long value){
        this.changed.put(FIELD_PAYMENT_AFTER_VALUE, value);
        this.afterValue = value;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
        this.changed.put(FIELD_PAYMENT_CARD_TYPE, cardType);
    }

    public int getCardValue() {
        return cardValue;
    }

    public void setCardValue(int cardValue) {
        this.cardValue = cardValue;
        this.changed.put(FIELD_PAYMENT_CARD_VALUE, cardValue);
    }

    public int getAcceptedTimestamp() {
        return acceptedTimestamp;
    }

    public void setAcceptedTimestamp(int acceptedTimestamp) {
        this.acceptedTimestamp = acceptedTimestamp;
        this.changed.put(FIELD_PAYMENT_ACCEPTED_TIME, acceptedTimestamp);
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.changed.put(FIELD_PAYMENT_APPROVER, approver);
        this.approver = approver;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.changed.put(FIELD_PAYMENT_CARD_CODE, cardCode);
        this.cardCode = cardCode;
    }

    public String getCardSerial() {
        return cardSerial;
    }

    public void setCardSerial(String cardSerial) {
        this.changed.put(FIELD_PAYMENT_CARD_SERIAL, cardSerial);
        this.cardSerial = cardSerial;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.changed.put(FIELD_PAYMENT_REQUEST_STATUS, requestStatus);
        this.requestStatus = requestStatus;
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.changed.put(FIELD_PAYMENT_VERIFY_STATUS, verifyStatus);
        this.verifyStatus = verifyStatus;
    }

    @Override
    public ShopProtos.HistoryPaymentInfoBean.Builder parseProtoBuilder() {
        ShopProtos.HistoryPaymentInfoBean.Builder builder = ShopProtos.HistoryPaymentInfoBean.newBuilder();
        //if(this.getType() == )
        String note = "Coin sau gd: " + this.getAfterValue();
        builder.setNote(note);//this.getNote()
        builder.setType(this.getType());
        builder.setTimeProcess(this.getTimestamp());
        builder.setStatus(this.getStatus());
        builder.setValue((int)this.getValue());
        builder.setBeforeValue((int)this.getBeforeValue());
        builder.setAfterValue((int)this.getAfterValue());
//        builder.setCardType(this.getCardType());
//        builder.setCardValue(this.getCardValue());
        return builder;
    }

    public Card getValidCard(){
        return MCard.getInstance().getValidCardBy(this.getCardType(), this.getCardValue());
    }
}
