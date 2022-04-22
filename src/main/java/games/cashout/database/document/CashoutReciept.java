package games.cashout.database.document;

import games.cashout.database.MongoCashoutConst;
import games.cashout.database.model.MCashoutReciept;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;

/**
 * Created by tuanhoang on 8/31/17.
 */
@CollectionClass(MongoCashoutConst.COLL_CASHOUT)
public class CashoutReciept extends MGDocument implements  MongoCashoutConst {

    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int recieptId = 0;

    @DocumentField(FIELD_CO_USER_ID)
    private int userId = 0;

    @DocumentField(FILED_CO_TIME_REQUEST)
    private int timeRequest = 0;//timestamp

    @DocumentField(FIELD_CO_TIME_ACCEPT)
    private int timeAccept = 0;//timestamp

    //User xác nhận
    @DocumentField(FIELD_CO_USER_VERIFY)
    private boolean isVerify = false;//

    @DocumentField(FIELD_CO_APPROVER)
    private String approver = "";

    @DocumentField(FIELD_CO_TYPE)
    private int type = 0;

    @DocumentField(FIELD_CO_PRODUCT_ID)
    private String product_id = "";

    @DocumentField(FIELD_CO_QUANTITY)
    private int quantity = 0;

    @DocumentField(FIELD_RECEIPT_STATUS)
    private int status = 0;

    public CashoutReciept() {

    }

    public CashoutReciept(int recieptId) {
        this.recieptId = recieptId;
    }

    @Override
    public MGDocument updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase {
        MCashoutReciept.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        MCashoutReciept.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    //region getter - setter

    public int getRecieptId() {
        return recieptId;
    }

    public void setRecieptId(int recieptId) {
        this.recieptId = recieptId;
        this.changed.put(FIELD_DEFAULT_ID, this.recieptId);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.changed.put(FIELD_CO_USER_ID, this.userId);
    }

    public int getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(int timeRequest) {
        this.timeRequest = timeRequest;

    }

    public boolean isVerify() {
        return isVerify;
    }

    public void setVerify(boolean verify) {
        isVerify = verify;
        this.changed.put(FIELD_CO_USER_VERIFY, this.isVerify);
    }

    public int getTimeAccept() {
        return timeAccept;
    }

    public void setTimeAccept(int timeAccept) {
        this.timeAccept = timeAccept;
        this.changed.put(FIELD_CO_TIME_ACCEPT, this.timeAccept);
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
        this.changed.put(FIELD_CO_APPROVER, this.approver);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        this.changed.put(FIELD_CO_TYPE, this.type);
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
        this.changed.put(FIELD_CO_PRODUCT_ID, this.product_id);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.changed.put(FIELD_CO_QUANTITY, this.quantity);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        this.changed.put(FIELD_RECEIPT_STATUS, status);
    }

    //endregion
}
