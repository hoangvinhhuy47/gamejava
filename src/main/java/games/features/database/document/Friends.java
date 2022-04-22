package games.features.database.document;

import games.features.database.MongoFeatureConst;
import games.features.database.model.MFriends;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;
import org.bson.Document;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tuanhoang on 9/14/17.
 */
@CollectionClass(MongoFeatureConst.COLL_FRIEND)
public class Friends extends MGDocument implements MongoFeatureConst{

    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int userId = 0;

    @DocumentField(FIELD_FRIEND_LIST_FRIENDS)
    private Document listFriends = new Document();

    @DocumentField(FIELD_FRIEND_LIST_FRIENDS_FACEBOOK)
    private Document listFriendsFacebook = new Document();

    @DocumentField(FIELD_FRIEND_LIST_FRIENDS_REQUEST)
    private Document listFriendsRequest = new Document();

    @DocumentField(FIELD_FRIEND_LIST_FRIENDS_REJECT)
    private Document listFriendsReject = new Document();


    @Override
    public MGDocument updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase {
        MFriends.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        MFriends.getInstance().saveToMongo(this);
        return this;
    }

    public void declineFriend(Integer friendId) {
        if (listFriendsRequest.containsKey("" + friendId)) {
            listFriendsRequest.remove("" + friendId);
            this.setListFriendsRequest(this.listFriendsRequest);
        }

    }

    public void removeFriend(Integer friendId) {
        if (listFriends.containsKey("" + friendId)) {
            listFriends.remove("" + friendId);
            this.setListFriends(listFriends);
        }

    }

    public void rejectFriend(Integer friendId) {
        if (!listFriendsReject.containsKey("" + friendId)) {
            listFriendsReject.put("" + userId, ZonedDateTime.now().toEpochSecond());
            this.setListFriendsReject(this.listFriendsReject);
        }


        if (listFriendsRequest.containsKey(friendId)) {
            listFriendsRequest.remove("" + friendId);
            this.setListFriendsRequest(this.listFriendsRequest);
        }

    }

    public void addFriendsSuccess(Integer friendId) {
        if (!listFriends.containsKey("" + friendId)) {
            listFriends.put("" + friendId, ZonedDateTime.now().toEpochSecond());
            this.setListFriends(this.listFriends);
        }


        if (listFriendsReject.containsKey("" + friendId)) {
            listFriendsReject.remove("" + friendId);
            this.setListFriendsReject(this.listFriendsReject);
        }


        if (listFriendsRequest.containsKey("" + friendId)) {
            listFriendsRequest.remove("" + friendId);
            this.setListFriendsRequest(this.listFriendsRequest);
        }

    }

    public void requestAddFriends (Integer friendIds) {
        if (!listFriendsRequest.containsKey("" + friendIds)) {
            listFriendsRequest.put("" + friendIds, ZonedDateTime.now().toEpochSecond());
            this.setListFriendsRequest(this.listFriendsRequest);
        }

    }

    public boolean checkIsFriend(Integer friendId) {
        if (listFriends.containsKey("" + friendId)) {
            return true;
        }
        if (listFriendsFacebook.containsKey("" + friendId)) {
            return true;
        }
        return false;
    }

    public boolean checkExistInListRequest(Integer friendId) {
        if (listFriendsRequest.containsKey("" + friendId))
            return true;
        return false;
    }

    public boolean checkExistInReject(Integer friendId) {
        if (listFriendsReject.containsKey("" + friendId))
            return true;
        return false;
    }

    List<Integer> getListIdByDocs (Document document) {
        List<Integer> listFriendIds = new ArrayList<>();
        for (Map.Entry entry : document.entrySet()) {
            int friendIds = Integer.parseInt(String.valueOf(entry.getKey()));
            listFriendIds.add(friendIds);
        }
        return listFriendIds;
    }

    public List<Integer> getListFriendIds () {
        return getListIdByDocs(listFriends);
    }

    public List<Integer> getListFriendFacebook() {
        return getListIdByDocs(listFriendsFacebook);
    }

    public List<Integer> getListFriendRequest() {
        return getListIdByDocs(listFriendsRequest);
    }

    public List<Integer> getListReject() {
        return getListIdByDocs(listFriendsReject);
    }

    //region getter - setter

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.changed.put(FIELD_DEFAULT_ID, this.userId);
    }

    public Document getListFriends() {
        return listFriends;
    }

    public void setListFriends(Document listFriends) {
        this.listFriends = listFriends;
        this.changed.put(FIELD_FRIEND_LIST_FRIENDS, this.listFriends);
    }

    public Document getListFriendsFacebook() {
        return listFriendsFacebook;
    }

    public void setListFriendsFacebook(Document listFriendsFacebook) {
        this.listFriendsFacebook = listFriendsFacebook;
        this.changed.put(FIELD_FRIEND_LIST_FRIENDS_FACEBOOK, this.listFriendsFacebook);
    }

    public Document getListFriendsRequest() {
        return listFriendsRequest;
    }

    public void setListFriendsRequest(Document listFriendsRequest) {
        this.listFriendsRequest = listFriendsRequest;
        this.changed.put(FIELD_FRIEND_LIST_FRIENDS_REQUEST, this.listFriendsRequest);
    }

    public Document getListFriendsReject() {
        return listFriendsReject;
    }

    public void setListFriendsReject(Document listFriendsReject) {
        this.listFriendsReject = listFriendsReject;
        this.changed.put(FIELD_FRIEND_LIST_FRIENDS_REJECT, this.listFriendsReject);
    }

    //endgreion
}
