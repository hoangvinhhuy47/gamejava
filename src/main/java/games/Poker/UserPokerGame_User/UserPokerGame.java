package games.Poker.UserPokerGame_User;

import games.Poker.ENUM.PHASE;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;

public class UserPokerGame extends MGDocument {
    @DocumentField("_id")
    @DocumentFieldPK
    private int userId = 0;
    @DocumentField("currentMoney_Phase")
    private long currentMoney_Phase = 0;
    @DocumentField("money_PREFLOP")
    private long money_PREFLOP = 0;
    @DocumentField("money_FLOP")
    private long money_FLOP = 0;
    @DocumentField("money_TURN")
    private long money_TURN = 0;
    @DocumentField("money_RIVER")
    private long money_RIVER = 0;
    public UserPokerGame () {

    }
    public UserPokerGame(int userId){
        this.userId = userId;
    }

    @Override
    public UserPokerGame updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase {
        MUserPokerGame.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public UserPokerGame saveDB() throws IllegalAccessException, NoSuchFieldException {
        MUserPokerGame.getInstance().updateFromMongo(this);
        this.changed.clear();
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.changed.put("_id", userId);
    }

    public long getCurrentMoney_Phase() {
        return currentMoney_Phase;
    }

    public void setCurrentMoney_Phase(long currentMoney_Phase) {
        this.currentMoney_Phase = currentMoney_Phase;
        this.changed.put("setCurrentMoney_Phase", currentMoney_Phase);
    }

    public long getMoney_PREFLOP() {
        return money_PREFLOP;
    }

    public void setMoney_PREFLOP(long money_PREFLOP) {
        this.money_PREFLOP = money_PREFLOP;
        this.changed.put("money_PREFLOP", money_PREFLOP);
    }

    public long getMoney_FLOP() {
        return money_FLOP;
    }

    public void setMoney_FLOP(long money_FLOP) {
        this.money_FLOP = money_FLOP;
        this.changed.put("money_FLOP", money_FLOP);
    }

    public long getMoney_TURN() {
        return money_TURN;
    }

    public void setMoney_TURN(long money_TURN) {
        this.money_TURN = money_TURN;
        this.changed.put("money_TURN", money_TURN);
    }

    public long getMoney_RIVER() {
        return money_RIVER;
    }

    public void setMoney_RIVER(long money_RIVER) {
        this.money_RIVER = money_RIVER;
        this.changed.put("money_RIVER", money_RIVER);
    }

    public void CurrentMoney_CurrentPhase (long CurrentMoney ,int CurrentPhase) {
        this.currentMoney_Phase = CurrentMoney;
        if (CurrentPhase == PHASE.PRE_FLOP){
            this.money_PREFLOP = this.currentMoney_Phase;
        }
        if (CurrentPhase == PHASE.FLOP){
            this.money_FLOP = this.currentMoney_Phase;
        }
        if (CurrentPhase == PHASE.TURN){
            this.money_TURN = this.currentMoney_Phase;
        }
        if (CurrentPhase == PHASE.RIVER){
            this.money_RIVER = this.currentMoney_Phase;
        }
        this.currentMoney_Phase = 0;
    }
}
