package games.core.database.document;

import games.Poker.ENUM.PokerConstanst;
import games.core.exception.currency.CannotLessThanZero;
import games.core.database.MongoConst;
import games.core.database.model.MRoomInfo;
import games.shootingfish.GameLobby;
import games.tx.TaiXiuConst;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import org.bson.Document;


@CollectionClass(MongoConst.COLL_ROOM_INFO)
public class DRoomInfo extends MGDocument implements MongoConst{
    @DocumentFieldPK
    @DocumentField(FIELD_DEFAULT_ID)
    private int id;

    @DocumentField(FIELD_ROOM_INFO_GAME_ID)
    private int gameId;

    @DocumentField(FIELD_ROOM_INFO_POT)
    private long pot;

    @DocumentField(FIELD_ROOM_INFO_MIN_JACKPOT)
    private long minJackpot;

    @DocumentField(FIELD_ROOM_INFO_MAX_JACKPOT)
    private long maxJackpot;

    @DocumentField(FIELD_ROOM_INFO_POT_PERCENT)
    private double potPercent;

    @DocumentField(FIELD_ROOM_INFO_FUND_PERCENT)
    private double fundPercent;

    @DocumentField(FIELD_ROOM_INFO_FUND)
    private long fund;

    @DocumentField(FIELD_ROOM_INFO_FUND_DEALER)
    private long fundDealer;

    @DocumentField(FIELD_ROOM_INFO_SLOT_MIN_STEP)
    private int slotMinStep;

    @DocumentField(FIELD_ROOM_INFO_SLOT_MAX_STEP)
    private int slotMaxStep;

    @DocumentField(FIELD_ROOM_INFO_SLOT_STEP_MONEY)
    private int slotStepMoney;

    @DocumentField(FIELD_ROOM_INFO_SLOT_WIN_RATE)
    private double slotWinRate;

    private DRoomInfo(){}

    public DRoomInfo(int roomId) throws NotFoundDocumentInDatabase {
        this.id = roomId;
        this.updateDB();
    }

    public DRoomInfo(int roomId, int gameId){
        this.setId(roomId);
        try {
            updateDB();
        } catch (NotFoundDocumentInDatabase notFoundDocumentInDatabase) {
            this.setGameId(gameId);
            saveDB();
        }
    }

    public static DRoomInfo initFishRoom(int roomId, long fund) {
        DRoomInfo dRoomInfo = new DRoomInfo();
        dRoomInfo.setId(roomId);
        dRoomInfo.setGameId(GameLobby.GAME_ID);
        try {
            dRoomInfo.updateDB();
        } catch (NotFoundDocumentInDatabase e){
            dRoomInfo.setFund(fund);
            dRoomInfo.saveDB();
        }
        return dRoomInfo;
    }

    public DRoomInfo(
            int roomId, int gameId, long pot,
            long fund, double potPercent, double fundPercent,
            int slotMinStep, int slotMaxStep,
            int slotStepMoney, long minJackpot, long maxJackpot,
            double slotWinRate
    )
    {
        this.setId(roomId);
        try {
            updateDB();
        } catch (NotFoundDocumentInDatabase notFoundDocumentInDatabase) {
            setGameId(gameId);
            setPot(pot);
            setFund(fund);
            setPotPercent(potPercent);
            setFundPercent(fundPercent);
            setSlotMinStep(slotMinStep);
            setSlotMaxStep(slotMaxStep);
            setSlotStepMoney(slotStepMoney);
            setMinJackpot(minJackpot);
            setMaxJackpot(maxJackpot);
            setSlotWinRate(slotWinRate);
            saveDB();
        }
    }

    public static DRoomInfo initTaiXiuRoom(int roomId, long fund)
    {
        DRoomInfo dRoomInfo = new DRoomInfo();
        dRoomInfo.setId(roomId);
        dRoomInfo.setGameId(roomId);
        try {
            dRoomInfo.updateDB();
        } catch (NotFoundDocumentInDatabase e){
            dRoomInfo.setFund(fund);
            dRoomInfo.saveDB();
        }
        return dRoomInfo;
    }
    public static DRoomInfo initPokerRoom(int roomId, long fund)
    {
        DRoomInfo dRoomInfo = new DRoomInfo();
        dRoomInfo.setId(roomId);
        dRoomInfo.setGameId(PokerConstanst.POKER_GAMEID_MONGODB);
        try {
            dRoomInfo.updateDB();
        } catch (NotFoundDocumentInDatabase e){
            dRoomInfo.setFund(fund);
            dRoomInfo.saveDB();
        }
        return dRoomInfo;
    }

    @Override
    public MGDocument updateDB() throws NotFoundDocumentInDatabase {
        MRoomInfo.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() {
        MRoomInfo.getInstance().saveToMongo(this);
        return this;
    }

    public DRoomInfo updateByUniqueFields(String ...uniqueFields) throws NoSuchFieldException, IllegalAccessException {
        MRoomInfo.getInstance().updateFromMongoByUniqueField(this, uniqueFields);
        return this;
    }


    public synchronized void plusPot(long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        if (money == 0){
            return;
        }

        // Ràng buộc max pot
        long newPot = this.pot + money;
        if (newPot > this.getMaxJackpot()){
            newPot = this.getMaxJackpot();
        }

        // newPost - this.pot luôn >= 0
        this.increaseMoney(newPot - this.pot, FIELD_ROOM_INFO_POT);
        this.setPot(newPot);
    }

    public void subPot(long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        if (money == 0){
            return;
        }

        this.increaseMoney(-money, FIELD_ROOM_INFO_POT);
        this.setPot(this.pot - money);
    }

    public void plusFund(long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        if (money == 0){
            return;
        }
        this.increaseMoney(money, FIELD_ROOM_INFO_FUND);
        this.setFund(this.fund+money);
    }

    public synchronized void subFund(long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        if (money == 0){
            return;
        }
        this.increaseMoney(-money, FIELD_ROOM_INFO_FUND);
        this.setFund(this.fund-money);
    }

    private synchronized void increaseMoney(long money, String field) {
        Document filter = new Document(FIELD_DEFAULT_ID, this.id);
        Document increase = new Document("$inc", new Document(field, money));
        MRoomInfo.getInstance().getMgCollection().updateOne(filter, increase);
    }

    public synchronized void subFundDealer(long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        if (money == 0){
            return;
        }
        this.increaseMoney(-money, FIELD_ROOM_INFO_FUND_DEALER);
        this.setFundDealer(this.fund-money);
    }

    public synchronized void plusFundDealer(long money) throws CannotLessThanZero {
        if (money < 0){
            throw new CannotLessThanZero();
        }
        if (money == 0){
            return;
        }
        this.increaseMoney(money, FIELD_ROOM_INFO_FUND_DEALER);
        this.setFundDealer(this.fundDealer +money);
    }

    public long getPot() {
        return pot;
    }

    public void setPot(long pot) {
        this.pot = pot;
        this.changed.append(FIELD_ROOM_INFO_POT, pot);
    }

    public long getFund() {
        return fund;
    }

    public void setFund(long fund) {
        this.fund = fund;
        this.changed.append(FIELD_ROOM_INFO_FUND, fund);
    }

    public long getFundDealer() {
        return fundDealer;
    }

    public void setFundDealer(long fundDealer) {
        this.fundDealer = fundDealer;
        this.changed.append(FIELD_ROOM_INFO_FUND_DEALER, fundDealer);
    }

    public double getPotPercent() {
        return potPercent;
    }

    public void setPotPercent(float potPercent) {
        this.potPercent = potPercent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
        this.changed.append(FIELD_ROOM_INFO_GAME_ID, gameId);
    }

    public void setPotPercent(double potPercent) {
        this.potPercent = potPercent;
        this.changed.append(FIELD_ROOM_INFO_POT_PERCENT, potPercent);
    }

    public int getSlotMinStep() {
        return slotMinStep;
    }

    public void setSlotMinStep(int slotMinStep) {
        this.slotMinStep = slotMinStep;
        this.changed.append(FIELD_ROOM_INFO_SLOT_MIN_STEP, slotMinStep);
    }

    public int getSlotMaxStep() {
        return slotMaxStep;
    }

    public void setSlotMaxStep(int slotMaxStep) {
        this.slotMaxStep = slotMaxStep;
        this.changed.append(FIELD_ROOM_INFO_SLOT_MAX_STEP, slotMaxStep);
    }

    public int getSlotStepMoney() {
        return slotStepMoney;
    }

    public void setSlotStepMoney(int slotStepMoney) {
        this.slotStepMoney = slotStepMoney;
        this.changed.append(FIELD_ROOM_INFO_SLOT_STEP_MONEY, slotStepMoney);
    }

    public double getSlotWinRate() {
        return slotWinRate;
    }

    public void setSlotWinRate(double slotWinRate) {
        changed.put(FIELD_ROOM_INFO_SLOT_WIN_RATE, slotWinRate);
        this.slotWinRate = slotWinRate;
    }

    public long getMaxJackpot() {
        return maxJackpot;
    }

    private void setMaxJackpot(long maxJackpot) {
        this.changed.put(FIELD_ROOM_INFO_MAX_JACKPOT, maxJackpot);
        this.maxJackpot = maxJackpot;
    }

    public long getMinJackpot() {
        return minJackpot;
    }

    private void setMinJackpot(long minJackpot) {
        this.changed.put(FIELD_ROOM_INFO_MIN_JACKPOT, minJackpot);
        this.minJackpot = minJackpot;
    }

    public double getFundPercent() {
        return fundPercent;
    }

    public void setFundPercent(double fundPercent) {
        this.fundPercent = fundPercent;
        this.changed.put(FIELD_ROOM_INFO_FUND_PERCENT, fundPercent);
    }
}
