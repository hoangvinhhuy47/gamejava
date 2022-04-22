package games.Poker.UserPokerGame_User;

import com.mongodb.client.MongoDatabase;
import games.core.database.MongoManager;
import games.core.database.document.UserMiniGame;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;

@CollectionClass("userPokerGame")
public class MUserPokerGame extends MGModelAdapter<UserPokerGame> {
    public static MUserPokerGame instance;

    public static MUserPokerGame getInstance() {
        if (instance == null) {
            instance = new MUserPokerGame();
        }
        return instance;
    }

    protected MUserPokerGame() {
        super(MongoManager.getInstance().getDatabase());
    }

    public UserPokerGame GetUserPokerGameByID(int userID) {
        UserPokerGame userPokerGame = new UserPokerGame(userID);
        try {
            this.updateFromMongo(userPokerGame);
            return userPokerGame;
        } catch (Exception e) {
            userPokerGame.setCurrentMoney_Phase(0);
            userPokerGame.setMoney_PREFLOP(0);
            userPokerGame.setMoney_FLOP(0);
            userPokerGame.setMoney_TURN(0);
            userPokerGame.setMoney_RIVER(0);
            this.saveToMongo(userPokerGame);
            return userPokerGame;
        }
    }
}
