package games.minigame.database.document;

import games.minigame.database.MongoMiniGameConst;
import games.minigame.database.model.MGiftCode;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;

import java.time.ZonedDateTime;

/**
 * Created by tuanhoang on 8/17/17.
 * Db của gifcode.
 *
 */
@CollectionClass(MongoMiniGameConst.COLL_GIFT_CODE)
public class GiftCode extends MGDocument implements MongoMiniGameConst {
    @DocumentField(FIELD_GC_CODE)
    @DocumentFieldPK
    String code = "";

    @DocumentField(FIELD_GC_CODE_NAME)
    String name = "";

    @DocumentField(FIELD_GC_GOLD_REWARD)
    long goldReward = 0;

    @DocumentField(FIELD_GC_SILVER_REWARD)
    long silverReward = 0;

    @DocumentField(FIELD_GC_TICKET_WHEEL)
    int ticketWheel = 0;

    @DocumentField(FIELD_GC_TIME_CREATE)
    long timeCreate = 0;//thoi gian khoi tao

    @DocumentField(FIELD_GC_IS_USED)
    boolean isUsed = false;//true: da su dung roi

    @DocumentField(FIELD_GC_USER_ID)
    long userId = 0;//nguoi su dung code

    @DocumentField(FIELD_GC_TIME_ACTIVE)
    long timeActive = 0;//thoi gian nhap code

    @Override
    public MGDocument updateDB() throws IllegalAccessException {
        MGiftCode.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        MGiftCode.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    public GiftCode() {

    }

    public GiftCode(String code, String name, long goldReward, long silverReward, int ticketWheel) {
        this.setCode(code.toUpperCase());
        this.setName(name);
        this.setGoldReward(goldReward);
        this.setSilverReward(silverReward);
        this.setTicketWheel(ticketWheel);
        this.setTimeCreate(ZonedDateTime.now().toEpochSecond());
        this.setTimeActive(0);
        this.setUserId(0);
        this.setUsed(false);
        this.setTimeActive(0);
    }

    public GiftCode(String code) {
        this.setCode(code);
    }

    //region getter - setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.changed.put(FIELD_GC_CODE, this.code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.changed.put(FIELD_GC_CODE_NAME, this.name);
    }

    public long getGoldReward() {
        return goldReward;
    }

    public void setGoldReward(long goldReward) {
        this.goldReward = goldReward;
        this.changed.put(FIELD_GC_GOLD_REWARD, this.goldReward);
    }

    public long getSilverReward() {
        return silverReward;
    }

    public void setSilverReward(long silverReward) {
        this.silverReward = silverReward;
        this.changed.put(FIELD_GC_SILVER_REWARD, this.silverReward);
    }

    public int getTicketWheel() {
        return ticketWheel;
    }

    public void setTicketWheel(int ticketWheel) {
        this.ticketWheel = ticketWheel;
        this.changed.put(FIELD_GC_TICKET_WHEEL, this.ticketWheel);
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
        this.changed.put(FIELD_GC_TIME_CREATE, this.timeCreate);
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
        this.changed.put(FIELD_GC_IS_USED, this.isUsed);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
        this.changed.put(FIELD_GC_USER_ID, this.userId);
    }

    public long getTimeActive() {
        return timeActive;
    }

    public void setTimeActive(long timeActive) {
        this.timeActive = timeActive;
        this.changed.put(FIELD_GC_TIME_ACTIVE, this.timeActive);
    }

    //endregion
}
