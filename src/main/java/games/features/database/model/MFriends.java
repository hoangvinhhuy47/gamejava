package games.features.database.model;

import games.core.database.MongoManager;
import games.features.database.MongoFeatureConst;
import games.features.database.document.Friends;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;

/**
 * Created by tuanhoang on 9/14/17.
 */
@CollectionClass(MongoFeatureConst.COLL_FRIEND)
public class MFriends extends MGModelAdapter<Friends> implements MongoFeatureConst {

    private static MFriends instance;
    public static MFriends getInstance() {
        if (instance == null) instance = new MFriends();
        return instance;
    }

    protected MFriends() {
        super(MongoManager.getInstance().getDatabase());
    }

    public Friends getFriendById(int userId) throws NoSuchFieldException, IllegalAccessException {
        Friends friends = new Friends();
        friends.setUserId(userId);
        try {
            this.updateFromMongo(friends);
        } catch (Exception e) {
            this.saveToMongo(friends);
        }
        return friends;
    }
}
