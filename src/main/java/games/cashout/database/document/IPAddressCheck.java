package games.cashout.database.document;

import games.cashout.database.MongoCashoutConst;
import games.cashout.database.model.MIPAddressCheck;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;
import libs.util.Helper;
import org.bson.Document;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tuanhoang on 9/1/17.
 */
@CollectionClass(MongoCashoutConst.COLL_IP_CHECK)
public class IPAddressCheck extends MGDocument implements MongoCashoutConst {

    @DocumentFieldPK
    @DocumentField(FIELD_DEFAULT_ID)
    String ipAddress = "";

    @DocumentField(FIELD_IP_LIST_USER_ID)
    Document listUserId = new Document();

    @DocumentField(FIELD_IP_LIST_CARD_RECIEVED)
    Document listCardRecieved = new Document();

    @DocumentField(FIELD_IP_COUNT_CARD)
    int countCard = 0;

    public IPAddressCheck() {

    }

    public IPAddressCheck(String ip) {
        this.ipAddress = ip;
    }

    @Override
    public MGDocument updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase {
        MIPAddressCheck.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        MIPAddressCheck.getInstance().saveToMongo(this);
        return this;
    }

    //region getter - setter

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        this.changed.put(FIELD_DEFAULT_ID, this.ipAddress);
    }

    public List<Integer> getListUserIds() {
        List<Integer> listUser = new ArrayList<>();
        for (Map.Entry key : this.listUserId.entrySet()) {
            int userID = Integer.parseInt(String.valueOf(key.getKey()));
            if (userID > 0) {
                listUser.add(userID);
            }
        }
        return listUser;
    }

    public boolean addUserId(int userId) {
        if (this.listUserId.containsKey(userId)) {
            return false;
        } else {
            this.listUserId.put(String.valueOf(userId), ZonedDateTime.now().toEpochSecond());
            this.setListUserId(this.listUserId);
            return true;
        }
    }

    public Document getListUserId() {
        return listUserId;
    }

    public void setListUserId(Document listUserId) {
        this.listUserId = listUserId;
        this.changed.put(FIELD_IP_LIST_USER_ID, this.listUserId);
    }


    public void addCard(int userId, String card) {
        String value = userId + "_" + card;
        this.listCardRecieved.put(ZonedDateTime.now().toEpochSecond() + "", value);
        this.countCard ++;

        this.setListCardRecieved(listCardRecieved);
        this.setCountCard(countCard);
    }

    //
    public boolean checkCardOneDay(int limitOneDay) {
        int size = this.listCardRecieved.size();
        if (size >= limitOneDay) {
            Set<String> keyTimes = this.listCardRecieved.keySet();
            List<String> listTimes = new ArrayList<>(keyTimes);
            String lastTime = listTimes.get(size - 1);
            String fistTime = listTimes.get(size - limitOneDay);

            int timeStart = Integer.parseInt(fistTime);
            int timeEnd = Integer.parseInt(lastTime);

            //3 lần nhưng chưa đủ 3 ngày
            if (timeEnd - timeStart < Helper.TOTAL_SECOND_ONE_DAY) {
                return false;
            }
        }

        return true;
    }

    public Document getListCardRecieved() {
        return listCardRecieved;
    }

    public void setListCardRecieved(Document listCardRecieved) {
        this.listCardRecieved = listCardRecieved;
        this.changed.put(FIELD_IP_LIST_CARD_RECIEVED, this.listCardRecieved);
    }

    public int getCountCard() {
        return countCard;
    }

    public void setCountCard(int countCard) {
        this.countCard = countCard;
        this.changed.put(FIELD_IP_COUNT_CARD, this.countCard);
    }

    //endregion
}
