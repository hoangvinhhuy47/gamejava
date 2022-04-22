package games.core.database.model;

import games.core.database.MongoManager;
import games.minigame.database.MongoMiniGameConst;
import games.core.database.document.UserMiniGame;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;

/**
 * Created by tuanhoang on 8/17/17.
 */
@CollectionClass(MongoMiniGameConst.COLL_USER_MINI_GAME)
public class MUserMiniGame extends MGModelAdapter<UserMiniGame> implements MongoMiniGameConst{

    public static MUserMiniGame instance;
    public static MUserMiniGame getInstance() {
        if (instance == null) instance = new MUserMiniGame();
        return instance;
    }
    protected MUserMiniGame() {
        super(MongoManager.getInstance().getDatabase());
    }

    public UserMiniGame getUserMiniGameByUserId(int userID) {
        UserMiniGame userMiniGame = new UserMiniGame(userID);
        try {
            this.updateFromMongo(userMiniGame);
            return userMiniGame;
        } catch (Exception e) {
            //e.printStackTrace();

            //không tìm thấy record với user => create

            userMiniGame.setTicketWheel(10);
            userMiniGame.setNumOfTrial(3);
            userMiniGame.setRewardCountDay(0);
            userMiniGame.setDayReward(0);
            this.saveToMongo(userMiniGame);
            return userMiniGame;
        }
    }

    public UserMiniGame getUserMiniGameByPromoteCode(String promoteCode) throws NoSuchFieldException, IllegalAccessException {
        UserMiniGame userMiniGame = new UserMiniGame();
        userMiniGame.setPromoteCode(promoteCode);
        this.updateFromMongoByUniqueField(userMiniGame, FIELD_PROMOTE_CODE);
        return userMiniGame;
    }
}
