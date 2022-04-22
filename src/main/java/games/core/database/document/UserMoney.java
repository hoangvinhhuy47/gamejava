package games.core.database.document;

import games.core.database.MongoConst;
import games.core.database.model.MUserMoney;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
@CollectionClass(MongoConst.COLL_USER_MONEY)
public class UserMoney extends MGDocument implements MongoConst{


    public UserMoney(int playerId)
    {
        this.id = playerId;
    }
    public UserMoney() {

    }
    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int id;

    @DocumentField(FIELD_USER_GOLD)
    private long moneyGold;

    @DocumentField(FIELD_DEPOSIT_FROM_WEB)
    private long depositFrWeb;

    @DocumentField(FIELD_DEPOSIT_FROM_WALLET)
    private long depositFrWallet;

    @DocumentField(FIELD_DEPOSIT_FROM_WEB_PROVISIONAL)
    private long depositFrWebProvisional;

    @DocumentField(FIELD_DEPOSIT_FROM_WALLET_PROVISIONAL)
    private long depositFrWalletProvisional;

    @DocumentField(FIELD_USER_SILVER)
    private long moneySilver;

    @DocumentField(FIELD_WIN_GOLD)
    private long winGold;

    @DocumentField(FIELD_WIN_SILVER)
    private long winSilver;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.changed.put(FIELD_DEFAULT_ID, this.getId());
    }

    public long getMoneyGold() {
        return moneyGold;
    }

    public void setMoneyGold(long moneyGold) {
        this.moneyGold = moneyGold;
        this.changed.put(FIELD_USER_GOLD, this.getMoneyGold());
    }

    public long getDepositFrWeb() {
        return depositFrWeb;
    }
    public void setDepositFrWeb(long deposit) {
        this.depositFrWeb = deposit;
        this.changed.put(FIELD_DEPOSIT_FROM_WEB, this.getDepositFrWeb());
    }

    public long getDepositFrWallet() {
        return depositFrWallet;
    }
    public void setDepositFrWallet(long deposit) {
        this.depositFrWallet = deposit;
        this.changed.put(FIELD_DEPOSIT_FROM_WALLET, getDepositFrWallet());
    }

    public long getDepositFrWebProvisional() {
        return depositFrWebProvisional;
    }

    public void setDepositFrWebProvisional(long deposit) {
        this.depositFrWebProvisional = deposit;
        this.changed.put(FIELD_DEPOSIT_FROM_WEB_PROVISIONAL, this.getDepositFrWebProvisional());
    }

    public long getDepositFrWalletProvisional() {
        return depositFrWalletProvisional;
    }

    public void setDepositFrWalletProvisional(long deposit) {
        this.depositFrWalletProvisional = deposit;
        this.changed.put(FIELD_DEPOSIT_FROM_WALLET_PROVISIONAL, this.getDepositFrWalletProvisional());
    }

    public long getMoneySilver() {
        return moneySilver;
    }

    public void setMoneySilver(long moneySilver) {
        this.moneySilver = moneySilver;
        this.changed.put(FIELD_USER_SILVER, this.getMoneySilver());
    }

    public long getWinGold() {
        return winGold;
    }

    public void setWinGold(long winGold) {
        this.winGold = winGold;
        this.changed.put(FIELD_WIN_GOLD, this.getWinGold());
    }

    public long getWinSilver() {
        return winSilver;
    }

    public void setWinSilver(long winSilver) {
        this.winSilver = winSilver;
        this.changed.put(FIELD_WIN_SILVER, this.getWinSilver());
    }
    @Override
    public UserMoney updateDB() throws IllegalAccessException {
        MUserMoney.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public UserMoney saveDB() throws IllegalAccessException, NoSuchFieldException {
        MUserMoney.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }
}
