package games.core.database.model;

import games.core.user.User;
import games.minigame.database.model.MWheelUserHistory;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import games.core.database.MongoConst;
import games.core.database.MongoManager;
import games.core.database.document.UserInfo;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


@CollectionClass(MongoConst.COLL_USER)
public class MUserInfo extends MGModelAdapter<UserInfo> implements MongoConst {
    public static MUserInfo instance;
    public static MUserInfo getInstance() {
        if (instance == null) instance = new MUserInfo();
        return instance;
    }
    private MUserInfo() {
        super(MongoManager.getInstance().getDatabase());
    }

    public UserInfo getUserInfoByToken(String token) throws NotFoundDocumentInDatabase, NoSuchFieldException, IllegalAccessException {
        UserInfo userInfo = new UserInfo();
        userInfo.setToken(token);
        this.updateFromMongoByUniqueField(userInfo, FIELD_ACCESS_TOKEN);
        return userInfo;
    }

    public List<Integer> getListUserId(){
        ArrayList<Document> result = new ArrayList<>(1000000);
        Document project = new Document().append("_id", 1);
        this.mgCollection.find().projection(project).into(result);
        ArrayList<Integer> returnList = new ArrayList<>(100000);
        result.forEach(document -> {
                returnList.add(document.getInteger("_id"));
        });
        return returnList;
    }

    //kiem tra sdt nay da ton tai chua
    public boolean checkAvailablePhoneNumber(String phoneNumber) {
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone(phoneNumber);
        try {
            this.updateFromMongoByUniqueField(userInfo, FIELD_USER_PHONE);
            if (userInfo != null) {
                return true;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    public static void main(String[] args) {
//        long time1 = System.currentTimeMillis();
//        long count = MUserInfo.getInstance().getCountCollection();
//        long time2 = System.currentTimeMillis();
//        System.out.print("Time check get count = " + (time2 - time1) + " - count user = " + count)  ;
//
//        time1 = System.currentTimeMillis();
//        count = MUserMoney.getInstance().getCountCollection();
//        time2 = System.currentTimeMillis();
//        System.out.print("Time check get count = " + (time2 - time1) + " - count user = " + count)  ;
//
//        time1 = System.currentTimeMillis();
//        count = MWheelUserHistory.getInstance().getCountCollection();
//        time2 = System.currentTimeMillis();
//        System.out.print("Time check get count = " + (time2 - time1) + " - count user = " + count)  ;
//    }
}
